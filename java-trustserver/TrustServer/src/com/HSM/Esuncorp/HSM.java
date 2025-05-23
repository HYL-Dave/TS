package com.HSM.Esuncorp;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.Cvn.Config.Cfg;
import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;
import com.toppanidgate.idenkey.common.model.ReturnCode;

public class HSM {
	private static final Gson gson = new Gson();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String sessionId = Base64.getEncoder().encodeToString(new SecureRandom().generateSeed(16));

	private static String SysCode;
	private static String TxnCode;
	private static String SenderCode;
	public static String ReceiverCode;

	private static String OperatorCode;
	private static String UnitCode;
	private static String AuthorizerCode;

	private static String KeyIndex;
	private static String Mode;
	private static String IvEncoding;
	private static String InitVector;
	private static String ECSP_Encrypt_URL;
	private static String ECSP_Decrypt_URL;

	@PostConstruct
	void init() {
		if ("HSM".equals(Cfg.getExternalCfgValue("HSMProvider"))) {
			SysCode = Cfg.getExternalCfgValue("SysCode") == null ? "UP0259" : Cfg.getExternalCfgValue("SysCode");
			TxnCode = Cfg.getExternalCfgValue("TxnCode") == null ? "FIDO" : Cfg.getExternalCfgValue("TxnCode");
			SenderCode = Cfg.getExternalCfgValue("SenderCode") == null ? "UP0259" : Cfg.getExternalCfgValue("SenderCode");
			ReceiverCode = Cfg.getExternalCfgValue("ReceiverCode") == null ? "UP0117"
					: Cfg.getExternalCfgValue("ReceiverCode");
			KeyIndex = Cfg.getExternalCfgValue("KeyIndex") == null ? "2023" : Cfg.getExternalCfgValue("KeyIndex");
			Mode = Cfg.getExternalCfgValue("Mode") == null ? "03" : Cfg.getExternalCfgValue("Mode");
			IvEncoding = Cfg.getExternalCfgValue("IvEncoding") == null ? "04" : Cfg.getExternalCfgValue("IvEncoding");
			InitVector = Cfg.getExternalCfgValue("InitVector") == null ? "0123456789123456"
					: Cfg.getExternalCfgValue("InitVector");
			ECSP_Encrypt_URL = Cfg.getExternalCfgValue("ECSP_Encrypt_URL") == null
					? "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/encryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169"
							: Cfg.getExternalCfgValue("ECSP_Encrypt_URL");
			ECSP_Decrypt_URL = Cfg.getExternalCfgValue("ECSP_Decrypt_URL") == null
					? "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/decryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169"
							: Cfg.getExternalCfgValue("ECSP_Decrypt_URL");
			
			if (Cfg.getExternalCfgValue("SysCode") == null) {
				Log4j.log.debug("USE default SysCode:" + "UP0259");
			}
			if (Cfg.getExternalCfgValue("TxnCode") == null) {
				Log4j.log.debug("USE default TxnCode:" + "FIDO");
			}
			if (Cfg.getExternalCfgValue("SenderCode") == null) {
				Log4j.log.debug("USE default SenderCode:" + "UP0259");
			}
			if (Cfg.getExternalCfgValue("ReceiverCode") == null) {
				Log4j.log.debug("USE default ReceiverCode:" + "UP0117");
			}
			if (Cfg.getExternalCfgValue("KeyIndex") == null) {
				Log4j.log.debug("USE default KeyIndex:" + "2023");
			}
			if (Cfg.getExternalCfgValue("Mode") == null) {
				Log4j.log.debug("USE default Mode:" + "03");
			}
			if (Cfg.getExternalCfgValue("IvEncoding") == null) {
				Log4j.log.debug("USE default IvEncoding:" + "04");
			}
			if (Cfg.getExternalCfgValue("InitVector") == null) {
				Log4j.log.debug("USE default InitVector:" + "0123456789123456");
			}
			if (Cfg.getExternalCfgValue("ECSP_Encrypt_URL") == null) {
				Log4j.log.debug("USE default Encrypt_URL:" + "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/encryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169");
			}
			if (Cfg.getExternalCfgValue("ECSP_Decrypt_URL") == null) {
				Log4j.log.debug("USE default Decrypt_URL:" + "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/decryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169");
			}
			
			Log4j.log.debug("#------------ HSMProvider is HSM ----------------#");
			Log4j.log.debug("FINAL SysCode:" + SysCode);
			Log4j.log.debug("FINAL TxnCode:" + TxnCode);
			Log4j.log.debug("FINAL SenderCode:" + SenderCode);
			Log4j.log.debug("FINAL ReceiverCode:" + ReceiverCode);
			Log4j.log.debug("FINAL KeyIndex:" + KeyIndex);
			Log4j.log.debug("FINAL Mode:" + Mode);
			Log4j.log.debug("FINAL IvEncoding:" + IvEncoding);
			Log4j.log.debug("FINAL InitVector:" + InitVector);
			Log4j.log.debug("FINAL IvEncoding:" + ECSP_Encrypt_URL);
			Log4j.log.debug("FINAL InitVector:" + ECSP_Decrypt_URL);
			Log4j.log.debug("#------------------------------------------------#");
		}
	}
	
