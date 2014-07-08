package com.sense.dbclient.service;

import com.sense.dbclient.service.impl.DBClientFactory;
import com.sense.dbclient.service.impl.DataServiceImpl;

public class DataServiceFactory {
	/*
	 * 单例
	 */
	private DataServiceFactory() {
	}

	private static class DataServiceHolder {

		static final DataService instance = new DataServiceImpl();
		static {
			instance.setDataServiceSvc(DBClientFactory.getBlockingService());
			instance.setDataServiceSvc(DBClientFactory.getService());
		}
	}

	public static DataService getDataService() {
		return DataServiceHolder.instance;
	}

}
