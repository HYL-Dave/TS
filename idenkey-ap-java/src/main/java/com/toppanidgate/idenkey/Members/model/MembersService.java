package com.toppanidgate.idenkey.Members.model;

import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.naming.NamingException;

public class MembersService {

	private MembersDAO_interface dao;

	public MembersService(final String jndi, final String sessID) throws UnknownHostException, NamingException {
		dao = new MembersDAO(jndi, sessID);
	}

	public long addMembers(String bank_ID, String customer_Name, String email, String mobile_Phone, String password,
			String channel_Code, String customer_Status, String lang) throws SQLException {

		MembersVO membersVO = new MembersVO();

		membersVO.setBank_ID(bank_ID);
		membersVO.setCustomer_Name(customer_Name);
		membersVO.setAccount(email);
		membersVO.setMobile_Name(mobile_Phone);
		membersVO.setPcode(password);
		membersVO.setChannel_Code(channel_Code);
		membersVO.setCustomer_Status(customer_Status);
		membersVO.setPref_Lang(lang);

		return dao.insert(membersVO);
	}

	public void updateMember(MembersVO memVO) throws SQLException {
		dao.update(memVO);
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

	public void updateDigitalFailCounter(long customer_ID, int count) throws SQLException {
		dao.updateDigitalFailCounter(customer_ID, count);
	}
	
	public void updatePatternFailCounter(long customer_ID, int count) throws SQLException {
		dao.updatePatternFailCounter(customer_ID, count);
	}

	public void updateAuthFailCounter(long customer_ID, int count) throws SQLException {
		dao.updateAuthFailCounter(customer_ID, count);
	}
	
	public void updateMsgCounter(long customer_ID, long count) throws SQLException {
		dao.updateMsgCounter(customer_ID, count);
	}
}