package com.sense.dbclient.client;

import org.junit.Test;

import com.sense.dbclient.api.DbProxy.User;
import com.sense.dbclient.service.DataService;
import com.sense.dbclient.service.DataServiceFactory;

import static org.junit.Assert.*;

public class TestDataService {
	
	
	@Test
	public void TestAddUser() throws InstantiationException, IllegalAccessException{
		User user = User.newBuilder().setName("jiangtao").setPassword("21213").build();
		DataService dataService = DataServiceFactory.getDataService();
		assertTrue(dataService.createUser(user));
	}

}
