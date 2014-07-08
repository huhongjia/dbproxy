package com.sense.dbclient.exception;

import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiNotification;
import com.sense.dbclient.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class NoSuchServiceException extends RpcException {

	public NoSuchServiceException(ApiHeader header, ApiRequest request, String serviceName) {
		super(header, request, "No such service name: " + serviceName);
	}
	public NoSuchServiceException(ApiHeader header, ApiNotification notification, String serviceName) {
        super(header, notification, "No such service name: " + serviceName);
    }
}