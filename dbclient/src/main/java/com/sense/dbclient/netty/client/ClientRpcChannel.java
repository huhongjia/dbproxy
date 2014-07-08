package com.sense.dbclient.netty.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.sense.dbclient.api.DbProxy;
import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiMessage;
import com.sense.dbclient.api.DbProxy.ApiRequest;
import com.sense.dbclient.api.DbProxy.ApiType;
import com.sense.dbclient.netty.NettyRpcController;
import com.sense.dbclient.util.ApiExtensionHelper;

public class ClientRpcChannel implements RpcChannel, BlockingRpcChannel
{

	/**
	 * 姝ょ嚎绋嬫瘡绉掗挓鍞ら啋涓�锛岃礋璐ｅ彂閫佸績璺炽�妫�祴杩炴帴銆佹柇寮�噸杩�	 */
	class HeartbeatThread extends Thread
	{
		@Override
		public void run()
		{
			long reconnectInterval = 1000;
			while (!closed)
			{
				try
				{
					//
					// 閲嶈繛
					//
					if (!isConnected())
					{
						// 鏈夊彲鑳芥槸璇垽锛屾墍浠ヤ负瀹夊叏璧疯寤惰繜鍏抽棴鍘熸潵鐨勮繛鎺ャ�
						if (rawChannel != null)
						{
							final Channel delayClosedChannel = rawChannel;
							final long delayTime = (timeout > 0 ? timeout * 2
									: 60000);
							new Thread()
							{
								@Override
								public void run()
								{
									sleepUninterruptibly(delayTime);
									delayClosedChannel.getCloseFuture()
											.removeListener(closeListener);
									delayClosedChannel.close()
											.awaitUninterruptibly();
								}
							}.start();
						}

						if (logger.isInfoEnabled())
						{
							logger.info("start to connect "
									+ remoteAddress.toString());
						}
						ChannelFuture f = bootstrap.connect(remoteAddress);
						f.awaitUninterruptibly();
						if (!f.isSuccess())
						{
							rawChannel = null;
							sleepUninterruptibly(reconnectInterval);
							reconnectInterval = reconnectInterval
									+ (reconnectInterval / 2);
							if (reconnectInterval > 60000)
							{
								reconnectInterval = 60000;
							}
							continue;
						}
						rawChannel = f.getChannel();
						rawChannel.getCloseFuture().addListener(closeListener);

						try
						{
							handler = rawChannel.getPipeline().get(
									NettyRpcClientChannelUpstreamHandler.class);
						}
						catch (Exception e)
						{
							throw new RuntimeException(
									"Channel does not have proper handler");
						}
						if (handler == null)
						{
							throw new RuntimeException(
									"Channel does not have proper handler");
						}

						lastSendTime = System.currentTimeMillis();
						handler.setLastReceiveTime(lastSendTime);
						updateConnectionStatus(true);
						synchronized (signal)
						{
							signal.notifyAll();
						}
						continue;
					}
					reconnectInterval = 1000;

					long now = System.currentTimeMillis();

					//
					// 杩炴帴妫�祴
					//
					if ((now - getLastReceiveTime()) > 60000)
					{
						updateConnectionStatus(false);
						continue;
					}
					if ((rawChannel == null) || !rawChannel.isConnected())
					{
						updateConnectionStatus(false);
						continue;
					}

					//
					// 蹇冭烦妫�祴
					//
					if ((now - lastSendTime) > 25000)
					{
						// 鍙戝績璺冲寘
						ApiHeader apiHeader = ApiHeader
								.newBuilder()
								.setSeq(0)
								.setType(ApiType.API_TYPE_HEARTBEAT.getNumber())
								.setVersion(0x00010000).build();

						ApiMessage.Builder apiMessage = ApiMessage.newBuilder()
								.setHeader(apiHeader);
						ChannelFuture f = rawChannel.write(apiMessage.build());
						if (f.awaitUninterruptibly(10000) && f.isSuccess())
						{
							lastSendTime = now;
						}
						else
						{
							updateConnectionStatus(false);
							continue;
						}
					}
				}
				catch (Throwable e)
				{
					logger.error("", e);
				}

				sleepUninterruptibly(1000);
			}

			if (rawChannel != null)
			{
				rawChannel.close().awaitUninterruptibly();
			}
		}

		private void sleepUninterruptibly(long millis)
		{
			try
			{
				Thread.sleep(millis);
			}
			catch (InterruptedException e)
			{
				// ignore;
			}
		}

