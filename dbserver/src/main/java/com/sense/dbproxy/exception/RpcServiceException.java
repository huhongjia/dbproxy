package com.sense.dbproxy.exception;

import com.google.protobuf.ServiceException;
import com.sense.dbproxy.api.DbProxy.ApiHeader;
import com.sense.dbproxy.api.DbProxy.ApiRequest;

@SuppressWarnings("serial")
public class RpcServiceException extends RpcException {

	public RpcServiceException(ServiceException serviceException, ApiHeader header, ApiRequest request, String message) {
		super(serviceException, header, request, message);
	}
}

