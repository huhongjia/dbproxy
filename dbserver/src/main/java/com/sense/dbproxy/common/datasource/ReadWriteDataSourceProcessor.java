package com.sense.dbproxy.common.datasource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.NestedRuntimeException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.ReflectionUtils;


public class ReadWriteDataSourceProcessor implements BeanPostProcessor {
    private static final Logger log =Logger.getLogger(ReadWriteDataSourceProcessor.class);
    
    private boolean forceChoiceReadWhenWrite = false;
    
    private Map<String, Boolean> readMethodMap = new HashMap<String, Boolean>();


    
    public void setForceChoiceReadWhenWrite(boolean forceChoiceReadWhenWrite) {
        
        this.forceChoiceReadWhenWrite = forceChoiceReadWhenWrite;
    }
    

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if(!(bean instanceof NameMatchTransactionAttributeSource)) {
            return bean;
        }
        
        try {
            NameMatchTransactionAttributeSource transactionAttributeSource = (NameMatchTransactionAttributeSource)bean;
            Field nameMapField = ReflectionUtils.findField(NameMatchTransactionAttributeSource.class, "nameMap");
            nameMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
			Map<String, TransactionAttribute> nameMap = (Map<String, TransactionAttribute>) nameMapField.get(transactionAttributeSource);
            
            for(Entry<String, TransactionAttribute> entry : nameMap.entrySet()) {
                RuleBasedTransactionAttribute attr = (RuleBasedTransactionAttribute)entry.getValue();

                if(!attr.isReadOnly()) {
                    continue;
                }
                
                String methodName = entry.getKey();
                Boolean isForceChoiceRead = Boolean.FALSE;
                if(forceChoiceReadWhenWrite) {
                    attr.setPropagationBehavior(Propagation.NOT_SUPPORTED.value());
                    isForceChoiceRead = Boolean.TRUE;
                } else {
                    attr.setPropagationBehavior(Propagation.SUPPORTS.value());
                }
                log.debug("read/write transaction process  method:{} force read:{}"+methodName+isForceChoiceRead);
                readMethodMap.put(methodName, isForceChoiceRead);
            }
            
        } catch (Exception e) {
            throw new ReadWriteDataSourceTransactionException("process read/write transaction error", e);
        }
        
        return bean;
    }
    
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @SuppressWarnings("serial")
	private class ReadWriteDataSourceTransactionException extends NestedRuntimeException {
        public ReadWriteDataSourceTransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    
    
    
    
    public Object determineReadOrWriteDB(ProceedingJoinPoint pjp) throws Throwable {
        
    	System.out.println("aop qie ku");
    	System.out.println(pjp.getSignature().getName());
    	
        if (isChoiceReadDB(pjp.getSignature().getName())) {
            ReadWriteDataSourceDecision.markRead();
        } else {
            ReadWriteDataSourceDecision.markWrite();
        }
            
        try {
            return pjp.proceed();
        } finally {
            ReadWriteDataSourceDecision.reset();
        }
        
        
    }
    
    private boolean isChoiceReadDB(String methodName) {

        String bestNameMatch = null;
        for (String mappedName : this.readMethodMap.keySet()) {
            if (isMatch(methodName, mappedName)) {
                bestNameMatch = mappedName;
                break;
            }
        }

        Boolean isForceChoiceRead = readMethodMap.get(bestNameMatch);
        if(isForceChoiceRead == Boolean.TRUE) {
            return true;
        }
        
        if(ReadWriteDataSourceDecision.isChoiceWrite()) {
            return false;
        }
        
        if(isForceChoiceRead != null) {
            return true;
        }
        return false;
    }


    protected boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

}

