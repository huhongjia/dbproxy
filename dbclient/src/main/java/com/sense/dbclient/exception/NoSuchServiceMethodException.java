package com.sense.dbclient.exception;

import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiNotification;
import com.sense.dbclient.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class NoSuchServiceMethodException extends RpcException {

	public NoSuchServiceMethodException(ApiHeader header, ApiRequest request, String method) {
		super(header, request, "No such method: " + method);
	}

    public NoSuchServiceMethodException(ApiHeader header, ApiNotification notification, String method) {
        super(header, notification, "No such method: " + method);
    }
}
