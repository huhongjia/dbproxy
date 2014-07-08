package com.sense.dbproxy.exception;

import com.sense.dbproxy.api.DbProxy.ApiHeader;
import com.sense.dbproxy.api.DbProxy.ApiNotification;
import com.sense.dbproxy.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class NoSuchServiceException extends RpcException {

	public NoSuchServiceException(ApiHeader header, ApiRequest request, String serviceName) {
		super(header, request, "No such service name: " + serviceName);
	}
	public NoSuchServiceException(ApiHeader header, ApiNotification notification, String serviceName) {
        super(header, notification, "No such service name: " + serviceName);
    }
}