	public static void main(String[] args) {
//		// System.out.print("\nPreset parameters required. Please enter following data:");

		try (Scanner reader = new Scanner(System.in);) {
//			// System.out.print("SysCode (UP0259): ");
//			SysCode = reader.nextLine();
//			if (StringUtils.isEmpty(SysCode)) {
//				SysCode = "UP0259";
//			}
//			// System.out.print("SysCode: " + SysCode);

//			// System.out.print("TxnCode (FIDO): ");
//			TxnCode = reader.nextLine();
//			if (StringUtils.isEmpty(TxnCode)) {
//				TxnCode = "FIDO";
//			}
//			// System.out.print("TxnCode: " + TxnCode);

//			// System.out.print("SenderCode (UP0259): ");
//			SenderCode = reader.nextLine();
//			if (StringUtils.isEmpty(SenderCode)) {
//				SenderCode = "UP0259";
//			}
//			// System.out.print("SenderCode: " + SenderCode);

//			// System.out.print("ReceiverCode (UP0117): ");
//			ReceiverCode = reader.nextLine();
//			if (StringUtils.isEmpty(ReceiverCode)) {
//				ReceiverCode = "UP0117";
//			}
//			// System.out.print("ReceiverCode: " + ReceiverCode);

			// System.out.print("OperatorCode (!*): ");
			OperatorCode = reader.nextLine();
			// System.out.print("OperatorCode: " + OperatorCode);
			// System.out.print("UnitCode (!*): ");
			UnitCode = reader.nextLine();
			// System.out.print("UnitCode: " + UnitCode);
			// System.out.print("AuthorizerCode (!*): ");
			AuthorizerCode = reader.nextLine();
			// System.out.print("AuthorizerCode: " + AuthorizerCode);

//			// System.out.print("keyIndex (2023): ");
//			KeyIndex = reader.nextLine();
//			if (StringUtils.isEmpty(KeyIndex)) {
//				KeyIndex = "2023";
//			}
			// System.out.print("keyIndex: " + KeyIndex);

//			// System.out.print("mode (Default is 03): ");
//			Mode = reader.nextLine();
//			if (StringUtils.isEmpty(Mode)) {
//				Mode = "03";
//			}
			// System.out.print("mode: " + Mode);

//			// System.out.print("ivEncoding (Default is 04): ");
//			IvEncoding = reader.nextLine();
//			if (StringUtils.isEmpty(IvEncoding)) {
//				IvEncoding = "04";
//			}
			// System.out.print("ivEncoding: " + IvEncoding);

//			// System.out.print("initVector (Default is 16 bytes): ");
//			InitVector = reader.nextLine();
//			if (StringUtils.isEmpty(InitVector)) {
//				InitVector = "0123456789123456";
//			}
			// System.out.print("initVector: " + InitVector);
//			// System.out.print("initVector(32): " + Hex.encodeHexString(InitVector.getBytes(StandardCharsets.UTF_8)));

//			// System.out.print(
//					"ECSP_Encrypt_URL (https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/encryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169): ");
//			ECSP_Encrypt_URL = reader.nextLine();
//			if (StringUtils.isEmpty(ECSP_Encrypt_URL)) {
//				ECSP_Encrypt_URL = "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/encryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169";
//			}
			// System.out.print("ECSP_Encrypt_URL: " + ECSP_Encrypt_URL);

//			// System.out.print(
//					"ECSP_Decrypt_URL (https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/decryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169): ");
//			ECSP_Decrypt_URL = reader.nextLine();
//			if (StringUtils.isEmpty(ECSP_Decrypt_URL)) {
//				ECSP_Decrypt_URL = "https://api-esipt.testesunbank.com.tw/esunbankt/bankapi/encryption/v1/tw/up0117/decryptSecretData?client_id=49a12038-d805-4d45-8b01-13d84433c169";
//			}
			// System.out.print("ECSP_Decrypt_URL: " + ECSP_Decrypt_URL);

			// System.out.print("Parameters are set. Show as following:");
			// System.out.print("SysCode: " + SysCode);
			// System.out.print("TxnCode: " + TxnCode);
			// System.out.print("SenderCode: " + SenderCode);
			// System.out.print("ReceiverCode: " + ReceiverCode);
			// System.out.print("OperatorCode: " + OperatorCode);
			// System.out.print("UnitCode: " + UnitCode);
			// System.out.print("AuthorizerCode: " + AuthorizerCode);
			// System.out.print("KeyIndex: " + KeyIndex);
			// System.out.print("Mode: " + Mode);
			// System.out.print("IvEncoding: " + IvEncoding);
			// System.out.print("InitVector: " + InitVector);
			// System.out.print("ECSP_Encrypt_URL: " + ECSP_Encrypt_URL);
			// System.out.print("ECSP_Decrypt_URL: " + ECSP_Decrypt_URL + "\n");

			String opt = "";
			do {
				// System.out.print("Encrypt an text, enter \"E\"");
				// System.out.print("Decrypt an encrypted text, enter \"D\"");
				// System.out.print("Enter \"Q\" to quit");
				// System.out.print("Option: ");
				opt = reader.nextLine();

				if ("E".equals(opt)) {
					System.out.print("Enter the text that you want to encrypt: ");
					encrypt(reader.nextLine(), sessionId);
				} else if ("D".equals(opt)) {
					System.out.print("Enter the text that you want to decrypt: ");
					decrypt(reader.nextLine(), sessionId);

				} else if (!"Q".equals(opt)) {
					System.out.print("Unknown option.");
				}

			} while (!opt.equals("Q"));

			// System.out.print("Press any key to quit...");
			reader.nextLine();
			System.exit(0);
		}
	}

