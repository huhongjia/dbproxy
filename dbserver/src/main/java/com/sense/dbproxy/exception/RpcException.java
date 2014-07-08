package com.sense.dbproxy.exception;

import com.sense.dbproxy.api.DbProxy.ApiHeader;
import com.sense.dbproxy.api.DbProxy.ApiNotification;
import com.sense.dbproxy.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class RpcException extends Exception {
	
	private final ApiHeader header;
	private final ApiRequest request;
	private final ApiNotification notification;
	
	public RpcException(Throwable t, ApiHeader header, ApiRequest request, String message) {
		this(header, request, message);
		initCause(t);
	}
	
    public RpcException(Throwable t, ApiHeader header, ApiNotification notification, String message) {
        this(header, notification, message);
        initCause(t);
    }
	
	public RpcException(ApiHeader header, ApiRequest request, String message) {
		super(message);
		this.header = header;
		this.request = request;
        this.notification = null;
	}
    
    public RpcException(ApiHeader header, ApiNotification notification, String message) {
        super(message);
        this.header = header;
        this.request = null;
        this.notification = notification;
    }

    public ApiNotification getNotification() {
        return notification;
    }

	public ApiRequest getRequest() {
		return request;
	}

	public ApiHeader getHeader() {
		return header;
	}
	
}
