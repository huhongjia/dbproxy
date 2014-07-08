package com.sense.dbproxy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sense.dbproxy.api.DbProxy.CreateUserRequest;
import com.sense.dbproxy.api.DbProxy.CreateUserResponse.Builder;
import com.sense.dbproxy.api.DbProxy.DeleteUserRequest;
import com.sense.dbproxy.api.DbProxy.GetUserRequest;
import com.sense.dbproxy.api.DbProxy.SSResultCode;
import com.sense.dbproxy.api.DbProxy.SetUserRequest;
import com.sense.dbproxy.api.DbProxy.User;
import com.sense.dbproxy.dao.UserDao;
import com.sense.dbproxy.netty.NettyRpcController;
import com.sense.dbproxy.service.DbProxyService;
@Service
@Transactional
public class DbProxyServiceImpl implements DbProxyService{
	
    @Autowired(required=true)
    private UserDao userDao;
    
	@Override
	public void createUser(NettyRpcController controller,
			CreateUserRequest request, Builder response) throws Exception {
		User user = request.getUser();
		userDao.save(user);
		/*
		 ye wu
		 */
		controller.setCode(SSResultCode.RC_OK.getNumber());
		
	}

	@Override
	public void getUser(NettyRpcController controller, GetUserRequest request,
			com.sense.dbproxy.api.DbProxy.GetUserResponse.Builder response)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUser(NettyRpcController controller, SetUserRequest request,
			com.sense.dbproxy.api.DbProxy.SetUserResponse.Builder response)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(NettyRpcController controller,
			DeleteUserRequest request,
			com.sense.dbproxy.api.DbProxy.DeleteUserResponse.Builder response)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	



}
