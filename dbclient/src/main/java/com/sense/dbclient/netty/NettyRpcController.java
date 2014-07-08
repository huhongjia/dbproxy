package com.sense.dbclient.netty;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiRequest;
import com.sense.dbclient.api.DbProxy.ApiResponse;
import com.sense.dbclient.api.DbProxy.SSResultCode;

public class NettyRpcController implements RpcController {

	private int code = SSResultCode.RC_OK.getNumber();
	private String reason;
	private boolean failed;
	private boolean canceled;
	private RpcCallback<Object> callback;
	
	public ApiHeader getApiHeader() {
		return apiHeader;
	}

	public void setApiHeader(ApiHeader apiHeader) {
		this.apiHeader = apiHeader;
	}

	public ApiRequest getApiRequest() {
		return apiRequest;
	}

	public void setApiRequest(ApiRequest apiRequest) {
		this.apiRequest = apiRequest;
	}

	public ApiResponse.Builder getApiResponse() {
		return apiResponse;
	}

	public void setApiResponse(ApiResponse.Builder apiResponse) {
		this.apiResponse = apiResponse;
	}

	private ApiHeader apiHeader;
	private ApiRequest apiRequest;
	private ApiResponse.Builder apiResponse;
	
	private ChannelHandlerContext channelHandlerContext;
	
	public String errorText() {
		return reason;
	}

	public boolean failed() {
		return failed;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void notifyOnCancel(RpcCallback<Object> callback) {
		this.callback = callback;
		if (canceled && this.callback != null) {
			this.callback.run(null);
		}
	}

	public void reset() {
		reason = null;
		failed = false;
		canceled = false;
		callback = null;
	}

	public void setFailed(String reason) {
		this.reason = reason;
		this.failed = true;
	}

	public void startCancel() {
		canceled = true;
		if (this.callback != null) {
            this.callback.run(null);
        }
	}

	public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}

}
