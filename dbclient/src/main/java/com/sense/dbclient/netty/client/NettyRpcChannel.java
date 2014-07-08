package com.sense.dbclient.netty.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.sense.dbclient.netty.NettyRpcController;

public class NettyRpcChannel implements RpcChannel, BlockingRpcChannel
{
	private static final Logger logger = LoggerFactory.getLogger(NettyRpcChannel.class);

	private List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

	private List<JMXEnabledClientRpcChannel> channels = new ArrayList<JMXEnabledClientRpcChannel>();

	private volatile JMXEnabledClientRpcChannel current = null;

	private long pendingThreshold = 64;

	public NettyRpcChannel(ClientBootstrap bootstrap,
			Collection<InetSocketAddress> addresses)
	{
		this(bootstrap, addresses, null);
	}

	/**
	 * 褰撻泦缇ゅ湴鍧�垪琛ㄨ秴杩�涓椂锛屽彧闅忔満閫夊彇3涓缓绔嬭繛鎺ャ�
	 * 
	 * @param bootstrap
	 * @param addresses
	 *            闆嗙兢鑺傜偣鐨勫湴鍧�垪琛�
	 */
	public NettyRpcChannel(ClientBootstrap bootstrap,
			Collection<InetSocketAddress> addresses,
			ConnectionStatusListener connectionStatusListener)
	{
		if ((addresses == null) || (addresses.size() == 0))
		{
			throw new IllegalArgumentException("addresses must be non-empty.");
		}

		//
		// 灏嗗垪琛ㄩ『搴忛殢鏈烘墦涔憋紝杩欐牱鍙互鍧囪　澶氫釜瀹㈡埛绔殑璋冪敤
		//
		this.addresses.addAll(addresses);
		Collections.shuffle(this.addresses);

		//
		// 鏈�閫�鍙版湇鍔″櫒浣滀负璐熻浇鍧囪　鍜屽鐢�
		//
		int count = 0;
		for (InetSocketAddress address : this.addresses)
		{
			JMXEnabledClientRpcChannel channel = new JMXEnabledClientRpcChannel(
					bootstrap, address, connectionStatusListener);
			channels.add(channel);
			if (++count >= 3)
			{
				return;
			}
		}
	}

	public ClientRpcChannel awaitConnected()
	{
		while (true)
		{
			try
			{
				return awaitConnected(1000);
			}
			catch (TimeoutException e)
			{
				// ignore
			}
		}
	}

