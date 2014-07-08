package com.sense.dbclient.netty.client;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;

import com.google.protobuf.Service;

public class NettyRpcClient {
	private final ClientBootstrap bootstrap;

	private ConnectionStatusListener connectionStatusListener;

	private final NettyRpcClientChannelUpstreamHandler handler = new NettyRpcClientChannelUpstreamHandler();

	private final ChannelPipelineFactory pipelineFactory;


	public NettyRpcClient(ChannelFactory channelFactory) {
		this.pipelineFactory = new NettyRpcClientPipelineFactory(handler);
		bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("connectTimeoutMillis", 10000);
	}

	public NettyRpcChannel blockingConnect(Collection<InetSocketAddress> addresses) {
		NettyRpcChannel c = new NettyRpcChannel(bootstrap, addresses,
				connectionStatusListener);
		c.awaitConnected();
		return c;
	}

	public NettyRpcChannel blockingConnect(InetSocketAddress sa) {
		return blockingConnect(Collections.singleton(sa));
	}

	public NettyRpcChannel connect(Collection<InetSocketAddress> addresses) {
		return new NettyRpcChannel(bootstrap, addresses,
				connectionStatusListener);
	}

	public NettyRpcChannel connect(InetSocketAddress sa) {
		return connect(Collections.singleton(sa));
	}

	/**
	 * @return the connectionStatusListener
	 */
	public ConnectionStatusListener getConnectionStatusListener() {
		return connectionStatusListener;
	}

	public void registerCallbackService(Service service) {
		handler.registerService(service);
	}

	/**
	 * @param connectionStatusListener
	 *            the connectionStatusListener to set
	 */
	public void setConnectionStatusListener(
			ConnectionStatusListener connectionStatusListener) {
		this.connectionStatusListener = connectionStatusListener;
	}

	public void shutdown() {
		bootstrap.releaseExternalResources();
	}

	public void unregisterCallbackService(Service service) {
		handler.unregisterService(service);
	}

}
