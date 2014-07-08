package com.sense.dbproxy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.sense.dbproxy.api.DbProxy;
import com.sense.dbproxy.api.DbProxy.ApiNotification;
import com.sense.dbproxy.api.DbProxy.ApiRequest;
import com.sense.dbproxy.api.DbProxy.ApiResponse;

public class ApiExtensionHelper {
	static private Map<String, GeneratedExtension<ApiRequest, ?>> requests = new HashMap<String, GeneratedExtension<ApiRequest, ?>>();
	static private Map<String, GeneratedExtension<ApiResponse, ?>> responses = new HashMap<String, GeneratedExtension<ApiResponse, ?>>();
	static private Map<String, GeneratedExtension<ApiNotification, ?>> notifications = new HashMap<String, GeneratedExtension<ApiNotification, ?>>();
	
	@SuppressWarnings("unchecked")
	static public <Type> GeneratedExtension<ApiRequest, Type> getRequestByMethodName(String methodName) {
		methodName = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1) +  "Request";
		return (GeneratedExtension<ApiRequest, Type>)requests.get(methodName);
	}
	@SuppressWarnings("unchecked")
	static public <Type> GeneratedExtension<ApiResponse, Type> getResponseByMethodName(String methodName) {
		methodName = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1) +  "Response";
		return (GeneratedExtension<ApiResponse, Type>)responses.get(methodName);
	}
	@SuppressWarnings("unchecked")
	static public <Type> GeneratedExtension<ApiNotification, Type> getNotificationByMethodName(String methodName) {
		methodName = Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1) +  "Notification";
		return (GeneratedExtension<ApiNotification, Type>)notifications.get(methodName);
	}
	
	static public void registerRequest(GeneratedExtension<ApiRequest, ?> extension) {
		extensionRegistry.add(extension);
		requests.put(extension.getDescriptor().getName(), extension);
	}
	static public void registerResponse(GeneratedExtension<ApiResponse, ?> extension) {
		extensionRegistry.add(extension);
		responses.put(extension.getDescriptor().getName(), extension);
	}
	static public void registerNotification(GeneratedExtension<ApiNotification, ?> extension) {
		extensionRegistry.add(extension);
		notifications.put(extension.getDescriptor().getName(), extension);
	}
	
	@SuppressWarnings("unchecked")
	static public void registerProto(FileDescriptor fileDescriptor, Class<?> clazz) {
		for (FieldDescriptor fieldDescriptor : fileDescriptor.getExtensions()) {
			try {
				if (fieldDescriptor.getContainingType().equals(ApiRequest.getDescriptor())) {
					Field field = clazz.getDeclaredField(fieldDescriptor.getName());
					if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
						ApiExtensionHelper.registerRequest((GeneratedExtension<ApiRequest, ?>)field.get(null));
					}
				} else if (fieldDescriptor.getContainingType().equals(ApiResponse.getDescriptor())) {
					Field field = clazz.getDeclaredField(fieldDescriptor.getName());
					if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
						ApiExtensionHelper.registerResponse((GeneratedExtension<ApiResponse, ?>)field.get(null));
					}
				} else if (fieldDescriptor.getContainingType().equals(ApiNotification.getDescriptor())) {
					Field field = clazz.getDeclaredField(fieldDescriptor.getName());
					if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
						ApiExtensionHelper.registerNotification((GeneratedExtension<ApiNotification, ?>)field.get(null));
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	static private ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
	static {
		DbProxy.registerAllExtensions(extensionRegistry);
		registerProto(DbProxy.getDescriptor(), DbProxy.class);
	}
	
	static public ExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}
}

