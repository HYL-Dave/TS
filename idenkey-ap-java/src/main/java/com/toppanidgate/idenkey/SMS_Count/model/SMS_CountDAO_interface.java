package com.toppanidgate.idenkey.SMS_Count.model;

import java.sql.SQLException;

public interface SMS_CountDAO_interface {
	
	public long create_Record(long idgateID, String operation) throws SQLException;
	
	public SMS_CountVO find_Active_Record(long idgateID, String operation) throws SQLException;
	
	public SMS_CountVO find_Lastest_5MIN_Success_Record(long idgateID) throws SQLException;
	
	public SMS_CountVO get_One(long idgateID) throws SQLException;
	
	public void update_Counter(long id, int count) throws SQLException;
	
	public void update_Status(long id, String status) throws SQLException;
}
