package com.sense.dbclient.netty.client;

public interface ConnectionStatusListener
{
	public void onStatusChanged(boolean isConnected, Object channel);
}
