package com.sense.dbclient.netty.client;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.netty.bootstrap.ClientBootstrap;

public class JMXEnabledClientRpcChannel extends ClientRpcChannel implements
		JMXEnabledClientRpcChannelMXBean {
	private final static AtomicLong sed = new AtomicLong(0);

	private boolean enabled = true;

	private final String mbeanName;

	public JMXEnabledClientRpcChannel(ClientBootstrap bootstrap,
			InetSocketAddress remoteAddress) {
		this(bootstrap, remoteAddress, null);
	}

	public JMXEnabledClientRpcChannel(ClientBootstrap bootstrap,
			InetSocketAddress remoteAddress,
			ConnectionStatusListener connectionStatusListener) {
		super(bootstrap, remoteAddress, connectionStatusListener);
		mbeanName = "com.zhuaiwa.api.client:type="
				+ getRemoteAddress().toString().replace(":", "@") + "@"
				+ sed.incrementAndGet();
		registerMBean();
	}

	@Override
	public void close() {
		super.close();
		unregisterMBean();
	}

	@Override
	public void disable() {
		this.enabled = false;
	}

	@Override
	public void enable() {
		this.enabled = true;
	}

	@Override
	public long getPendingCount() {
		return handler.getPendingCount();
	}

	@Override
	public long getRpcCount() {
		return handler.getSeqId();
	}

	@Override
	public boolean isConnected() {
		return super.isConnected();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
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
}
