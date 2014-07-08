package com.sense.dbclient.netty.client;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.sense.dbclient.api.DbProxy.ApiResponse;
import com.sense.dbclient.api.DbProxy.SSResultCode;
import com.sense.dbclient.netty.NettyRpcController;
import com.sense.dbclient.util.ApiExtensionHelper;

public class ResponsePrototypeRpcCallback implements RpcCallback<ApiResponse>
{

	private ApiResponse apiResponse;

	private final RpcCallback<Message> callback;

	private final NettyRpcController controller;

	private final MethodDescriptor method;

	private final Message responsePrototype;

	public ResponsePrototypeRpcCallback(MethodDescriptor method,
			RpcController controller, Message responsePrototype,
			RpcCallback<Message> callback)
	{
		if (responsePrototype == null)
		{
			throw new IllegalArgumentException(
					"Must provide response prototype");
		}
		else if (callback == null)
		{
			throw new IllegalArgumentException("Must provide callback");
		}
		this.controller = (NettyRpcController) controller;
		this.method = method;
		this.responsePrototype = responsePrototype;
		this.callback = callback;
	}

	public ApiResponse getApiResponse()
	{
		return apiResponse;
	}

	public NettyRpcController getRpcController()
	{
		return controller;
	}

	@Override
	public void run(ApiResponse message)
	{
		apiResponse = message;
		try
		{
			if (message == null)
			{
				callback.run(null);
				return;
			}

			if (apiResponse.getCode() != SSResultCode.RC_OK.getNumber())
			{
				if (controller != null)
				{
					controller.setCode(apiResponse.getCode());
					if (apiResponse.hasReason())
					{
						controller.setFailed(apiResponse.getReason());
					}
				}
				callback.run(responsePrototype.newBuilderForType().build());
				return;
			}

			Message response = responsePrototype.newBuilderForType()
					// .mergeFrom(apiResponse.getPayload())
					.mergeFrom(
							apiResponse.getExtension(ApiExtensionHelper
									.<Message> getResponseByMethodName(method
											.getName()))).build();
			callback.run(response);
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (controller != null)
			{
				controller
						.setFailed("Received invalid response type from server");
			}
			callback.run(responsePrototype.newBuilderForType().build());
			return;
		}
	}

}

