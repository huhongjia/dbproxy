package com.sense.dbproxy;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.protobuf.Service;
import com.sense.dbproxy.api.DbProxy.DBProxyService;
import com.sense.dbproxy.netty.server.NettyRpcServer;
import com.sense.dbproxy.service.impl.DbProxyServiceImpl;
import com.sense.dbproxy.util.DBProperties;
import com.sense.dbproxy.util.JMXLog4jConfigurator;

public class DbProxyServer {
	private static Logger logger = LoggerFactory.getLogger(DbProxyServer.class);
	private boolean hasCmdSetting = false;
	private String host;
	private int port = 33306;

	private NettyRpcServer server;
	private DbProxyInterfaceImpl dbService;
	private static ApplicationContext context;

	public void init(String[] args) {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-host")) {
					i++;
					host = args[i];
					hasCmdSetting = true;
				} else if (args[i].equalsIgnoreCase("-port")) {
					i++;
					port = Integer.valueOf(args[i]);
					hasCmdSetting = true;
				}
			}
		}
		context = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });
		server = new NettyRpcServer(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		dbService = new DbProxyInterfaceImpl();
//		dbService.setImpl(new DbProxyServiceImpl());
		dbService.setImpl((DbProxyServiceImpl)context.getBean("dbProxyServiceImpl"));
		Service svc = DBProxyService.newReflectiveService(dbService);
		server.registerService(svc);

	}

	public void start() {
		logger.info("DBProxyServer starting up...");
		InetSocketAddress address = null;
		try {
			if (hasCmdSetting) {
				if (host == null) {
					address = new InetSocketAddress(port);
				} else {
					address = new InetSocketAddress(host, port);
				}
			} else {
				if (DBProperties.getListenAddress() == null) {
					address = new InetSocketAddress(
							DBProperties.getListenPort());
				} else {
					address = new InetSocketAddress(
							DBProperties.getListenAddress(),
							DBProperties.getListenPort());
				}
			}
			server.serve(address);
		} catch (Exception e) {
//			 TODO: handle exception
		}

	}

	public static void main(String[] args) {
		JMXLog4jConfigurator.config();
		
		  
	    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                logger.error("Uncaught exception in thread " + t, e);
                if (e instanceof OutOfMemoryError)
                {
                    System.exit(100);
                }
            }
        });
	    
		try {
			DbProxyServer dps = new DbProxyServer();
			dps.init(args);
			dps.start();
		} catch (Exception e) {
		    String msg = "Exception encountered during startup.";
            logger.error(msg, e);

            // try to warn user on stdout too, if we haven't already detached
            System.out.println(msg);
            e.printStackTrace();

            System.exit(3);
		}
	}
}
