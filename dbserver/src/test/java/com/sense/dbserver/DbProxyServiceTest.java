package com.sense.dbserver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sense.dbproxy.dao.UserDao;
import com.sense.dbproxy.po.User;
import com.sense.dbproxy.service.DbProxyService;
import com.sense.dbproxy.service.impl.DbProxyServiceImpl;

public class DbProxyServiceTest {

	ApplicationContext ac;
	
	@Before
	public void before(){
		ac = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	@Test
	public void test() {
		DbProxyServiceImpl dps = (DbProxyServiceImpl)ac.getBean("dbProxyServiceImpl");
		System.out.println(dps.getUserDao());
		UserDao userDao = (UserDao)ac.getBean("userDao");
		System.out.println(userDao);
//		userDao.save(new com.sense.dbproxy.api.DbProxy.User());
	}

}
