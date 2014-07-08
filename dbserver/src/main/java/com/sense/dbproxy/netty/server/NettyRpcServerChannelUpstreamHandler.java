package com.sense.dbproxy.netty.server;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import com.google.protobuf.TextFormat;
import com.sense.dbproxy.api.DbProxy;
import com.sense.dbproxy.api.DbProxy.ApiHeader;
import com.sense.dbproxy.api.DbProxy.ApiMessage;
import com.sense.dbproxy.api.DbProxy.ApiRequest;
import com.sense.dbproxy.api.DbProxy.ApiResponse;
import com.sense.dbproxy.api.DbProxy.ApiType;
import com.sense.dbproxy.exception.InvalidRpcRequestException;
import com.sense.dbproxy.exception.NoSuchServiceException;
import com.sense.dbproxy.exception.NoSuchServiceMethodException;
import com.sense.dbproxy.exception.RpcException;
import com.sense.dbproxy.netty.NettyRpcController;
import com.sense.dbproxy.util.ApiExtensionHelper;
public class NettyRpcServerChannelUpstreamHandler extends SimpleChannelUpstreamHandler{
	private static final Logger logger = LoggerFactory.getLogger(NettyRpcServerChannelUpstreamHandler.class);
	
	protected final Map<String, Service> defaultServiceMap = new ConcurrentHashMap<String, Service>();
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		logger.info(e.getMessage().toString());
		final ApiMessage message =  (ApiMessage) e.getMessage();
		if (logger.isDebugEnabled())
		    logger.debug(TextFormat.printToString(message));
		
		if (message.getHeader().getType() == ApiType.API_TYPE_REQUEST.getNumber()) {
			OnReceiveRequest(ctx, e, message.getHeader(), message.getExtension(DbProxy.request));
		} else if (message.getHeader().getType() == ApiType.API_TYPE_RESPONSE.getNumber()) {
			OnReceiveResponse(ctx, e, message.getHeader(), message.getExtension(DbProxy.response));
		} else if (message.getHeader().getType() == ApiType.API_TYPE_HEARTBEAT.getNumber()){
			OnHeartbeatRequest(ctx, e, message.getHeader());
		} else {
			logger.warn("Can't handle the received message type: " + message.getHeader().getType());
		}
	}

	private void OnReceiveResponse(ChannelHandlerContext ctx, MessageEvent e,
			ApiHeader header, ApiResponse extension) {
		// TODO Auto-generated method stub
		
	}

	private void OnReceiveRequest(ChannelHandlerContext ctx, MessageEvent e,
			ApiHeader header, ApiRequest request) throws RpcException {
		final String serviceName = request.getService();
		final String methodName = request.getMethod();
		
//		logger.info("Received request: " + header.getSeq() + ", service: " + serviceName + ", method: " + methodName + ", version: " + version);
		
		Service service = getService(serviceName);
		if (service == null) {
			throw new NoSuchServiceException(header, request, serviceName);
		} else if (service.getDescriptorForType().findMethodByName(methodName) == null) {
			throw new NoSuchServiceMethodException(header, request, methodName);
		}
		
		try {
			MethodDescriptor methodDescriptor = service.getDescriptorForType().findMethodByName(methodName);
			final Message methodRequest = 
					request.getExtension(ApiExtensionHelper.<Message>getRequestByMethodName(request.getMethod()));
			if (methodRequest == null)
				throw new InvalidRpcRequestException(header, request, methodName);
			
			final Channel channel = e.getChannel();
			final NettyRpcController controller = new NettyRpcController();
			
			controller.setChannelHandlerContext(ctx);
			controller.setApiHeader(header);
			controller.setApiRequest(request);
			controller.setApiResponse(ApiResponse.newBuilder());
			
			RpcCallback<Message> callback = !header.hasSeq() ? null : new RpcCallback<Message>() {
				public void run(Message methodResponse) {
					

					// header
					ApiHeader.Builder responseApiHeader = ApiHeader.newBuilder();
					responseApiHeader.setSeq(controller.getApiHeader().getSeq());
					if (controller.getApiHeader().hasVersion()) {
						responseApiHeader.setVersion(controller.getApiHeader().getVersion());
					}
					if (controller.getApiHeader().hasSid()) {
						responseApiHeader.setSid(controller.getApiHeader().getSid());
					}
					responseApiHeader.setType(ApiType.API_TYPE_RESPONSE.getNumber());

					// body
					ApiResponse.Builder responseApiResponse = controller.getApiResponse();
					if (methodResponse == null) {
						responseApiResponse.setCode(controller.getCode());
						if (controller.errorText() != null) {
							responseApiResponse.setReason(controller.errorText());
						} else {
							
						}
					} else {
						responseApiResponse.setCode(controller.getCode());
						if (controller.errorText()!= null)
							responseApiResponse.setReason(controller.errorText());
						responseApiResponse.setExtension(ApiExtensionHelper.getResponseByMethodName(methodName), methodResponse);
					}

					// api message
					ApiMessage.Builder responseApiMessage = ApiMessage.newBuilder();
					responseApiMessage.setHeader(responseApiHeader);
					responseApiMessage.setExtension(DbProxy.response, responseApiResponse.build());
					
					// send
					messageSend(channel, responseApiMessage.build());
					
					return;
				}
			};
			service.callMethod(methodDescriptor, controller, methodRequest, callback);
		} catch (InvalidRpcRequestException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RpcException(ex, header, request, "Service threw unexpected exception");
		}
		
	}

	private void OnHeartbeatRequest(ChannelHandlerContext ctx, MessageEvent e,
			ApiHeader header) {
		final Channel channel = e.getChannel();
		final NettyRpcController controller = new NettyRpcController();
		controller.setChannelHandlerContext(ctx);
		controller.setApiHeader(header);
		controller.setApiResponse(ApiResponse.newBuilder());
		
		ApiHeader.Builder responseApiHeader = ApiHeader.newBuilder();
		responseApiHeader.setSeq(controller.getApiHeader().getSeq());
		if (controller.getApiHeader().hasVersion()) {
			responseApiHeader.setVersion(controller.getApiHeader().getVersion());
		}
		if (controller.getApiHeader().hasSid()) {
			responseApiHeader.setSid(controller.getApiHeader().getSid());
		}
		
		responseApiHeader.setType(ApiType.API_TYPE_HEARTBEAT.getNumber());	
//		ApiHeartbeat.Builder responseApiHearbeat = ApiHeartbeat.newBuilder();
		
		// api message
		ApiMessage.Builder responseApiMessage = ApiMessage.newBuilder();
		responseApiMessage.setHeader(responseApiHeader);
		
		// send
		messageSend(channel, responseApiMessage.build());
		
	}

	private void messageSend(Channel channel, ApiMessage build) {
		   if (logger.isDebugEnabled())
		        logger.debug(TextFormat.printToString(build));
			channel.write(build);
		
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
	}
	protected Service getService(String serviceName){
		return defaultServiceMap.get(serviceName);
	}
	public void registerService(Service service) {
		String serviceName = service.getDescriptorForType().getName();
		if(defaultServiceMap.containsKey(serviceName)) {
			throw new IllegalArgumentException("Service already registered");
		}
		defaultServiceMap.put(serviceName, service);
	}
	
	public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		System.err.println("远端关闭连接");
	}


	
}
