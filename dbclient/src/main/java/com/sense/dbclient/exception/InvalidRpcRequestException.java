package com.sense.dbclient.exception;

import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiNotification;
import com.sense.dbclient.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class InvalidRpcRequestException extends RpcException {

	public InvalidRpcRequestException(Throwable t, ApiHeader header, ApiRequest request, String message) {
		super(t, header, request, message);
	}
    public InvalidRpcRequestException(Throwable t, ApiHeader header, ApiNotification notification, String message) {
        super(t, header, notification, message);
    }
	
	public InvalidRpcRequestException(ApiHeader header, ApiRequest request, String message) {
		super(header, request, message);
	}
    public InvalidRpcRequestException(ApiHeader header, ApiNotification notification, String message) {
        super(header, notification, message);
    }
	
}
