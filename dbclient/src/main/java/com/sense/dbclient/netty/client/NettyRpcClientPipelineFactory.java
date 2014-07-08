package com.sense.dbclient.netty.client;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.sense.dbclient.api.DbProxy.ApiMessage;
import com.sense.dbclient.util.ApiExtensionHelper;

public class NettyRpcClientPipelineFactory implements ChannelPipelineFactory {
	private static final int MAX_FRAME_BYTES_LENGTH = 1048576;

	private NettyRpcClientChannelUpstreamHandler handler;
	private Message defaultInstance = ApiMessage.getDefaultInstance();
	private ExtensionRegistry extensionRegistry = ApiExtensionHelper
			.getExtensionRegistry();

	public NettyRpcClientPipelineFactory(
			NettyRpcClientChannelUpstreamHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = Channels.pipeline();


		p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
				MAX_FRAME_BYTES_LENGTH, 0, 4, 0, 4));
		p.addLast("frameEncoder", new LengthFieldPrepender(4));

		p.addLast("protobufDecoder", new ProtobufDecoder(defaultInstance,
				extensionRegistry));
		p.addLast("protobufEncoder", new ProtobufEncoder());

		p.addLast("handler", handler);
		return p;
	}
}
