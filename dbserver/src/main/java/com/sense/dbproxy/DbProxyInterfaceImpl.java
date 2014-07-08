package com.sense.dbproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.sense.dbproxy.api.DbProxy.CreateUserResponse;
import com.sense.dbproxy.api.DbProxy.DBProxyService.Interface;
import com.sense.dbproxy.api.DbProxy.DeleteUserResponse;
import com.sense.dbproxy.api.DbProxy.GetUserResponse;
import com.sense.dbproxy.api.DbProxy.SSResultCode;
import com.sense.dbproxy.api.DbProxy.SetUserRequest;
import com.sense.dbproxy.api.DbProxy.SetUserResponse;
import com.sense.dbproxy.netty.NettyRpcController;
import com.sense.dbproxy.service.DbProxyService;

public class DbProxyInterfaceImpl implements Interface{
	
	 private static Logger logger = LoggerFactory.getLogger(DbProxyInterfaceImpl.class);
	 private DbProxyService impl;
	
	

	@Override
	public void createUser(RpcController controller,
			com.sense.dbproxy.api.DbProxy.CreateUserRequest request,
			RpcCallback<com.sense.dbproxy.api.DbProxy.CreateUserResponse> done) {
			CreateUserResponse.Builder response = CreateUserResponse.newBuilder();
			try {
				impl.createUser((NettyRpcController) controller, request, response);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				((NettyRpcController)controller).setCode(SSResultCode.RC_SERVER_INTERNAL_ERROR.getNumber());
				((NettyRpcController)controller).setFailed(e.getMessage());
			}
			done.run(response.build());
	}

	@Override
	public void getUser(RpcController controller,
			com.sense.dbproxy.api.DbProxy.GetUserRequest request,
			RpcCallback<com.sense.dbproxy.api.DbProxy.GetUserResponse> done) {
		GetUserResponse.Builder response = GetUserResponse.newBuilder();
		try {
			impl.getUser((NettyRpcController) controller, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			((NettyRpcController)controller).setCode(SSResultCode.RC_SERVER_INTERNAL_ERROR.getNumber());
			((NettyRpcController)controller).setFailed(e.getMessage());
		}
		done.run(response.build());
		
	}

	@Override
	public void setUser(RpcController controller, SetUserRequest request,
			RpcCallback<SetUserResponse> done) {
		SetUserResponse.Builder response = SetUserResponse.newBuilder();
		try {
			impl.setUser((NettyRpcController) controller, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			((NettyRpcController)controller).setCode(SSResultCode.RC_SERVER_INTERNAL_ERROR.getNumber());
			((NettyRpcController)controller).setFailed(e.getMessage());
		}
		done.run(response.build());
		
	}

	@Override
	public void deleteUser(RpcController controller,
			com.sense.dbproxy.api.DbProxy.DeleteUserRequest request,
			RpcCallback<com.sense.dbproxy.api.DbProxy.DeleteUserResponse> done) {
		DeleteUserResponse.Builder response = DeleteUserResponse.newBuilder();
		try {
			impl.deleteUser((NettyRpcController) controller, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			((NettyRpcController)controller).setCode(SSResultCode.RC_SERVER_INTERNAL_ERROR.getNumber());
			((NettyRpcController)controller).setFailed(e.getMessage());
		}
		done.run(response.build());
		
	}

	public DbProxyService getImpl() {
		return impl;
	}

	public void setImpl(DbProxyService impl) {
		this.impl = impl;
	}

	
}
