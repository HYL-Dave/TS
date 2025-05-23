package com.Members.model;

import java.sql.SQLException;

public class MembersService {

	private MembersDAO_interface dao;

	public MembersService(final String jndi, final String sessID) {
		dao = new MembersDAO(jndi, sessID);
	}

	public long addMembers(Long idgate_ID, String device_ID, String customer_Name, String email, String mobile_Phone,
			String verify_Type, String channel_Code, String customer_Status, String lang) throws SQLException {

		MembersVO membersVO = new MembersVO();

		membersVO.setIDGateID(idgate_ID);
		membersVO.setDevice_ID(device_ID);
		membersVO.setCustomer_Name(customer_Name);
		membersVO.setVerify_Type(verify_Type);
		membersVO.setEmail(email);
		membersVO.setMobile_Phone(mobile_Phone);
		membersVO.setChannel_Code(channel_Code);
		membersVO.setCustomer_Status(customer_Status);
		membersVO.setPref_Lang(lang);

		return dao.insert(membersVO);
	}

	public void updateMember(MembersVO memVO) throws SQLException {
		dao.update(memVO);
	}

	public void updateMemberDeviceID(long idgateID, String deviceID) throws SQLException {
		dao.updateDeviceID(idgateID, deviceID);
	}

	public MembersVO getByIdgateID(long idgateID) throws SQLException {
		return dao.findByCustomerID(idgateID);
	}

	public MembersVO getCustomerIDByBankID(String id) throws SQLException {
		return dao.findCustomerIDByBankID(id);
	}

	public MembersVO getByCustomerAccount(String customer_ID, String signType) throws SQLException {
		return dao.findByCustomerAccount(customer_ID, signType);
	}

	public int getCustomerAccountCount(String customer_Account) throws SQLException {
		return dao.findCustomerAccountCount(customer_Account);
	}

	public void updateLang(int idgateID, String lang) throws SQLException {
		dao.updateMemberLang(idgateID, lang);
	}

	public void addMemberStatusLog(long idgate_ID, String prevStatus, String newStatus, String reason)
			throws SQLException {
		dao.addMemberStatusLog(idgate_ID, prevStatus, newStatus, reason);
	}

	public void disableMemberUnderBankID(String bankID) throws SQLException {
		dao.disableMemberUnderBankID(bankID);
	}

	public void disableOneMemberID(long idgateID) throws SQLException {
		dao.disableOneMemberID(idgateID);
	}

	public void updateTxnAuthFails(long customer_ID, int count) throws SQLException {
		dao.updateTxnAuthFails(customer_ID, count);
	}

	public void updateLoginAuthFails(long customer_ID, int count) throws SQLException {
		dao.updateLoginAuthFails(customer_ID, count);
	}

	public void updateOfflineAuthFails(long customer_ID, int count) throws SQLException {
		dao.updateOfflineAuthFails(customer_ID, count);
	}

	public void updateMsgCounter(long customer_ID, long count) throws SQLException {
		dao.updateMsgCounter(customer_ID, count);
	}

	public void updateMemberStatusUnderBankID(final String deviceID, final String status) throws SQLException {
		dao.updateMemberUnderBankID(deviceID, status);
	}
}