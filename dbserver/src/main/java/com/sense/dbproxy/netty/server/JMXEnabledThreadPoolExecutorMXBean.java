package com.sense.dbproxy.netty.server;


public interface JMXEnabledThreadPoolExecutorMXBean {
    public int getActiveCount();
    
    public long getTaskCount();
    public long getCompletedTaskCount();
    public long getPendingTaskCount();
    
    public int getCorePoolSize();
    public void setCorePoolSize(int corePoolSize);
    
    public int getMaximumPoolSize();
    public void setMaximumPoolSize(int maximumPoolSize);
}

