package com.toppanidgate.idenkey.PubkeyStore.model;

import java.sql.SQLException;

public interface PubkeyStoreDAO_interface {

	public void insert(PubkeyStoreVO PubkeyStoreVO) throws SQLException;

	public void update(PubkeyStoreVO PubkeyStoreVO) throws SQLException;

	public PubkeyStoreVO findByIDgateID(long iDGateID) throws SQLException;

	public void removeOneRowByIDgateID(String iDGateID) throws SQLException;

}