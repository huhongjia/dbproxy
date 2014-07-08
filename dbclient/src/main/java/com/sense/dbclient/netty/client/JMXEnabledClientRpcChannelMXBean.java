package com.sense.dbclient.netty.client;

public interface JMXEnabledClientRpcChannelMXBean {
    public long getRpcCount();
    public long getPendingCount();
    public boolean isConnected();
    public boolean isUsed();
    public boolean isEnabled();
    public void enable();
    public void disable();
}