		@Override
		public synchronized void start()
		{
			String name = "HeartbeatThread-" + remoteAddress.toString();
			setName(name);
			setDaemon(true);
			super.start();
		}
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ClientRpcChannel.class);

	private ClientBootstrap bootstrap;

	private volatile boolean closed = false; // 鎺у埗瀛楋紝鍏抽棴姝pc閫氶亾

	private final ChannelFutureListener closeListener = new ChannelFutureListener()
	{
		@Override
		public void operationComplete(ChannelFuture future) throws Exception
		{
			if (future.getChannel() == rawChannel)
			{
				updateConnectionStatus(false);
			}
		}
	};

	private volatile boolean connected = false; // 鐘舵�瀛楋紝鏄惁宸茶繛鎺�
	private ConnectionStatusListener connectionStatusListener;

	protected NettyRpcClientChannelUpstreamHandler handler; // 浠巖awChannel涓婅幏寰楃殑UpstreamHandler

	private volatile long lastSendTime = 0;

	protected Channel rawChannel; // 杩炴帴鍒皉emoteAddress鍚庤幏寰楃殑channel

	private InetSocketAddress remoteAddress;

	private final Object signal = new Object();

	private long timeout = 10000;

	private boolean used = false;

	public ClientRpcChannel(ClientBootstrap bootstrap,
			InetSocketAddress remoteAddress)
	{
		this.remoteAddress = remoteAddress;
		this.bootstrap = bootstrap;
		new HeartbeatThread().start();
	}

	public ClientRpcChannel(ClientBootstrap bootstrap,
			InetSocketAddress remoteAddress,
			ConnectionStatusListener connectionStatusListener)
	{
		this.connectionStatusListener = connectionStatusListener;
		this.remoteAddress = remoteAddress;
		this.bootstrap = bootstrap;
		new HeartbeatThread().start();
	}

