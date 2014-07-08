package com.sense.dbproxy.util;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.jmx.HierarchyDynamicMBean;

public class JMXLog4jConfigurator {
    public static void config() {

        try {
            HierarchyDynamicMBean hdm = new HierarchyDynamicMBean();
            ManagementFactory.getPlatformMBeanServer()
            .registerMBean(hdm, new ObjectName("log4j:hierarchy=default"));
            
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        } catch (NotCompliantMBeanException e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        } catch (Exception e) {
            throw new RuntimeException("Can't register log4j to jmx.", e);
        }
    }
}