	public static HashMap<String, Object> encrypt(String plainTxt, String sessionId) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());

		HashMap<String, Object> data = new HashMap<String, Object>();
		HashMap<String, Object> secLayer = new HashMap<String, Object>();
		HashMap<String, String> trdLayer = new HashMap<String, String>();

		data.put("msgNo", SysCode + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_"
				+ sessionId.substring(0, 3));// 用session
//		data.put("msgNo", SysCode + "_" + String.valueOf(ts.getTime()) + "_" + sessionId.substring(0, 3));// 用session
		// id只是增加亂數值
		data.put("txnTime", sdf.format(ts));
		data.put("txnCode", TxnCode);
		data.put("senderCode", SenderCode);
		data.put("receiverCode", ReceiverCode);
		data.put("operatorCode", OperatorCode);
		data.put("unitCode", UnitCode);
		data.put("authorizerCode", AuthorizerCode);

		trdLayer.put("data", plainTxt);
		secLayer.put("keyIndex", KeyIndex);
//		secLayer.put("mode", "03");
//		secLayer.put("initVector", "0123456789123456");
		secLayer.put("mode", Mode);
		secLayer.put("initVector", InitVector);
		secLayer.put("ivEncoding", IvEncoding);
		secLayer.put("secDatasets", trdLayer);
		data.put("requestBody", secLayer);
