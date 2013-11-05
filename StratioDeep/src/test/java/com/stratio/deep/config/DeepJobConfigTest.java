package com.stratio.deep.config;

import static org.testng.Assert.*;

import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.stratio.deep.config.impl.DeepJobConfig;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.exception.DeepIllegalAccessException;

public class DeepJobConfigTest {
	private Logger log = Logger.getLogger(getClass());
	
	@Test
	public void testCorrectInitialisation() {
		IDeepJobConfig<TestEntity> djc = DeepJobConfig.create(TestEntity.class)
				.host("localhost")
				.port(9160)
				.keyspace("test_keyspace")
				.columnFamily("test_cf");

		djc.getConfiguration();
	}

	private <T extends IDeepType> void testMethodCall(IDeepJobConfig<T> config,
			String methodName) {
		try {
			Method m = DeepJobConfig.class.getMethod(methodName);

			m.invoke(config, (Object[]) null);

			fail();
		} catch (InvocationTargetException e) {

			try {
				throw e.getCause();
			} catch (DeepIllegalAccessException diae) {
				// OK
				log.info("Correctly catched DeepIllegalAccessException: "+diae.getLocalizedMessage());
			} catch (Throwable ex) {
				log.error("e", e);
				fail(e.getMessage());
			}

		} catch (Exception e) {
			log.error("e", e);
			fail(e.getMessage());
		}

	}
	
	class NotAnnotatedTestEntity implements IDeepType{
		private static final long serialVersionUID = -2603126590709315326L;
	}
	
	@Test
	public void testValidationNotAnnotadedTestEntity(){
		IDeepJobConfig<NotAnnotatedTestEntity> djc = DeepJobConfig.create(NotAnnotatedTestEntity.class).keyspace("a").columnFamily("cf");
		try {
			djc.validate();
			
			fail();
		} catch (AnnotationTypeMismatchException iae) {
			log.info("Correctly catched AnnotationTypeMismatchException: "+iae.getLocalizedMessage());
		} catch (Exception e){
			fail(e.getMessage());
		}
	}

	@Test
	public void testValidation() {
		IDeepJobConfig<TestEntity> djc = DeepJobConfig.create(TestEntity.class);

		djc.host(null).port(null);
		
		testMethodCall(djc, "getKeyspace");
		testMethodCall(djc, "getHost");
		testMethodCall(djc, "getPort");
		testMethodCall(djc, "getUsername");
		testMethodCall(djc, "getPassword");
		testMethodCall(djc, "getColumnFamily");
		testMethodCall(djc, "getEntityClass");
		
		try {
			djc.validate();
			fail();
		} catch (IllegalArgumentException iae) {
			// OK
			log.info("Correctly catched IllegalArgumentException: "+iae.getLocalizedMessage());
		} catch (Exception e){
			fail(e.getMessage());
		}
		
		djc.host("localhost");
		
		try {
			djc.validate();
			fail();
		} catch (IllegalArgumentException iae) {
			// OK
			log.info("Correctly catched IllegalArgumentException: "+iae.getLocalizedMessage());
		} catch (Exception e){
			fail(e.getMessage());
		}
		
		djc.port(9160);
		
		try {
			djc.validate();
			fail();
		} catch (IllegalArgumentException iae) {
			// OK
			log.info("Correctly catched IllegalArgumentException: "+iae.getLocalizedMessage());
		} catch (Exception e){
			fail(e.getMessage());
		}
		
		djc.keyspace("test_keyspace");
		
		try {
			djc.validate();
			fail();
		} catch (IllegalArgumentException iae) {
			// OK
			log.info("Correctly catched IllegalArgumentException: "+iae.getLocalizedMessage());
		} catch (Exception e){
			fail(e.getMessage());
		}
		
		djc.columnFamily("columnFamily");
		
		djc.validate();
			
	}
}
