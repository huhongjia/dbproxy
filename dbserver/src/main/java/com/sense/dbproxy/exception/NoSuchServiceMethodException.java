package com.sense.dbproxy.exception;

import com.sense.dbproxy.api.DbProxy.ApiHeader;
import com.sense.dbproxy.api.DbProxy.ApiNotification;
import com.sense.dbproxy.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class NoSuchServiceMethodException extends RpcException {

	public NoSuchServiceMethodException(ApiHeader header, ApiRequest request, String method) {
		super(header, request, "No such method: " + method);
	}

    public NoSuchServiceMethodException(ApiHeader header, ApiNotification notification, String method) {
        super(header, notification, "No such method: " + method);
    }
}