	public ClientRpcChannel awaitConnected(long timeout)
			throws TimeoutException
	{
		long end = System.currentTimeMillis() + timeout;
		while (true)
		{
			long start = System.currentTimeMillis();

			long left = end - start;
			if (left <= 0)
			{
				throw new TimeoutException();
			}

			long await = Math.min(Math.max(1, left / channels.size()), 100);
			for (JMXEnabledClientRpcChannel channel : channels)
			{
				channel.awaitConnected(await);
				if (isAlive(channel))
				{
					return channel;
				}
			}

			await = (start + await) - System.currentTimeMillis();
			if (await > 0)
			{
				try
				{
					Thread.sleep(await);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}
	}

	@Override
	public Message callBlockingMethod(MethodDescriptor method,
			RpcController controller, Message request, Message responsePrototype)
			throws ServiceException
	{
		Object attachment = null;
		ChannelHandlerContext context = ((NettyRpcController) controller)
				.getChannelHandlerContext();
		if (context != null)
		{
			attachment = context.getAttachment();
		}
		ClientRpcChannel channel = getChannel((JMXEnabledClientRpcChannel) attachment);
		if (channel == null)
		{
			controller.setFailed("Network is disconnected.");
			throw new ServiceException("Network is disconnected.");
		}
		return channel.callBlockingMethod(method, controller, request,
				responsePrototype);
	}

	@Override
	public void callMethod(MethodDescriptor method, RpcController controller,
			Message request, Message responsePrototype,
			RpcCallback<Message> done)
	{
		ClientRpcChannel channel = getChannel();
		if (channel == null)
		{
			controller.setFailed("Network is disconnected.");
			throw new RuntimeException("Network is disconnected.");
		}
		channel.callMethod(method, controller, request, responsePrototype, done);
	}

	public void close()
	{
		for (ClientRpcChannel channel : channels)
		{
			channel.close();
		}
	}

	public ClientRpcChannel getChannel()
	{
		return getChannel(null);
	}

	public ClientRpcChannel getChannel(JMXEnabledClientRpcChannel hint)
	{
		if (hint != null)
		{
			return isAlive(hint) ? routeTo(hint) : null;
		}

		long minPendingCount = Long.MAX_VALUE;
		JMXEnabledClientRpcChannel best = null;
		if (current == null)
		{
			for (JMXEnabledClientRpcChannel channel : channels)
			{
				if (isAlive(channel))
				{
					return routeTo(channel);
				}
			}
			return null;
		}

		//
		// 濡傛灉褰撳墠閫氶亾绉疮鐨勬湭瀹屾垚RPC瓒呰繃闃堝�锛�0涓級锛屽垯灏濊瘯鍏朵粬閫氶亾銆�
		//
		long pendingCount = current.getPendingCount();
		if (isAlive(current))
		{
			if (pendingCount < pendingThreshold)
			{
				return routeTo(current);
			}
			else
			{
				minPendingCount = pendingCount;
				best = current;
			}
		}

		//
		// 璐熻浇鍧囪　鐨勭瓥鐣ワ細
		// 浠庢墍鏈夐�閬撻噷鎸戦�鎵惧埌鐨勭涓�釜鏈畬鎴怰PC涓暟灏忎簬闃堝�鐨勯�閬擄紝濡傛灉涓嶅瓨鍦ㄨ繖鏍风殑閫氶亾锛�
		// 鍒欐寫閫夋湭瀹屾垚RPC涓暟鏈�皬鐨勯�閬撱�涓轰簡鍏钩锛屼粠褰撳墠閫氶亾鐨勪笅涓�釜閫氶亾寮�灏濊瘯锛屽啀缁�
		// 鍥炲埌鍒楄〃鍓嶉潰
		//
		int i, j;
		i = j = channels.indexOf(current);
		while (((++i) % channels.size()) != j)
		{
			JMXEnabledClientRpcChannel channel = channels.get(i
					% channels.size());
			if (isAlive(channel))
			{
				pendingCount = channel.getPendingCount();
				if (pendingCount < pendingThreshold)
				{
					return routeTo(channel);
				}
				else if (pendingCount < minPendingCount)
				{
					minPendingCount = pendingCount;
					best = channel;
				}
			}
		}

		if (best != null)
		{
			return routeTo(best);
		}

		return null;
	}

	public ClientRpcChannel getChannelBlocking()
	{
		ClientRpcChannel channel = getChannel();
		if (channel != null)
		{
			return channel;
		}

		//
		// 鍒拌繖閲岃〃鏄巆hannels鍏ㄩ儴鏄柇寮�殑
		//
		channel = awaitConnected();
		return routeTo((JMXEnabledClientRpcChannel) channel);
	}

	public long getPendingThreshold()
	{
		return pendingThreshold;
	}

	public long getTimeout()
	{
		for (ClientRpcChannel channel : channels)
		{
			return channel.getTimeout();
		}
		return 0;
	}

	private boolean isAlive(JMXEnabledClientRpcChannel channel)
	{
		return ((channel != null) && !channel.isClosed() && channel.isEnabled() && channel
				.isConnected());
	}

	public boolean isClosed()
	{
		for (ClientRpcChannel channel : channels)
		{
			if (!channel.isClosed())
			{
				return false;
			}
		}
		return true;
	}

	public boolean isConnected()
	{
		for (ClientRpcChannel channel : channels)
		{
			if (!channel.isClosed() && channel.isConnected())
			{
				return true;
			}
		}
		return false;
	}

	public NettyRpcController newRpcController()
	{
		return new NettyRpcController();
	}

	private ClientRpcChannel routeTo(JMXEnabledClientRpcChannel channel)
	{
		if (channel != current)
		{
			logger.info("route rpc to " + channel.getRemoteAddress().toString());
			if (current != null)
			{
				current.setUsed(false);
			}
			current = channel;
			current.setUsed(true);
		}
		return channel;
	}

	public void setPendingThreshold(long pendingThreshold)
	{
		this.pendingThreshold = pendingThreshold;
	}

	public void setTimeout(long timeout)
	{
		for (ClientRpcChannel channel : channels)
		{
			channel.setTimeout(timeout);
		}
	}
}

