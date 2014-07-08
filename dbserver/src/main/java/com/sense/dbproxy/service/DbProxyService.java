package com.sense.dbproxy.service;

import com.sense.dbproxy.api.DbProxy.CreateUserRequest;
import com.sense.dbproxy.api.DbProxy.CreateUserResponse;
import com.sense.dbproxy.api.DbProxy.DeleteUserRequest;
import com.sense.dbproxy.api.DbProxy.DeleteUserResponse;
import com.sense.dbproxy.api.DbProxy.GetUserRequest;
import com.sense.dbproxy.api.DbProxy.GetUserResponse;
import com.sense.dbproxy.api.DbProxy.SetUserRequest;
import com.sense.dbproxy.api.DbProxy.SetUserResponse;
import com.sense.dbproxy.netty.NettyRpcController;

public interface DbProxyService {
	public void createUser(NettyRpcController controller, CreateUserRequest request,
			CreateUserResponse.Builder response) throws Exception;

	public void getUser(NettyRpcController controller, GetUserRequest request,
			GetUserResponse.Builder response) throws Exception;

	public void setUser(NettyRpcController controller, SetUserRequest request,
			SetUserResponse.Builder response) throws Exception;

	public void deleteUser(NettyRpcController controller, DeleteUserRequest request,
			DeleteUserResponse.Builder response) throws Exception;

}
