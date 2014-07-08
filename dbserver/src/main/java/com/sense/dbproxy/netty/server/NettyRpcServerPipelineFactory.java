package com.sense.dbproxy.netty.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.sense.dbproxy.api.DbProxy.ApiMessage;
import com.sense.dbproxy.util.ApiExtensionHelper;

public class NettyRpcServerPipelineFactory implements ChannelPipelineFactory {
	private ChannelUpstreamHandler handler;
	private Message defaultInstance = ApiMessage.getDefaultInstance();
	private ExtensionRegistry extensionRegistry = ApiExtensionHelper
			.getExtensionRegistry();
	private static final int MAX_FRAME_BYTES_LENGTH = 1048576;
	private static final ChannelHandler executor = new ExecutionHandler(
			new JMXEnabledThreadPoolExecutor("RpcExecutor", 64, 256, 30,
					TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65535),
					new ThreadPoolExecutor.CallerRunsPolicy()));

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = Channels.pipeline();

		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
				MAX_FRAME_BYTES_LENGTH, 0, 4, 0, 4));
		p.addLast("frameEncoder", new LengthFieldPrepender(4));

		p.addLast("protobufDecoder", new ProtobufDecoder(defaultInstance,
				extensionRegistry));
		p.addLast("protobufEncoder", new ProtobufEncoder());

		p.addLast("executor", executor);
		p.addLast("handler", handler);
		return p;
	}

	public NettyRpcServerPipelineFactory(ChannelUpstreamHandler handler) {
		super();
		this.handler = handler;
	}

}