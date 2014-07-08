package com.sense.dbproxy.netty.server;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class JMXEnabledThreadPoolExecutor extends ThreadPoolExecutor implements
		JMXEnabledThreadPoolExecutorMXBean {

	private String mbeanName;

	// private long maxPendingTaskCountLimit;

	public JMXEnabledThreadPoolExecutor(String name, int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				new NamedThreadFactory(name), handler);
		mbeanName = "com.zhuaiwa.api.server:type=" + name;
		registerMBean();
	}

	@Override
	public long getPendingTaskCount() {
		return getTaskCount() - getCompletedTaskCount();
	}

	// @Override
	// public long getMaxPendingTaskCountLimit() {
	// return maxPendingTaskCountLimit;
	// }
	//
	// @Override
	// public void setMaxPendingTaskCountLimit(long limit) {
	// this.maxPendingTaskCountLimit = limit;
	// }

	@Override
	public synchronized void shutdown() {
		// synchronized, because there is no way to access super.mainLock, which
		// would be
		// the preferred way to make this threadsafe
		if (!isShutdown()) {
			unregisterMBean();
		}
		super.shutdown();
	}

	@Override
	public synchronized List<Runnable> shutdownNow() {
		// synchronized, because there is no way to access super.mainLock, which
		// would be
		// the preferred way to make this threadsafe
		if (!isShutdown()) {
			unregisterMBean();
		}
		return super.shutdownNow();
	}

	private void registerMBean() {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			mbs.registerMBean(this, new ObjectName(mbeanName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void unregisterMBean() {
		try {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(
					new ObjectName(mbeanName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class NamedThreadFactory implements ThreadFactory {

		protected final String id;
		protected final AtomicInteger n = new AtomicInteger(1);

		public NamedThreadFactory(String id) {
			this.id = id;
		}

		@Override
		public Thread newThread(Runnable r) {
			String name = id + "-" + n.getAndIncrement();
			Thread thread = new Thread(r, name);
			thread.setDaemon(true);
			return thread;
		}
	}
}
