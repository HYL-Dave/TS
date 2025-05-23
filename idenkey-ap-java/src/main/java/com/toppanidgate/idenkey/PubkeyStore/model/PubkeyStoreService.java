package com.toppanidgate.idenkey.PubkeyStore.model;

import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.naming.NamingException;

public class PubkeyStoreService {

	private PubkeyStoreDAO_interface dao;

	public PubkeyStoreService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new PubkeyStoreDAO(jndi, sessID);
	}

	public void addPubkeyStore(long idgateID, String username, String deviceData, String idgateIDPubKey, String idgateIDECCPubKey)
			throws SQLException {

		PubkeyStoreVO pubkeyStoreVO = new PubkeyStoreVO();

		pubkeyStoreVO.setiDGateID(idgateID);
		pubkeyStoreVO.setPub_key(idgateIDPubKey);
		pubkeyStoreVO.setPub_key_ECC(idgateIDECCPubKey);
		pubkeyStoreVO.setAlias(username); // Step3 用到 svfGetAuthRequest
//		pubkeyStoreVO.setAlias(userID); // 測試用
		pubkeyStoreVO.setDevice_data(deviceData);

		dao.insert(pubkeyStoreVO);
	}

	public void updateMember(PubkeyStoreVO memVO) throws SQLException {
		dao.update(memVO);
	}

	public PubkeyStoreVO getByIdgateID(long iDGateID) throws SQLException {
		return dao.findByIDgateID(iDGateID);
	}

	public void removeOneRow(String iDGateID) throws SQLException {
		dao.removeOneRowByIDgateID(iDGateID);
	}
}