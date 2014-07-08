package com.sense.dbproxy.common.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ReadWriteDataSource extends AbstractDataSource implements InitializingBean {
    private static final Logger log = Logger.getLogger(ReadWriteDataSource.class);
    
    private DataSource writeDataSource;
    private Map<String, DataSource> readDataSourceMap;
    
    
    private String[] readDataSourceNames;
    private DataSource[] readDataSources;
    private int readDataSourceCount;

    private AtomicInteger counter = new AtomicInteger(1);

    public void setReadDataSourceMap(Map<String, DataSource> readDataSourceMap) {
        this.readDataSourceMap = readDataSourceMap;
    }
    public void setWriteDataSource(DataSource writeDataSource) {
        this.writeDataSource = writeDataSource;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        if(writeDataSource == null) {
            throw new IllegalArgumentException("property 'writeDataSource' is required");
        }
        if(CollectionUtils.isEmpty(readDataSourceMap)) {
            throw new IllegalArgumentException("property 'readDataSourceMap' is required");
        }
        readDataSourceCount = readDataSourceMap.size();
        
        readDataSources = new DataSource[readDataSourceCount];
        readDataSourceNames = new String[readDataSourceCount];
        
        int i = 0;
        for(Entry<String, DataSource> e : readDataSourceMap.entrySet()) {
            readDataSources[i] = e.getValue();
            readDataSourceNames[i] = e.getKey();
            i++;
        }
        
        
    }
    
    
    private DataSource determineDataSource() {
        if(ReadWriteDataSourceDecision.isChoiceWrite()) {
            log.debug("current determine write datasource");
            return writeDataSource;
        }
        
        if(ReadWriteDataSourceDecision.isChoiceNone()) {
            log.debug("no choice read/write, default determine write datasource");
            return writeDataSource;
        } 
        return determineReadDataSource();
    }
    
    private DataSource determineReadDataSource() {
        int index = counter.incrementAndGet() % readDataSourceCount;
        if(index < 0) {
            index = - index;
        }
            
        String dataSourceName = readDataSourceNames[index];
        
        log.debug("current determine read datasource : {}"+dataSourceName);

        return readDataSources[index];
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return determineDataSource().getConnection();
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
