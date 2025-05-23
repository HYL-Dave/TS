package com.Members.model;

import java.sql.SQLException;

public interface MembersDAO_interface {

	public long insert(MembersVO membersVO) throws SQLException;

	public void update(MembersVO membersVO) throws SQLException;
	
	public void updateDeviceID(long customer_ID, String deviceID) throws SQLException;

	public MembersVO findByCustomerID(long customer_ID) throws SQLException;

	public MembersVO findCustomerIDByBankID(String customer_Account) throws SQLException;

	public MembersVO findByCustomerAccount(String customer_Account, String signType) throws SQLException;

	public int findCustomerAccountCount(String customer_Account) throws SQLException;
	
	public void updateMemberLang(long idgate_ID, String lang) throws SQLException;
	
	public void addMemberStatusLog(long idgate_ID, String prevStatus, String newStatus, String reason) throws SQLException;
	
	public void disableMemberUnderBankID(String bank_ID) throws SQLException;
	
	public void updateMemberUnderBankID(final String deviceID, final String status) throws SQLException;
	
	public void disableOneMemberID(long idgate_ID) throws SQLException;
		
	public void updateTxnAuthFails(long customer_ID, int counter) throws SQLException;
	
	public void updateLoginAuthFails(long customer_ID, int counter) throws SQLException;
	
	public void updateOfflineAuthFails(long customer_ID, int counter) throws SQLException;
	
	public void updateMsgCounter(long customer_ID, long counter) throws SQLException;
}