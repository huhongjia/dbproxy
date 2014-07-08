package com.sense.dbclient.service.impl;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.sense.dbclient.api.DbProxy;
import com.sense.dbclient.api.DbProxy.DBProxyService.BlockingInterface;
import com.sense.dbclient.api.DbProxy.DBProxyService.Interface;
import com.sense.dbclient.netty.client.NettyRpcChannel;
import com.sense.dbclient.netty.client.NettyRpcClient;
import com.sense.dbclient.util.PropertiesHelper;

public class DBClientFactory {
	private static class BlockingInterfaceHolder
	{
		private static BlockingInterface dbProxyClient;
		static
		{
			dbProxyClient = DbProxy.DBProxyService.newBlockingStub(ChannelHolder.channel);
		}
	}

	private static class ChannelHolder
	{
		private static NettyRpcChannel channel;

		private static NettyRpcClient client;
		static
		{
			client = new NettyRpcClient(new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));

			String[] ips = PropertiesHelper.getValue("dbproxy.ip",
					"127.0.0.1").split(" ");
			String[] ports = PropertiesHelper.getValue("dbproxy.port",
					"33306").split(" ");
			InetSocketAddress[] isas = new InetSocketAddress[ips.length];
			for (int i = 0; i < ips.length; i++)
			{
				isas[i] = new InetSocketAddress(ips[i],
						Integer.parseInt(ports[i]));
			}
			channel = client.blockingConnect(Arrays.asList(isas));
		}

		public static void close()
		{
			client.shutdown();
		}
	}

	private static class InterfaceHolder
	{
		private static Interface dbProxyClient;
		static
		{
			dbProxyClient =  DbProxy.DBProxyService.newStub(ChannelHolder.channel);
		}
	}

	public static void close()
	{
		ChannelHolder.close();
	}

	public static BlockingInterface getBlockingService()
	{
		return BlockingInterfaceHolder.dbProxyClient;
	}

	public static Interface getService()
	{
		return InterfaceHolder.dbProxyClient;
	}

}
