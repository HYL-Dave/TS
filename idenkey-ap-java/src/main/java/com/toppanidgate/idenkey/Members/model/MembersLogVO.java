package com.toppanidgate.idenkey.Members.model;

public class MembersLogVO {
	private long iDGateID;
	
	private String Previous_Status;

	public long getiDGateID() {
		return iDGateID;
	}

	public void setiDGateID(long iDGateID) {
		this.iDGateID = iDGateID;
	}

	public String getPrevious_Status() {
		return Previous_Status;
	}

	public void setPrevious_Status(String previous_Status) {
		Previous_Status = previous_Status;
	}
}