//		Log4j.log.debug("*** encrypt toJson:" + gson.toJson(data));

		JSONObject json = null;
		HashMap<String, Object> rspData = new HashMap<String, Object>();
		// return new Send2Remote().post(ECSP_Encrypt_URL, gson.toJson(data));
		try {
			String rsp = new Send2Remote().post(ECSP_Encrypt_URL, gson.toJson(data));

//			Log4j.log.debug("*** encrypt rsp:" + rsp);

			json = new JSONObject(rsp);

//			Log4j.log.debug("*** HSM encrypt json:" + json);

			if (!(HSM.ReceiverCode + "_0000").equals(json.getString("resultCode"))) {
				Log4j.log.error("[encrypt_HSM] Encryption (ECSP-AES) failed with error: ["
						+ json.getString("resultCode") + "]");
				rspData.put("ReturnCode", ReturnCode.Fail);
				if (json != null)
					rspData.put("ReturnMsg", "[encrypt_HSM] Encryption (ECSP-AES) failed with error: ["
							+ json.getString("resultCode") + "]");
				rspData.put("EncData", "--");
			}
//			Log4j.log.debug("*** HSM encrypt Result:" + new Gson().toJson(json));
			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "Success");
			rspData.put("EncData", json.getJSONObject("resultBody").getJSONObject("cipherDatasets").getString("data"));
//			Log4j.log.debug("*** HSM encrypt EncData:"
//					+ json.getJSONObject("resultBody").getJSONObject("cipherDatasets").getString("data"));

		} catch (JSONException e) {
			Log4j.log.error("[encrypt_HSM] Unable to parse ECSP response:" + e.getMessage());
			rspData.put("ReturnCode", ReturnCode.Fail);
			if (json != null)
				rspData.put("ReturnMsg", json.getJSONObject("resultBody").getJSONObject("resultDescription"));
			rspData.put("EncData", "--");
		} catch (IOException e) {
			Log4j.log.error("[encrypt_HSM] Connecting to ECSP error:" + e.getMessage());
			rspData.put("ReturnCode", ReturnCode.Fail);
			rspData.put("ReturnMsg", e.getMessage());
			rspData.put("EncData", "--");
		} catch (Exception e) {
			Log4j.log.error("[encrypt_HSM] HSM Exception::" + e.getMessage());
			rspData.put("ReturnCode", ReturnCode.Fail);
			rspData.put("ReturnMsg", e.getMessage());
			rspData.put("EncData", "--");
		}
		return rspData;
	}

	public static HashMap<String, Object> decrypt(String enTxt, String sessionId) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());

		HashMap<String, Object> data = new HashMap<String, Object>();
		HashMap<String, Object> secLayer = new HashMap<String, Object>();
		HashMap<String, String> trdLayer = new HashMap<String, String>();

		data.put("msgNo", SysCode + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_"
				+ sessionId.substring(0, 3));// 用session
//		data.put("msgNo", SysCode + "_" + String.valueOf(ts.getTime()) + "_" + sessionId .substring(0, 3));// 用session
		// id只是增加亂數值
		data.put("txnTime", sdf.format(ts));
		data.put("txnCode", TxnCode);
		data.put("senderCode", SenderCode);
		data.put("receiverCode", ReceiverCode);
		data.put("operatorCode", OperatorCode);
		data.put("unitCode", UnitCode);
		data.put("authorizerCode", AuthorizerCode);
		
		trdLayer.put("data", enTxt);
		secLayer.put("keyIndex", KeyIndex);
		secLayer.put("mode", Mode);
		secLayer.put("initVector", InitVector);
		secLayer.put("cipherDatasets", trdLayer);
		data.put("requestBody", secLayer);
