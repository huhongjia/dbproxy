package com.sense.dbclient.exception;

import com.google.protobuf.ServiceException;
import com.sense.dbclient.api.DbProxy.ApiHeader;
import com.sense.dbclient.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class RpcServiceException extends RpcException {

	public RpcServiceException(ServiceException serviceException, ApiHeader header, ApiRequest request, String message) {
		super(serviceException, header, request, message);
	}
}