	public void awaitConnected(long timeout)
	{
		synchronized (signal)
		{
			if (!connected)
			{
				try
				{
					signal.wait(timeout);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}

	private Message buildRequestMessage(int seqId, MethodDescriptor method,
			Message request)
	{

		// header
		ApiHeader.Builder requestApiHeader = ApiHeader.newBuilder();
		requestApiHeader.setSeq(seqId);
		requestApiHeader.setVersion(0x00010000);
		if (handler.getSid() != null)
		{
			requestApiHeader.setSid(handler.getSid());
		}
		requestApiHeader.setType(ApiType.API_TYPE_REQUEST.getNumber());// 鍖呯被鍨嬶紝鍙栧�涓猴細璇锋眰涓�銆佸搷搴斾负1

		// body
		ApiRequest.Builder requestApiRequest = ApiRequest.newBuilder();
		requestApiRequest.setService(method.getService().getName());
		requestApiRequest.setMethod(method.getName());
		requestApiRequest.setExtension(
				ApiExtensionHelper.getRequestByMethodName(method.getName()),
				request);

		// api message
		ApiMessage.Builder requestApiMessage = ApiMessage.newBuilder();
		requestApiMessage.setHeader(requestApiHeader);
		requestApiMessage.setExtension(DbProxy.request,
				requestApiRequest.build());

		return requestApiMessage.build();
	}

	@Override
	public Message callBlockingMethod(MethodDescriptor method,
			final RpcController controller, Message request,
			Message responsePrototype) throws ServiceException
	{
		final ArrayBlockingQueue<Object> responseQueue = new ArrayBlockingQueue<Object>(
				1);
		callMethod(method, controller, request, responsePrototype,
				new RpcCallback<Message>()
				{
					@Override
					public void run(Message response)
					{
						try
						{
							responseQueue.put(response);
						}
						catch (InterruptedException e)
						{
							controller.startCancel();
							controller.setFailed(e.getMessage() != null ? e
									.getMessage() : "Interrupted");
						}
					}
				});

		Message response = null;
		try
		{
			response = (Message) responseQueue.poll(timeout,
					TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			controller.startCancel();
			throw new ServiceException(e.getMessage());
		}

		if (response == null)
		{
			controller.startCancel();
			throw new ServiceException("Time out.");
		}

		if (controller.failed())
		{
			throw new ServiceException(controller.errorText());
		}

		return response;
	}

	@Override
	public void callMethod(MethodDescriptor method,
			final RpcController controller, Message request,
			Message responsePrototype, RpcCallback<Message> done)
	{
		if (!isConnected())
		{
			throw new RuntimeException("Network is disconnected.");
		}
		if (done == null)
		{
			throw new IllegalArgumentException(
					"Parameter 'done' can not be null.");
		}

		final NettyRpcClientChannelUpstreamHandler handler = this.handler;
		final int nextSeqId = handler.getNextSeqId();
		Message apiMessage = buildRequestMessage(nextSeqId, method, request);
		handler.registerCallback(nextSeqId, new ResponsePrototypeRpcCallback(
				method, controller, responsePrototype, done));

		ChannelFuture f = rawChannel.write(apiMessage);
		if (f == null)
		{
			updateConnectionStatus(false);
			handler.unregisterCallback(nextSeqId);
			return;
		}

		lastSendTime = System.currentTimeMillis();

		f.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception
			{
				if (!future.isDone())
				{
					return;
				}
				if (future.isSuccess())
				{
					return;
				}
				if (future.isCancelled())
				{
					updateConnectionStatus(false);
					controller.setFailed("The rpc is cancelled.");
					return;
				}
				if (future.getCause() != null)
				{
					updateConnectionStatus(false);
					logger.error("Failed to call rpc.", future.getCause());

					if (future.getCause().getMessage() != null)
					{
						controller.setFailed(future.getCause().getMessage());
					}
					else
					{
						controller.setFailed("Unknown reason for the failure.");
					}
					//
					// 浣胯皟鐢ㄥ揩閫熷け璐ワ紝涓嶅繀绛夊埌瓒呮椂
					//
					ResponsePrototypeRpcCallback callback = handler
							.unregisterCallback(nextSeqId);
					if (callback != null)
					{
						callback.run(null);
					}
					return;
				}
			}
		});

		controller.notifyOnCancel(new RpcCallback<Object>()
		{
			@Override
			public void run(Object parameter)
			{
				handler.unregisterCallback(nextSeqId);
			}
		});
	}

	public void close()
	{
		logger.info("Close the connection " + remoteAddress.toString());
		this.closed = true;
	}

	/**
	 * @return the connectionStatusListener
	 */
	public ConnectionStatusListener getConnectionStatusListener()
	{
		return connectionStatusListener;
	}

	public long getLastReceiveTime()
	{
		return handler.getLastReceiveTime();
	}

	public long getLastSendTime()
	{
		return lastSendTime;
	}

	public Channel getRawChannel()
	{
		return rawChannel;
	}

	public InetSocketAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public long getTimeout()
	{
		return timeout;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public boolean isConnected()
	{
		return connected && (rawChannel != null) && rawChannel.isConnected();
	}

	public boolean isUsed()
	{
		return this.used;
	}

	public NettyRpcController newRpcController()
	{
		return new NettyRpcController();
	}

	// public Message callBlockingMethod(MethodDescriptor method,
	// RpcController controller, Message request, Message responsePrototype)
	// throws ServiceException {
	// FutureResponse<Message> fr = new FutureResponse<Message>();
	//
	// callMethod(method, controller, request, responsePrototype, fr);
	//
	// Message response = null;
	// try {
	// response = fr.get(timeout, TimeUnit.MILLISECONDS);
	// } catch (InterruptedException e) {
	// controller.startCancel();
	// throw new ServiceException(e.getMessage());
	// } catch (ExecutionException e) {
	// controller.startCancel();
	// throw new ServiceException(e.getMessage());
	// } catch (TimeoutException e) {
	// controller.startCancel();
	// throw new ServiceException("Time out.");
	// }
	//
	// if (controller.failed()) {
	// throw new ServiceException(controller.errorText());
	// }
	//
	// return response;
	// }

	/**
	 * @param connectionStatusListener
	 *            the connectionStatusListener to set
	 */
	public void setConnectionStatusListener(
			ConnectionStatusListener connectionStatusListener)
	{
		this.connectionStatusListener = connectionStatusListener;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public void setUsed(Boolean used)
	{
		this.used = used;
	}

	private void updateConnectionStatus(boolean connected)
	{
		this.connected = connected;
		if (connected)
		{
			logger.info("Connected to " + remoteAddress.toString());
		}
		else
		{
			logger.info("Disconnected to " + remoteAddress.toString());
			if (logger.isDebugEnabled())
			{
				logger.debug("Call stack:", new Exception());
			}
		}

		if (connectionStatusListener != null)
		{
			connectionStatusListener.onStatusChanged(connected, this);
		}
		else
		{
			logger.warn("connection status changed, but connectionStatusListener is null.");
		}
	}
}