//		Log4j.log.debug("*** decrypt toJson:" + gson.toJson(data));

		HashMap<String, Object> rspData = new HashMap<String, Object>();
		JSONObject json = null;
		//		return new Send2Remote().post(ECSP_Decrypt_URL, gson.toJson(data));
		try {
			String rsp = new Send2Remote().post(ECSP_Decrypt_URL, gson.toJson(data));
			// TODO TEST ONLY
			// 以下二行測試資料用於本機測試，以決定是 rspData.put("Data", plainTxt);
//			String rsp = "{\"resultCode\":\"UP0117_0000\",\"resultDescription\":\"成功\",\"resultBody\":{\"secDatasets\":{\"data\":\"7969C1D64BE991A76A009B3A737A468670D0EBCDD25D8E35A03FA9473C3DD5C398575A869D300A0ED9462420D33B61891EFE73563CC3F28B33B6FDE115DAFA8DD868F365507303555C82D724A0D5E880B247025D0A153E186FF50878DFA0ED1707BCF85667D5A1D8F67D9C713067FA9C83207CA058144E5750C17C6872E5F8678F285F87E2353788F08F97C81C41F2D2B326B5C23FE109C41DCF0010F4040E74D46C920D128A8428F9B5D29564FBE545113B68A428FC230D4ED83A96ED6E32E5D71FB57B0CA4DA69B21E9B6E89466BF432E70E78639CE841B09CEAE4A90FBC7E1DFD6759BEAFCEC57E0FFBB2E4383BA1DFA812A4523C433D91785F26BE7EFBE9071B619B2F8857E01313573C84359A077B438936C912AD3D155E781A1498A5D1949C100CE7AAF4A023806F93BB2C8BB04A50C3A8067900821BE1C77BEC86A48FB166D381C6BF3369011568FBC9A9CA038CE6C92684AC3909736C826EEB1DA0639AD71361F8F313219AE4F0C9D262535738D4FFB25A1CDA9F825B1B11884671E26CB8C8CB0B3CCD5D7E0F342CBA5E09887F02F2715D3E436C21613815D6F5122C3E44EC0E3D7EB5C1449446B6069FF3A39CD80A77DEB3660C14A3A3983CF84C18FD4CD77A2B1E46DC96755DE9013D05BD530F8B55CC6E78464D7C3969BBBE471E9D4D56C471B456224A0CF2C5AAF917BB7CAE910B478899B5C4E06E1E2DC81F54BCD99F1DE5759D33123570F04A68FBAC4B75835D3B303D96848A2FEF9BEDC0B5E1991D728C1D9214EF3B2769F5BC094E3165880A660AC1CD7B0A239C6999871DFBB16C3FF1291E9F79D82DF4B2B037ED0BB6907F53BE330FD9B7A3B9938944BFFC68A3A446744E9F7144798B5BB8B998157F1765BDC284ABE14441502CB477B3E2FCD60F0A67D4EFF1FF844AFAD2CEC4810F85DD04A21B0FD35CCC535AA894D65D438D3D5AE3FC26C0FA56F8D1D36CAB2ACDFB76DEE5F92D08E519EDB00CF51DB5EC8EBD7DE8EAEC7FF74A28AF405E8DBB10F6FEDE6786E4D6F8D0B424B946F7D9BB97FA1F9F097EDFA0B972D4C9E5C48BD138B1BF55A181C4FA3CA719F35B2B3E7D0121CD82203552943374F7B5EB571AFE566EDEE6C6D9BDEC652594083044400D387CE2CEDFB851F23BF03448E992DD81367048E97B4E3124DA0E58C7427F9E1E3EFC3281ACC7D77D7E5F399E3E764B9C4EDF3060B0C4E004B164909260FAA3836C2A848B5BCE2A80CF7C853C117F6059A5B13F0916FB91939DEE8C740314DE131C494BCCBAE0A06610D945751A0170ABAA5707FAD784540EA60ABADA906F6143CD37D4E63E9F0E0BE3D780CCD41CC15E5D8F19BDC8FF5E66E9D6FAC52809CDDD97F43A3B68912D23DC5A0A09671338E27040C90FFDBFF7F8C9312A13C912998F1780F149E606146962BEE62545D063029D296CCDE111E3AA9C1C2F26CC1E11874B4E63A9662471E8C6A5F3CB139D15A78038654DD7AC03D8530AB96B1FBE3D935548DA1C756047CF7CC21D0FFC38B51B8763619F274AC16D694B4EB6CE1F229A0723C08F4CD85D7012E9C44BEE02C512F556BCDC0A395DAE4993DE19883113BB75DACDEB089B80ECDF2235154194006EA35F95DEB98924B0D0382F01BDBCAC8A6C675EED72F80FE13825825B5C1979C3FF101A1B486EE7D0402A89F4CEA6DDFD524AF904BE87041A75E1A388D623FAE83967BBFC0171B66D67D1C254360841F264D22AAC412EF536B43A856A270795C09746D7B51BF04F3F09587A184F82D07D2224243E5EA2A89396C8A25D047483A85290A7DCDAB2FADCE2DBB156A8CBE535E2345B508C39901B8486591E0734D33DB5AF1DD768A394A8CD12121E9810DB65AE2D5E03F071EE7F1534BCAA517B3444032CE3A0210E1749439686A2FD735BAB60C827C70EB6556A66A10DE25A2B80B809A47AFD7D32C06D88C2409636D13B607AA5D30D50C6849DB38FF7975E0B37192959ED547CA1CD1F7C3C28DCCC16119D5D9A7D060B71CA2F1F5FDE4E2C792799DE10E3F4F21CDCD41429F5F80BB69AA8FAAC0FDD5BAFABD065388E25982E278376EA445F29BF8C8C09D231E5707C04C51A7C5EAAD8EBD88E4BB5A7C4DC6590171B8C6E332C0692E05BFB4C481065710CEC8D4758327067ADBC3D8F0188D1A8C972B7CAA4D2C0D314BA627C180E7A63CEE3FBA7EA6E74074A75FCAA3935F9657C5A1097D3C42AD3CE4D4C020CDE70D3AE871FF568A602BC3629F80DF01208FD26AE905E8FF6D2FB18C4D59D04A2D049C116445DEC0B6AF96629138987E8A01C2070046461CC572F8460DEA59192E2B96AAAE50775C6F4070DEDE9E9AD3B9749F140F1AFA9879D25B84588951B922C8754ABAA4C607D12FC54F30FED21612120C879BBFB44B54EADF567A38EA99D8EC86A15CBEFF035F6F2E9F7B991C2A3449BF021E848439C1E9E9B18106B859B21188D3B0B23D3AE86C0D24F589B0AAD5D4C0B54795D1F2C2CC174EF1958FAD00A880DE14ECF06DBDB50A227A817DDFDF566E1411CEA35526181938640973CD3A356C9521FF8812C109BFD39A0BF5C086424D629335ABAA9CD2EC50649645DC140D65F26070EB40E8CFB2D7C6D83DB2F4D077FB29F9CEB94E4DDFD86206B24503C09714535A42516677BDF4AB2E2ABC06AC5FF2C4D654C6B34410E8200F1D0B6CA717CBD9F4AA07D251F85DB8F0B02AD41930E1AAC525A0E9F44E7EB2E1EE763FE12BEECE1C4FEE69BDD99A5AF493AB5CBD6AF394D6D4AB89C4252F327CE1483C8B958C1A9580E64663AB65EE07D0953F9B6DE58B2AF28BC7843F8001A7070435D2452FF8E66F4884CA49282275E1EA633EFF38477D3EAE11EEABA587C48FBCA839CD29FACBA09F29A0B9B5C8B4D255C53F58E8890101259A85004D866CAFA5787020FB35F1DB77B3C812C26389DC9CB9577836AEF03442473E824199B7AAD295EB3C5174034DE10B05D8FCF09336876B420ED325B1FB20C1A019D3AA890628B45A3696E8FAB3BAE41027E0D31FD0115EB1EEF9BC3BDF7D4F81F6B5F6FEB3CC71085685868B67A4FB612318B85CC67383DAD105314C4C6759DF9AC69568644259A5E66E2FAC9C76E9E9BDC5F1F420603DC4C0BC10CBDA57F157B84D9A3E4029EA4E3F68FE6BAD6DE281BE592C60EAB293CBD01593FEEF2B8A12621610E66ACBC52A5268A6A2AA1C41E8E4A96AB802032BA8D48DA25D6C605CBFA6D3B7016C5663439C746F3DB2DC775D76DF03B3CE365E8CAB4C13AFCC4461E633DB3C65E77060CAF99A25F31D38F5FD0096AC720CA56A5D89E5517AEBA7078753FDB6D5C28E8A63E989B101A31E6F05A09F953D74E068C1D35B225AEB5DE2AD3B4A0D63C94F7729871F7931F6171B69AB4A58B6A46C7B030F7DF69B671C59BBFB45E601714146DEACDFD9E4D755653946C1F390E113AFAFF933F5E043E2DDFB08FFCDDA2D00BDCF63501766DA3DCFEF64699\"}}}";
//			String rsp = "{\"resultCode\":\"UP0117_0000\",\"resultDescription\":\"成功\",\"resultBody\":{\"secDatasets\":{\"data\":\"308204BE020100300D06092A864886F70D0101010500048204A8308204A402010002820101009A67C95975D35C6EB2421DA9D674CE121D9FD5B92A3CAA6336A9C66255071B7764ACAEE3A40015EC3E75408F2FBDE5327A222F54B6039476899360C75ECAEAC08AB9C7213CC1546BEBAC213D1B04E1BE7C3FD8420A7E95B2D7CCE6A9F64F5818642EC8654B8D2F52E0F5EFA0A21CE109760D7D55BD2BD93C5ED465071C2496CF1E3F6FC81904D78767C73111E26DFD4B35775936DE9E59C0B0BF42879854F8FA3CFCB5ED855CDFD850F90914DB03B9BE6C49A62EF64310ED04C8C15B78A7F30BE841766A5A3F00230D2CBED24AB180D8BC9FF4B17DD919A0134EDB6081CE7FBFA0EB5B7CC5B85DC0C69DA604360D3AC6AA361AEBDDCC6ECD4AA318B09CEECDB30203010001028201007BB9BCB36DE1EF0858D8F7F21701C3FAEA3820C75E5BFC60C1F5AB1D326B1E8C3E64CD436630176115EF58CB2403737C4E37C45B89FA366601F9D1AC107E633BE42DD7ADFF841F84D6F5383141CC58A40DE60F01646D68C1458150385341E322B06C78720AE7584B7E2E3455307219B53E1EB317181DE1D6295AE2959EEC86D6B1935F9AC78577617235FDA4E7542E95F68A04A350F6646D46E0BCB08797932281A588B20756AE87EF99D6E8F6CB3E0DB745B54B7C209CF88BF56E91CA635885DC267221CFED2225B0516C61C7D1237A8715493DD333FE467B3C8EBF8C6237E52C9141CD9AD3C8A8756C66B7C1D041EE4EA9C23FDFC247C33B25A76D961D59C902818100E14EA5574112A2CFACB354A08FB7C58AB9DDFCE81B726729D5F6C83224CF7A8DCFB6B071FE77FC5EAF04B4738F66E9D2FA400360CF49EE10760352B01D0F6E49814E6178FFDED3EB478C6D8929A46ADEB60289817AE39B7F7FFEF4DCF3FB427620E7A9FBB3EE58759FED02D72CE172C465CAF400135D50C2C18AC077B23E657702818100AF7083B4ECA23104A5B6CE8A4E7AE4542A08B6F7AF924FC48CAB56A3A44259869B07367DB230472827955DF61D0868D2A4120C0E5C6B849878C43884DF18FFCB1C316A1AAFA7D8B564ABBC5ACF1C6CE69B560E1190F7B2D9CA77234499A84D02E427C15CD142720017EE066951E22890EBA2305D864D99BF1437AE90467DD8A502818100A4B9A642DBEDAF215CE7A408DBEA853FFED26134A5D487BE5B93D91C6833552BD7607F3268D4FAAB9314428676A2CC7396C9346F143EBE1244E5FB0F8DBF99679E655AF8674257F81DD07AC91CABFC02A659C684E6FEFFD241177B990BBC7903FADCC537A47EAF28186BE576C276763D471B1237F7F072BA615C7B8947C53CD3028181009E278813E9DEACE1CBA1E8BFB5D00355FC2EAF02CA3BD3BD1432C17FF3EBFC58E418B47ECBCBC9D9E1153E074854F46A2F8E35601CE0B03FE35EC0B4C789F9CE583413BACAEF3CE50943E820D4E48327C090EB4DDAA9F1897483C23F6D65D346EFF706D16DB3C0A164F126C67B12AE0F36DC974D386D6C545D1D75388AD398210281800CA970D2909901A81856CB0499517F7EFD1ED7AE0DAC7CD27295ED2EDCD3C4D205FBF95514063F672DC906A6EB4364D0BAEE9577C0CAC15B7E1BF1FA0A03149969505AFB193BA8280510B80916B4FDDB9093C4E6FAEB6552D6C8FA5B0AD16B3C9AC9FA5D2464D01F8A56AC59BE099E23CAC0A7CD6F6C399A4759B4D9F073E4BF\"}}}";
			Log4j.log.debug("*** decrypt rsp:" + rsp);
			
			if (rsp.equals("")) {
				Log4j.log.error("[decrypt_HSM] Decryption (ECSP-AES) failed with empty result.");
				rspData.put("ReturnCode", ReturnCode.Fail);
				rspData.put("ReturnMsg", "[decrypt_HSM] Decryption (ECSP-AES) failed with empty result.");
				rspData.put("Data", "--");
				return rspData;
			}

			json = new JSONObject(rsp);
			
//			Log4j.log.debug("*** decrypt json:" + json);
//			Log4j.log.debug("*** json.getString(\"resultCode\"):" + json.getString("resultCode"));

			if (!(HSM.ReceiverCode + "_0000").equals(json.getString("resultCode"))) {
				Log4j.log.error("[decrypt_HSM] Decryption (ECSP-AES) failed with error: ["
						+ json.getString("resultCode") + "]");
				rspData.put("ReturnCode", ReturnCode.Fail);
				if (json != null)
					rspData.put("ReturnMsg", "[decrypt_HSM] Decryption (ECSP-AES) failed with error: ["
							+ json.getString("resultCode") + "]");
				rspData.put("Data", "--");
				return rspData;
			}

			String plainTxt = json.getJSONObject("resultBody").getJSONObject("secDatasets").getString("data");

			// 本機測試結果，註解以下這塊
//			int trimDummy = plainTxt.indexOf("00");
//			if (trimDummy > -1) { // found dummy, trim it
//				Log4j.log.error("[decrypt_HSM] Spotted dummy in text: " + plainTxt);
//
//				plainTxt = plainTxt.substring(0, trimDummy); // //
//				Log4j.log.debug("[decrypt_HSM] Trimmed text: " + plainTxt);
//			}

			rspData.put("ReturnCode", ReturnCode.Success);
			rspData.put("ReturnMsg", "HSM decrypted Success");
			
			rspData.put("Data", plainTxt);
//			rspData.put("Data", new String(Encode.hexToByte(plainTxt), "UTF-8"));
//			rspData.put("Data", json.getJSONObject("resultBody").getJSONObject("secDatasets").getString("data"));
//			rspData.put("Data", plainTxt);
			
//			rspData.put("Data", new Gson().toJson(json));

//			// Log4j.log.debug("Data getString(\"data\"):"
//					+ json.getJSONObject("resultBody").getJSONObject("secDatasets").getString("data"));
//			// Log4j.log.debug("Data plainTxt:" + plainTxt);
//			// Log4j.log.debug("Data new String(Encode.hexToByte(plainTxt), \"UTF-8\"):" + new String(Encode.hexToByte(plainTxt), "UTF-8"));
//			// Log4j.log.debug("decrypt Result:" + new Gson().toJson(json));
//			// Log4j.log.debug("decrypt Result:" + new String(Encode.hexToByte(plainTxt), "UTF-8"));
//			return new String(Encode.hexToByte(plainTxt), "UTF-8");

		} catch (JSONException e) {
			Log4j.log.error("[decrypt_HSM] Unable to parse ECSP response:" + e.getMessage());
			rspData.put("ReturnCode", ReturnCode.Fail);
			if (json != null)
				rspData.put("ReturnMsg", json.getJSONObject("resultBody").getJSONObject("resultDescription"));
			rspData.put("Data", "--");
//		} catch (IOException e) {
//			Log4j.log.error("[decrypt_HSM] Connecting to ECSP error:" + e.getMessage());
//			rspData.put("ReturnCode", ReturnCode.Fail);
//			if (json != null)
//				rspData.put("ReturnMsg", json.getJSONObject("resultBody").getJSONObject("resultDescription"));
//			rspData.put("Data", "--");
		} catch (Exception e) {
			Log4j.log.error("[decrypt_HSM] Exception::" + e.getMessage());
			rspData.put("ReturnCode", ReturnCode.Fail);
			rspData.put("ReturnMsg", e.getMessage());
			rspData.put("Data", "--");
		}
		return rspData;
	}
}
