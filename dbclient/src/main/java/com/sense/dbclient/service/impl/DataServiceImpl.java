package com.sense.dbclient.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ServiceException;
import com.sense.dbclient.api.DbProxy.CreateUserRequest;
import com.sense.dbclient.api.DbProxy.DBProxyService.BlockingInterface;
import com.sense.dbclient.api.DbProxy.DBProxyService.Interface;
import com.sense.dbclient.api.DbProxy.DeleteUserRequest;
import com.sense.dbclient.api.DbProxy.GetUserRequest;
import com.sense.dbclient.api.DbProxy.GetUserResponse;
import com.sense.dbclient.api.DbProxy.SetUserRequest;
import com.sense.dbclient.api.DbProxy.User;
import com.sense.dbclient.netty.NettyRpcController;
import com.sense.dbclient.service.DataService;

public class DataServiceImpl implements DataService {
	private static final Logger logger = LoggerFactory
			.getLogger(DataServiceImpl.class);
	private BlockingInterface dd;

	private Interface ddAsync;

	@Override
	public boolean createUser(User user) {
		NettyRpcController controller = new NettyRpcController();
		CreateUserRequest setUserRequest = CreateUserRequest.newBuilder()
				.setUser(user).build();
		try {
			getDataDomainSvc().createUser(controller, setUserRequest);
			if (controller.failed()) {
				logger.error(controller.errorText());
				return false;
			}
			return true;
		} catch (ServiceException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public User getUser(int id) {
		NettyRpcController controller = new NettyRpcController();
		GetUserRequest setUserRequest = GetUserRequest.newBuilder().setId(id)
				.build();
		try {
			GetUserResponse getUserResponse = getDataDomainSvc().getUser(
					controller, setUserRequest);
			if (controller.failed()) {
				logger.error(controller.errorText());
				return null;
			}
			return getUserResponse.getUser();
		} catch (ServiceException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean setUser(User user) {
		NettyRpcController controller = new NettyRpcController();
		SetUserRequest setUserRequest = SetUserRequest.newBuilder()
				.setUser(user).build();
		try {
			getDataDomainSvc().setUser(controller, setUserRequest);
			if (controller.failed()) {
				logger.error(controller.errorText());
				return false;
			}
			return true;
		} catch (ServiceException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteUser(int id) {
		NettyRpcController controller = new NettyRpcController();
		DeleteUserRequest setUserRequest = DeleteUserRequest.newBuilder()
				.setId(id).build();
		try {
			getDataDomainSvc().deleteUser(controller, setUserRequest);
			if (controller.failed()) {
				logger.error(controller.errorText());
				return false;
			}
			return true;
		} catch (ServiceException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void setDataServiceSvc(BlockingInterface blockingService) {
		this.dd = blockingService;

	}

	@Override
	public void setDataServiceSvc(Interface service) {
		this.ddAsync = service;

	}

	public BlockingInterface getDataDomainSvc() {
		return this.dd;
	}

	public Interface getDataDomainSvcAsync() {
		return this.ddAsync;
	}

}
