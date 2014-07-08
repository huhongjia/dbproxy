package com.sense.dbproxy.netty.server;

import java.net.SocketAddress;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Service;

public class NettyRpcServer{
	private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);
	private final ChannelPipelineFactory pipelineFactory;
	private final NettyRpcServerChannelUpstreamHandler handler = new NettyRpcServerChannelUpstreamHandler();
	private final ServerBootstrap bootstrap;
	public NettyRpcServer(ChannelFactory channelFactory) {
		this(channelFactory, false);
	}

	public NettyRpcServer(ChannelFactory channelFactory, boolean b) {
		this.pipelineFactory = new NettyRpcServerPipelineFactory(handler);
		bootstrap = new ServerBootstrap(channelFactory);
		bootstrap.setPipelineFactory(pipelineFactory);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
	}
	
	 public void registerService(Service service){
		 handler.registerService(service);
	}
	public void serve() {
		logger.info("Serving...");
		bootstrap.bind();
	}
	
	public void serve(SocketAddress sa) {
		logger.info("Serving on: " + sa);
		bootstrap.bind(sa);
	}

	
	
}