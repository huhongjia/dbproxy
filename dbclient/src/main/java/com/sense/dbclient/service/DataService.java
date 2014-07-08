package com.sense.dbclient.service;

import com.sense.dbclient.api.DbProxy.DBProxyService.BlockingInterface;
import com.sense.dbclient.api.DbProxy.DBProxyService.Interface;
import com.sense.dbclient.api.DbProxy.User;


public interface DataService {

	void setDataServiceSvc(Interface service);

	void setDataServiceSvc(BlockingInterface blockingService);
	
	boolean createUser(User user);
	User getUser(int id);
	boolean setUser(User user);
	boolean deleteUser(int id);
}
