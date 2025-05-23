package com.Cvn.Verifier;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.toppanidgate.idenkey.common.model.ReturnCode;
import com.Cvn.Database.ChannelTrustService;
import com.Cvn.Database.DevicesService;
import com.Cvn.Database.KeyStore;
import com.Cvn.Database.KeyStoreService;
import com.Cvn.Encryptor.AES;
import com.Cvn.Encryptor.CvnSecrets;
import com.Cvn.Encryptor.Encode;
import com.Cvn.Encryptor.Hash;
import com.Cvn.Encryptor.Rndm;
import com.Cvn.KObjs.KObjMain;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class Verifier {
	private KObjMain kObjA = null, kObjB = null, kObjC = null;
	private final Gson gson = new Gson();
	private String kyA = null, kyB = null, kyC = null;

	private Logger logger = LogManager.getLogger(Verifier.class);

	@Value("${HSMProvider}")
	private String HSMProvider;

	@Value("${PersoKeyAlias}")
	private String HSM_KAlias;

	@Value("${HSMPlusMode}")
	private String HSMPlusMode;

	@Value("${SetKeyAlias}")
	private String SetKeyAlias;

	@Value("${PersoKeyAlias}")
	private String PersoKeyAlias;

	@Autowired
	DevicesService devicesService;

	@Autowired
	Rndm rndm;

	@Autowired
	KeyStoreService keyStoreService;

	@Autowired
	ChannelTrustService channelTrustService;

	public Verifier() {
	}

	/**
	 * According to different HSM-Provider library, generate a new seed.
	 * 
	 * @param esn
	 * @param sessionId
	 * @return a new seed binary(if it's null, an error occurred).
	 */
	public byte[] generateSEED(String esn, String sessionId) {


		if (HSMProvider.equals("ECSP") || HSMProvider.equals("SafeNet")) {
			String ky = "";
			String dataStr = null;
			HashMap<String, String> decData = null, encData = null;


			// HSM plus mode
			if ("Y".equals(HSMPlusMode)) {
				logger.trace("[" + sessionId + "][generateSEED-P] - # Using HSM Plus mode");

				if (kyA == null) {
					KeyStore keyStore = keyStoreService.findOne("MasterKey");

					if (keyStore.isEmpty()) {
						logger.error("[" + sessionId + "][generateSEED-P] - # Fetch master key from DB failed.");
						return null;
					}

						logger.debug("[" + sessionId + "][generateSEED] - # Encrypted Master key: " + keyStore.getPrikey());

					dataStr = decrypt_AES(keyStore.getPrikey(), SetKeyAlias, sessionId);
					decData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

						logger.debug(
								"[" + sessionId + "][generateSEED] - # Decrypted Master key: " + decData.get("Data"));

					ky = Hash.encode_SHA256_Hex(decData.get("Data") + CvnSecrets.seedSec_ret, sessionId);

						logger.debug("[" + sessionId + "][generateSEED] - # Hashed key: " + ky);

						dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
					encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

					kyA = encData.get("EncData");

						logger.debug("[" + sessionId + "][generateSEED] - # KeyA: " + kyA);

				}

				ky = kyA;

			} else {
				logger.trace("[" + sessionId + "][generateSEED-P] - # Not using HSM Plus mode");
				if (kObjC != null) {
					kObjC = null;
				}

				KeyStore keyStore = keyStoreService.findOne("MasterKey");

				if (keyStore.isEmpty()) {
					logger.error("[" + sessionId + "][generateSEED] - # Fetch master key from DB failed.");
					return null;
				}

					logger.debug("[" + sessionId + "][generateSEED] - # Encrypted Master key: " + keyStore.getPrikey());

				dataStr = decrypt_AES(keyStore.getPrikey(), SetKeyAlias, sessionId);
				decData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

					logger.debug(
							"[" + sessionId + "][generateSEED] - # Decrypted Master key: " + decData.get("Data"));

				ky = Hash.encode_SHA256_Hex(decData.get("Data") + CvnSecrets.seedSec_ret, sessionId);

					logger.debug("[" + sessionId + "][generateSEED] - # Hashed key: " + ky);

					dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
				encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

				ky = encData.get("EncData");
					logger.debug("[" + sessionId + "][generateSEED] - # KeyA: " + ky);
			}

			logger.debug("[" + sessionId + "][generateSEED] - # Generate SeedSrc (AES with key index: " + ky + ")");

			byte[] encryptedData;
			try {
				encryptedData = AES.drvRfc2898_encrypt(ky.getBytes("UTF-8"), (esn + esn + esn).getBytes("UTF-8"),
						sessionId);
			} catch (UnsupportedEncodingException e) {
				logger.error("[" + sessionId + "][generateSEED] - # Generate Seed (" + HSMProvider + ") failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			} catch (Exception e) {
				logger.error("[" + sessionId + "][generateSEED] - # Generate Seed (" + HSMProvider + ") failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			}

			return Hash.encode_SHA1(encryptedData, sessionId);

		} else {
			// CVN mode
			try {
				String dataStr;
				try {
					dataStr = Encode.byteToHex((esn + esn + esn).getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					logger.error("[" + sessionId
							+ "[generateSEED] - # Generate SeedSrc (Cvn-AES) failed during converting data: "
							+ e1.getMessage());
					return null;
				}

				int dummyNeeded = dataStr.length() % 32;
				if (dummyNeeded > 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						dataStr += "00";
					}
				}

				byte[] plain_bin = Encode.hexToByte(dataStr);
				byte[] k_bin = HSM_KAlias.getBytes("UTF-8");
				byte[] c_bin = AES.drvRfc2898_encrypt(k_bin, plain_bin, sessionId);

				logger.info(
						"[" + sessionId + "][generateSEED] - # Generate Seed (Cvn-AES): " + Encode.byteToHex(c_bin));
				return Hash.encode_SHA1(c_bin, sessionId);

			} catch (UnsupportedEncodingException e) {
				logger.error(
						"[" + sessionId + "][generateSEED] - # Generate Seed (Cvn-AES) failed: " + e.getMessage());
				return null;
			} catch (Exception e) {
				logger.error(
						"[" + sessionId + "][generateSEED] - # Generate Seed (Cvn-AES) failed: " + e.getMessage());
				return null;
			}
		}
	}

	public byte[] generateSEEDKey(String seedSec, String devData, String pinHash, String sessionId) {

		// Combine all 4 factors of SeedKey.
		String combine = seedSec + devData + pinHash;
		byte[] combineStream;
		try {
			combineStream = combine.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("[" + sessionId + "][generateSEEDKey] - #Encoding error:[" + e.getMessage() + "]");
			return new byte[] {};
		}

		try {
			return Hash.encode_SHA256(combineStream, sessionId);
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generateSEEDKey] - #Computing seedkey error:[" + e.getMessage() + "]");
			return new byte[] {};
		}
	}

	/**
	 * Generate an electrical serial number for a device.
	 * 
	 * @param channel  :Char(2) as channel code.
	 * @param devType  :Char(1) as device type.
	 * @param unixTime :Current-millis as a long integer.
	 * @return
	 */
	public String generateESN(String channel, String devType, Long unixTime, String sessionId) {

		// ESN structure: Channel[2] + DevType[1] + HexUnix[10] + EsnSeq[3]
		String EsnHexUnix = Long.toHexString(unixTime);
		EsnHexUnix = EsnHexUnix.substring(EsnHexUnix.length() - 10, EsnHexUnix.length());
		String EsnSeq = devicesService.getNewEsnSeq(sessionId);
		if (EsnSeq.length() != 3) {
			logger.error("[" + sessionId + "][generateESN] - #Generate DB-EsnSeq failed: " + EsnSeq);
			return "";
		}

		return channel + devType + EsnHexUnix + EsnSeq;
	}

	/**
	 * Generate a XESN for mobile SDK. (Using esn to generate OTP is not allowed.)
	 * 
	 * @param esn
	 * @param sessionId
	 * @return
	 */
	public byte[] generateXESN(String esn, String sessionId) {

		String HSMKyAlias = PersoKeyAlias;

		logger.info("[" + sessionId + "][generateXESN] - #esn:[" + esn + "] - #HSMProvider:[" + HSMProvider
				+ "] - #HSMKeyAlias:[" + HSMKyAlias + "]");

		/**
		 * Compute AES encryption with ECSP/SafeNet services.
		 */
		if (HSMProvider.equals("ECSP") || HSMProvider.equals("SafeNet")) {
			String ky = "";
			String dataStr = null;
			HashMap<String, String> encData = null;

			logger.info("[" + sessionId + "][generateXESN] - # Generate xEsnSrc (AES with key index: " + ky + ")");

			// HSM plus mode
			if ("Y".equals(HSMPlusMode)) {
				logger.trace("[" + sessionId + "][generateXESN] - # Using HSM plus mode");

				if (kyB == null) {
					KeyStore keyStore = keyStoreService.findOne("MasterKey");

					if (keyStore.isEmpty()) {
						logger.error("[" + sessionId + "][generateXESN] - # Fetch master key from DB failed.");
						return null;
					}

						logger.debug("[" + sessionId + "][generateXESN] - # Encrypted Master key: " + keyStore.getPrikey());

					dataStr = decrypt_AES(keyStore.getPrikey(), SetKeyAlias, sessionId);
					encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

						logger.debug(
								"[" + sessionId + "][generateXESN] - # Decrypted Master key: " + encData.get("Data"));

					ky = Hash.encode_SHA256_Hex(encData.get("Data") + CvnSecrets.esnSec_ret, sessionId);

						logger.debug("[" + sessionId + "][generateXESN] - # Hashed key: " + ky);

					dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
					encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

					kyB = encData.get("EncData");

						logger.debug("[" + sessionId + "][generateXESN] - # KeyB: " + kyB);
				}
				ky = kyB;

			} else {
				logger.trace("[" + sessionId + "][generateXESN] - # Not using HSM plus mode");
				if (kObjB != null) {
					kObjB = null;
				}

				KeyStore keyStore = keyStoreService.findOne("MasterKey");

				if (keyStore.isEmpty()) {
					logger.error("[" + sessionId + "][generateXESN] - # Fetch master key from DB failed.");
					return null;
				}

					logger.debug("[" + sessionId + "][generateXESN] - # Encrypted Master key: " + keyStore.getPrikey());

				dataStr = decrypt_AES(keyStore.getPrikey(), "SetKeyAlias", sessionId);
				encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

					logger.debug(
							"[" + sessionId + "][generateXESN] - # Decrypted Master key: " + encData.get("Data"));

				ky = Hash.encode_SHA256_Hex(encData.get("Data") + CvnSecrets.esnSec_ret, sessionId);

					logger.debug("[" + sessionId + "][generateXESN] - # Hashed key: " + ky);

					dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
				encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

				ky = encData.get("EncData");

					logger.debug("[" + sessionId + "][generateXESN] - # KeyB: " + ky);
			}

			byte[] enData = null;
			try {
				enData = AES.drvRfc2898_encrypt(ky.getBytes("UTF-8"),
						(esn + CvnSecrets.commonSec_ret).getBytes("UTF-8"), sessionId);
			} catch (UnsupportedEncodingException e) {
				logger.error("[" + sessionId + "][generateXESN] - # Generate xEsnSrc (" + HSMProvider + ") failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			} catch (Exception e) {
				logger.error("[" + sessionId + "][generateXESN] - # Generate xEsnSrc (" + HSMProvider + ") failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			}

			ky = "";
			return Hash.encode_SHA1(enData, sessionId);
		}

		/**
		 * Compute AES encryption with Cvn services.
		 */
		else {
			try {
				String dataStr;
				try {
					dataStr = Encode.byteToHex((esn + CvnSecrets.commonSec_ret).getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					logger.error("[" + sessionId
							+ "[generateXESN] - # Generate SeedSrc (Cvn-AES) failed during converting data: "
							+ e1.getMessage());
					return null;
				}

				int dummyNeeded = dataStr.length() % 32;
				if (dummyNeeded > 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						dataStr += "00";
					}
				}

				byte[] plain_bin = Encode.hexToByte(dataStr);
				byte[] k_bin = CvnSecrets.esnSec_ret.getBytes("UTF-8");
				byte[] c_bin = AES.drvRfc2898_encrypt(k_bin, plain_bin, sessionId);

				logger.info(
						"[" + sessionId + "][generateXESN] - # Generate xEsnSrc (Cvn-AES): " + Encode.byteToHex(c_bin));
				return Hash.encode_SHA1(c_bin, sessionId);

			} catch (UnsupportedEncodingException e) {
				logger.error("[" + sessionId + "][generateXESN] - # Generate xEsnSrc (Cvn-AES) failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			} catch (Exception e) {
				logger.error("[" + sessionId + "][generateXESN] - # Generate xEsnSrc (Cvn-AES) failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			}
		}

	}

	/**
	 * Use 3 factors to generate ESKey to protect ES-Package.
	 * 
	 * @param esnSec     :
	 * @param devData
	 * @param hexMercury
	 * @param sessionId
	 * @return
	 */
	public byte[] generateESKey(String esnSec, String devData, String hexMercury, String sessionId) {

		logger.info("[" + sessionId + "][generateESKey] - #esnSecret:[" + esnSec + "] - #devData:[" + devData
				+ "] - #MercuryA:[" + hexMercury + "]");

		String combine = esnSec + devData + hexMercury;
		byte[] combineStream;
		try {
			combineStream = combine.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("[" + sessionId + "][generateESKey] - #Encoding error:[" + e.getMessage() + "]");
			return new byte[] {};
		}

		try {
			return Hash.encode_SHA256(combineStream, sessionId);
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generateESKey] - #Computing SHA256 error:[" + e.getMessage() + "]"
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new byte[] {};
		}
	}

	public byte[] generateDID(String esn, String sessionId) throws Exception {

		// GEN dynamic id with HSM key3
		String didFactors = (esn + CvnSecrets.masterSec_ret);

		/**
		 * Compute AES encryption with ECSP/SafeNet services.
		 */
		if (HSMProvider.equals("ECSP") || HSMProvider.equals("SafeNet")) {
			String ky = "", HsmKy = "";
			String dataStr = null;
			HashMap<String, String> encData = null;

			// no need to select key because did key is L1 key, which is KeyA
			HsmKy = "PersoKeyAlias";

			logger.info(
					"[" + sessionId + "][generateDID] - # Generate dynamicIdSrc (AES with key index: " + ky + ")");

			if ("Y".equals(HSMPlusMode)) {
				logger.trace("[" + sessionId + "][generateDID] - # Using HSM plus mode");

				if (kyC == null) {
					KeyStore keyStore = keyStoreService.findOne("MasterKey");

					if (keyStore.isEmpty()) {
						logger.error("[" + sessionId + "][generateDID] - # Fetch master key from DB failed.");
						return null;
					}

						logger.debug("[" + sessionId + "][generateDID] - # Encrypted Master key: " + keyStore.getPrikey());

					dataStr = decrypt_AES(keyStore.getPrikey(),SetKeyAlias, sessionId);
					encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

						logger.debug(
								"[" + sessionId + "][generateDID] - # Decrypted Master key: " + encData.get("Data"));

					ky = Hash.encode_SHA256_Hex(encData.get("Data") + CvnSecrets.didSec_ret, sessionId);

						logger.debug("[" + sessionId + "][generateDID] - # Hashed key: " + ky);

						dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
					encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
					}.getType());

					kyC = encData.get("EncData");
					logger.debug("[" + sessionId + "][generateDID] - # KeyC: " + kyC);
				}
				ky = kyC;

			} else {
				logger.trace("[" + sessionId + "][generateDID] - # Not using HSM plus mode");

				if (kObjA != null) {
					kObjA = null;
				}

				KeyStore keyStore = keyStoreService.findOne("MasterKey");

				if (keyStore.isEmpty()) {
					logger.error("[" + sessionId + "][generateDID] - # Fetch master key from DB failed.");
					return null;
				}

					logger.debug("[" + sessionId + "][generateDID] - # Encrypted Master key: " + keyStore.getPrikey());

				dataStr = decrypt_AES(keyStore.getPrikey(), "SetKeyAlias", sessionId);
				encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

//				Log4j.log
//				.debug("[" + sessionId + "][generateDID] - # Decrypted Master key: " + encData.get("Data"));

				ky = Hash.encode_SHA256_Hex(encData.get("Data") + CvnSecrets.didSec_ret, sessionId);

				logger.debug("[" + sessionId + "][generateDID] - # Hashed key: " + ky);

				dataStr = encrypt_AES(ky, PersoKeyAlias, sessionId);
				encData = gson.fromJson(dataStr, new TypeToken<HashMap<String, String>>() {
				}.getType());

				ky = encData.get("EncData");
				logger.debug("[" + sessionId + "][generateDID] - # KeyC: " + ky);
			}

			byte[] enData = null;

			enData = AES.drvRfc2898_encrypt(ky.getBytes("UTF-8"), didFactors.getBytes("UTF-8"), sessionId);

			return Hash.encode_SHA256(enData, sessionId);
		}

		/**
		 * Compute AES encryption with Cvn services.
		 */
		else {
			try {
				String dataStr;
				try {
					dataStr = Encode.byteToHex(didFactors.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					logger.error("[" + sessionId
							+ "[generateDID] - # Generate SeedSrc (Cvn-AES) failed during converting data: "
							+ e1.getMessage());
					return null;
				}

				int dummyNeeded = dataStr.length() % 32;
				if (dummyNeeded > 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						dataStr += "00";
					}
				}

				byte[] plain_bin = Encode.hexToByte(dataStr);
				byte[] k_bin = CvnSecrets.esnSec_ret.getBytes("UTF-8");
				byte[] c_bin = AES.drvRfc2898_encrypt(k_bin, plain_bin, sessionId);

				logger.debug("[" + sessionId + "][generateDID] - # Generate dynamicIdSrc (Cvn-AES): "
						+ Encode.byteToHex(c_bin));
				return Hash.encode_SHA256(c_bin, sessionId);

			} catch (UnsupportedEncodingException e) {
				logger.error("[" + sessionId + "][generateDID] - # Generate dynamicIdSrc (Cvn-AES) failed: "
						+ e.getMessage() + ", stacktrace: " + gson.toJson(e.getStackTrace()));
				return null;
			}
		}
	}

	public byte[] generatePersoKey(String masterSec, String devData, String sessionId) throws Exception {

		byte[] masterStream = masterSec.getBytes("UTF-8");
		byte[] devDataStream = devData.getBytes("UTF-8");

		// Compute Hash-SHA1(ESN + Seed) to make DID.
		byte[] combined = new byte[masterStream.length + devDataStream.length];
		System.arraycopy(masterStream, 0, combined, 0, masterStream.length);
		System.arraycopy(devDataStream, 0, combined, masterStream.length, devDataStream.length);

		return Hash.encode_SHA256(combined, sessionId);
	}

	public String[] generatePersoData(String channel, String devType, String esnSec, String seedSec,
			String masterSec, String devData, String pinHash, String sessionId) {

		HashMap<String, Object> channelDetail = null;

		if (channel.length() <= 3) {
			channelDetail = channelTrustService.getChannelById(channel, sessionId);
		} else {
			channelDetail = channelTrustService.getChannelByName(channel, sessionId);
		}

		if (channelDetail == null) {
			return new String[] { ReturnCode.ChannelInvalidError, "Channel Invalid" };
		}

		// read OTP settings if channel is found
		int timeInterval = Integer.parseInt(channelDetail.get("OTP_Interval").toString());
		int otpLength = Integer.parseInt(channelDetail.get("OTP_Length").toString());

		// + Buildup PersoData-Identifier
		String perso_Identifier = "IDG";

		// + Buildup PersoData-ESPack-ESN
		java.util.Date myDate = new java.util.Date();
		long unixTime = myDate.getTime();
		String esn = generateESN(channel, devType, unixTime, sessionId);
		String Xesn = Encode.byteToHex(generateXESN(esn, sessionId));
		logger.info("[" + sessionId + "][generatePersoData] - #Generate Xesn(Hex): " + Xesn);

		/// + Buildup PersoData-ESPack-SeedPack
		byte[] seed = null;
		byte[] seedKy = null;
		String SeedPack = "--";
		if (!pinHash.equals("")) {
			seed = generateSEED(esn, sessionId);
			if (seed == null) {
				logger.error("[" + sessionId + "][generatePersoData] - #Generate Seed failed.");
				return new String[] { "" };
			}

			seedKy = generateSEEDKey(seedSec, devData, pinHash, sessionId);
			if (seedKy == null) {
				logger.error("[" + sessionId + "][generatePersoData] - #Generate SeedKey failed.");
				return new String[] { "" };
			}

			try {
				SeedPack = Encode.byteToHex(AES.drvRfc2898_encrypt(seedKy, seed, sessionId));
			} catch (Exception e) {
				logger.error("[" + sessionId + "][generatePersoData] - #Generate SeedKey failed.");
				return new String[] { "" };
			}
		}

		// + Buildup PersoData-ESPack
		HashMap<String, Object> ES = new HashMap<String, Object>();
		ES.put("ESN", Xesn);
		ES.put("SeedPack", SeedPack);
		String ES_JSON = gson.toJson(ES);

		String MercuryAB = rndm.generateRdmHexStr(20);
		byte[] ESKy = generateESKey(esnSec, devData, MercuryAB.substring(0, 10), sessionId);
		if (ESKy == null) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate ESKey failed.");
			return new String[] { "" };
		}

		String perso_ESPack;
		try {
			perso_ESPack = Encode
					.byteToHex(AES.drvRfc2898_encrypt(ESKy, ES_JSON.getBytes("UTF-8"), sessionId));
			logger.debug("[" + sessionId + "][generatePersoData] - #Generate ESkey(Hex) as basekey: "
					+ Encode.byteToHex(ESKy));
			logger.debug("[" + sessionId + "][generatePersoData] - #Generate ES_JSON(UTF8): " + ES_JSON);

		} catch (UnsupportedEncodingException e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate ESPack failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate ESPack failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		}

		// + Buildup PersoData-DevID
		String perso_DevID;
		try {
			perso_DevID = Encode.byteToHex(generateDID(esn, sessionId));
			logger.info("[" + sessionId + "][generatePersoData] - #Generate perso_DevID: " + perso_DevID);
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate DevID failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		}

		// + Buildup PersoData
		HashMap<String, Object> persoData = new HashMap<String, Object>();
		persoData.put("Identifier", perso_Identifier);
		persoData.put("ESPack", perso_ESPack);
		persoData.put("DevID", perso_DevID);
		persoData.put("Cfg_OtpInterval", timeInterval);
		persoData.put("Cfg_OtpLength", otpLength);
		String PersoData_JSON = gson.toJson(persoData);
		logger.info("[" + sessionId + "][generatePersoData] - #Generate: PersoData_JSON:[" + PersoData_JSON + "]");

		HashMap<String, Object> persoFile = new HashMap<String, Object>();
		persoFile.put("PersoData", PersoData_JSON);
		persoFile.put("License", "iDGate-2019-0218");
		persoFile.put("Version", 20190218);

		String PersoFile_JSON = gson.toJson(persoFile).replace("\\", "").replace(":\"{", ":{").replace("}\"", "}");
		logger.info("[" + sessionId + "][generatePersoData] - #Generate PersoFile:[" + PersoFile_JSON + "]");

		byte[] PersoKy;
		try {
			PersoKy = generatePersoKey(masterSec, devData, sessionId);
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate PersoKey failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		}

		String EncPersoFile;
		try {
			EncPersoFile = Encode.byteToHex(AES.drvRfc2898_encrypt(PersoKy, PersoFile_JSON.getBytes("UTF-8"), sessionId));
		} catch (UnsupportedEncodingException e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate EncPersoFile failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		} catch (Exception e) {
			logger.error("[" + sessionId + "][generatePersoData] - #Generate EncPersoFile failed:" + e.getMessage()
					+ ", stacktrace: " + gson.toJson(e.getStackTrace()));
			return new String[] { "" };
		}

		return new String[] { esn, MercuryAB, perso_DevID, EncPersoFile };
	}

	public byte[] generateOCRAData(long timeStep, String challenge, String txnData, String sessionId)
			throws UnsupportedEncodingException {

		byte[] input_OCRASuite = "OCRA-2:HOTP-SHA1-6:QA8-T1M".getBytes("UTF-8"); // 23 Bytes
		byte[] input_Partition = Encode.hexToByte("00"); // 1 Byte
		byte[] input_Challenge = Encode.hexToByte(challenge); // 8-Bytes
		byte[] input_TxnData = Encode.hexToByte(txnData); // 60-Bytes
		byte[] input_HexTimeStep = Encode
				.hexToByte(String.format("%1$" + 16 + "s", Long.toHexString(timeStep).toUpperCase(Locale.getDefault()))
						.replace(' ', '0')); // 8-Bytes

		// Combine all 5 streams to an OCRA-DataInput.
		byte[] combined = new byte[input_OCRASuite.length + input_Partition.length + input_Challenge.length
				+ input_TxnData.length + input_HexTimeStep.length];

		System.arraycopy(input_OCRASuite, 0, combined, 0, input_OCRASuite.length);
		System.arraycopy(input_Partition, 0, combined, input_OCRASuite.length, input_Partition.length);
		System.arraycopy(input_Challenge, 0, combined, input_OCRASuite.length + input_Partition.length,
				input_Challenge.length);
		System.arraycopy(input_TxnData, 0, combined,
				input_OCRASuite.length + input_Partition.length + input_Challenge.length, input_TxnData.length);
		System.arraycopy(input_HexTimeStep, 0, combined,
				input_OCRASuite.length + input_Partition.length + input_Challenge.length + input_TxnData.length,
				input_HexTimeStep.length);

//		logger.debug("[" + sessionId + "][generateOCRAData] - #timeStep:(" + timeStep + ") - #combined OCRA-Data:("
//				+ Encode.byteToHex(combined) + ").");

		return combined;
	}

	public String generateOTP(byte[] hmacSHA1) {

		// Catch the tail
		String hex_hmacSHA1 = Encode.byteToHex(hmacSHA1); // 40 Hex, 20 Bytes
		String hex_hmacSHA1_tail = hex_hmacSHA1.substring(hex_hmacSHA1.length() - 1);
		int int_hmacSHA1_tail = Integer.parseInt(hex_hmacSHA1_tail, 16);

		// Extract the 8-Hex (32bit Int).
		String otpstr = hex_hmacSHA1.substring(int_hmacSHA1_tail * 2, int_hmacSHA1_tail * 2 + 8);
		Long otp = Long.valueOf(Long.parseLong(otpstr, 16) & 0x7FFFFFFF);
		Long trimOtp = otp.longValue() % 10000000000L;

		return trimOtp.toString();
	}

	public String truncator(String otpStr, int length) {
		String result = null;

		if (otpStr.length() >= length) {
			result = otpStr.substring(otpStr.length() - length, otpStr.length());
		} else if (otpStr.length() < length) {
			result = otpStr;

			for (int i = 0; i < length - otpStr.length(); i++) {
				result = "0" + result;
			}
		}

		return result;
	}

	public String decrypt_AES(String enTxt, String kAlias, String sessionId) {

		logger.info("inside decrypt_AES");

//		if (logger.isEnabledFor(Level.DEBUG)) {
//		}
		logger.info("[" + sessionId + "][Version:][decrypt_AES] -> #cipherTxt:["
				+ enTxt + "] - #keyAlias:[" + kAlias + "] - #HSMProvider:[" + HSMProvider + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String plainTxt;

		switch (HSMProvider) {
			case "ECSP":
				/*
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				HashMap<String, Object> data = new HashMap<String, Object>();
				HashMap<String, Object> secLayer = new HashMap<String, Object>();
				HashMap<String, String> trdLayer = new HashMap<String, String>();

				data.put("msgNo", SysCode + "_" + String.valueOf(ts.getTime()) + "_"
						+ sessionId.substring(0, 4));
				data.put("txnTime", sdf.format(ts));
				data.put("txnCode", TxnCode);
				data.put("senderCode", SenderCode);
				data.put("receiverCode", ReceiverCode);
				data.put("operatorCode", OperatorCode);
				data.put("unitCode", UnitCode);
				data.put("authorizerCode", AuthorizerCode);

				trdLayer.put("data", enTxt);
				secLayer.put("keyIndex", kAlias);
				secLayer.put("cipherDatasets", trdLayer);
				data.put("requestBody", secLayer);

				logger.info("[" + sessionId + "][Version:][decrypt_AES] - # Decrypting msg (AES with key index: " + kAlias + ")");
//			if (logger.isEnabledFor(Level.DEBUG)) {
//			}
//				logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
//						+ "][decrypt_AES] - # Sending msg: " + gson.toJson(data));

				try {
					String remoteRsp = new Send2Remote().post(DecryptUrl, gson.toJson(data));
//				if (logger.isEnabledFor(Level.DEBUG)) {
//				}
//					logger.debug("[" + sessionId + "][Version:" + TS_MainFunc.SvVersion
//							+ "][decrypt_AES] - #ECSP response:[" + remoteRsp + "]");

					if (remoteRsp.equals("")) {
						logger.error(
								"[" + sessionId + "[decrypt_AES] - # Decryption (ECSP-AES) failed with empty result.");
						return null;
					}

					JSONObject json = new JSONObject(remoteRsp);

					if (!(ReceiverCode + "_0000").equals(json.getString("resultCode"))) {
						logger.info("[" + sessionId + "[decrypt_AES] - # Decryption (ECSP-AES) failed with error: ["
								+ json.getString("resultCode") + "]");
						return null;
					}

					plainTxt = json.getJSONObject("resultBody").getJSONObject("secDatasets").getString("data");

					int trimDummy = plainTxt.indexOf("00");
					if (trimDummy > -1) {
						// found dummy, trim it
						logger.debug("[" + sessionId + "[decrypt_AES] Spotted dummy in text: " + plainTxt);

						plainTxt = plainTxt.substring(0, trimDummy);

						logger.debug("[" + sessionId + "[decrypt_AES] Trimmed text: " + plainTxt);
					}

					plainTxt = new String(Encode.hexToByte(plainTxt), "UTF-8");

				} catch (JSONException e) {
					logger.error("[" + sessionId + "[decrypt_AES] - # Unable to parse ECSP response:" + e.getMessage());
					return null;
				} catch (IOException e) {
					logger.error("[" + sessionId + "[decrypt_AES] - # Connecting to ECSP error:" + e.getMessage());
					return null;
				}

				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "ECSP-AES decrypted.");
				rspData.put("Data", plainTxt);


				 */
				break;

			case "SafeNet":
				/*
				try {
					int slotId = Integer.parseInt(HSM_SLOT);
					String slotPwd = HSM_PXD;

					com.HSM.SafeNet.AES myAES = com.HSM.SafeNet.AES.initSafeNetAES(slotId, slotPwd, sessionId);
					plainTxt = myAES.decrypt_UTF8(enTxt, kAlias, sessionId);
					rspData.put("ReturnCode", ReturnCode.Success);
					rspData.put("ReturnMsg", "SafeNet-AES decrypted.");
					rspData.put("Data", plainTxt);
				} catch (Exception e) {
					rspData.put("ReturnCode", ReturnCode.Fail);
					rspData.put("ReturnMsg", e.getMessage());
					rspData.put("Data", "--");
				}

				 */
				break;

			case "Cvn":

				try {
					byte[] ci_bin = Encode.hexToByte(enTxt);
					byte[] k_bin = kAlias.getBytes("UTF-8");
					byte[] plain_bin = AES.drvRfc2898_decrypt(k_bin, ci_bin, sessionId);

					String retData = new String(plain_bin, "UTF-8");
					int trimIndex = retData.indexOf("00");
					if (trimIndex > -1) {
						retData = retData.substring(0, trimIndex);
					}

					rspData.put("ReturnCode", ReturnCode.Success);
					rspData.put("ReturnMsg", "Cvn-AES decrypted.");
					rspData.put("Data", retData);

				} catch (Exception e) {
					rspData.put("ReturnCode", ReturnCode.Fail);
					rspData.put("ReturnMsg", e.getMessage());
					rspData.put("Data", "--");
				}
				break;

			default:
				rspData.put("ReturnCode", ReturnCode.ParameterError);
				rspData.put("ReturnMsg", "Invalid provider.");
				rspData.put("Data", "--");
				break;
		}

		String rsp = gson.toJson(rspData).replace("\\u003d", "=");
		return rsp;

	}

	public String encrypt_AES(String plainTxt, String kAlias, String sessionId) {

		logger.debug("[" + sessionId + "][Version:][Version:][encrypt_AES] -> #plainTxt:[" + plainTxt + "] - #keyAlias:[" + kAlias
				+ "] - #HSMProvider:[" + HSMProvider + "]");

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		String encTxt;
		int dummyNeeded;

		switch (HSMProvider) {

			case "ECSP":
				/*
				try {
					plainTxt = Encode.byteToHex(plainTxt.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					logger.error("[" + sessionId + "[encrypt_AES] - # Data convert error:" + e.getMessage());
					return null;
				}

				dummyNeeded = plainTxt.length() % 32;

				if (dummyNeeded != 0) {
					for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
						plainTxt += "00";
					}
				}

				Timestamp ts = new Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				HashMap<String, Object> data = new HashMap<String, Object>();
				HashMap<String, Object> secLayer = new HashMap<String, Object>();
				HashMap<String, String> trdLayer = new HashMap<String, String>();

				data.put("msgNo", SysCode + "_" + String.valueOf(ts.getTime()) + "_"
						+ sessionId.substring(0, 4));
				data.put("txnTime", sdf.format(ts));
				data.put("txnCode", TxnCode);
				data.put("senderCode", SenderCode);
				data.put("receiverCode", ReceiverCode);
				data.put("operatorCode", OperatorCode);
				data.put("unitCode", UnitCode);
				data.put("authorizerCode", AuthorizerCode);

				trdLayer.put("data", plainTxt);
				secLayer.put("keyIndex", kAlias);
				secLayer.put("secDatasets", trdLayer);
				data.put("requestBody", secLayer);

				logger.info("[" + sessionId + "][Version:][encrypt_AES] - # Generate xEsnSrc (AES with key index: " + kAlias + ")");

//			if (logger.isEnabledFor(Level.DEBUG)) {
//			}
				logger.debug("[" + sessionId + "][Version:][encrypt_AES] - # Sending msg: " + gson.toJson(data));

				String enTxt = null;
				try {
					String remoteRsp = new Send2Remote().post(EncryptUrl, gson.toJson(data));
//				if (logger.isEnabledFor(Level.DEBUG)) {
//				}
					logger.debug("[" + sessionId + "][Version:][encrypt_AES] - #ECSP response:[" + remoteRsp + "]");

					if (remoteRsp.equals("")) {
						logger.error(
								"[" + sessionId + "[encrypt_AES] - # Encryption (ECSP-AES) failed with empty result.");
						return null;
					}

					JSONObject json = new JSONObject(remoteRsp);

					if (!(ReceiverCode + "_0000").equals(json.getString("resultCode"))) {
						logger.info("[" + sessionId + "[encrypt_AES] - # Encryption (ECSP-AES) failed with error: ["
								+ json.getString("resultCode") + "]");
						return null;
					}

					enTxt = json.getJSONObject("resultBody").getJSONObject("cipherDatasets").getString("data");

				} catch (JSONException e) {
					logger.error("[" + sessionId + "[encrypt_AES] - # Unable to parse ECSP response:" + e.getMessage());
					return null;
				} catch (IOException e) {
					logger.error("[" + sessionId + "[encrypt_AES] - # Connecting to ECSP error:" + e.getMessage());
					return null;
				}

				rspData.put("ReturnCode", ReturnCode.Success);
				rspData.put("ReturnMsg", "ECSP-AES encrypted.");
				rspData.put("EncData", enTxt);


				 */
				break;

			case "SafeNet":
				/*
				int slotId = Integer.parseInt(HSM_SLOT);
				String slotPwd = HSM_PXD;

				com.HSM.SafeNet.AES myAES = com.HSM.SafeNet.AES.initSafeNetAES(slotId, slotPwd, sessionId);
				encTxt = myAES.encrypt_Hex(plainTxt, kAlias, sessionId);

				if (encTxt.contains("ERROR:")) {
					rspData.put("ReturnCode", ReturnCode.Fail);
					rspData.put("ReturnMsg", encTxt);
					rspData.put("EncData", "--");
				} else {
					rspData.put("ReturnCode", ReturnCode.Success);
					rspData.put("ReturnMsg", "SafeNet-AES encrypted.");
					rspData.put("EncData", encTxt);
				}

				 */

				break;

			case "Cvn":
				try {
					String pBin = Encode.byteToHex(plainTxt.getBytes("UTF-8"));
					dummyNeeded = pBin.length() % 32;

					if (dummyNeeded > 0) {
						for (int i = 32 - dummyNeeded; i > 0; i -= 2) {
							pBin += "00";
						}
					}

					byte[] plain_bin = pBin.getBytes("UTF-8");
					byte[] kBin = kAlias.getBytes("UTF-8");
					byte[] ci_bin = AES.drvRfc2898_encrypt(kBin, plain_bin, sessionId);
					rspData.put("ReturnCode", ReturnCode.Success);
					rspData.put("ReturnMsg", "Cvn-AES encrypted.");
					rspData.put("EncData", Encode.byteToHex(ci_bin));

				} catch (Exception e) {
					rspData.put("ReturnCode", ReturnCode.Fail);
					rspData.put("ReturnMsg", e.getMessage());
					rspData.put("EncData", "--");
				}

				break;

			default:
				rspData.put("ReturnCode", ReturnCode.ParameterError);
				rspData.put("ReturnMsg", "Invalid provider.");
				rspData.put("EncData", "--");
				break;
		}

		String rsp = gson.toJson(rspData).replace("\\u003d", "=");
		return rsp;

	}

}
