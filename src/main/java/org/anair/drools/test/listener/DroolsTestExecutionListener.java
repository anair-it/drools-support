package org.anair.drools.test.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.stream.Collectors;

import org.anair.drools.test.annotation.EventListeners;
import org.anair.drools.test.annotation.StatelessKSession;
import org.anair.rules.exception.RulesSupportRuntimeException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.Results;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.cdi.KSession;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;



public class DroolsTestExecutionListener extends DependencyInjectionTestExecutionListener {
	private static Logger LOG = LoggerFactory.getLogger(DroolsTestExecutionListener.class);
	private KieContainer kieContainer;
	private KieRuntimeLogger logger = null;
	private KieServices kieServices;
	
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		String[] releaseId = extractReleaseId(testContext);
		this.kieServices = KieServices.Factory.get();
		this.kieContainer = kieServices.newKieContainer(kieServices.newReleaseId(releaseId[0], releaseId[1], releaseId[2]));
		
		validateKieContainer(this.kieContainer.verify());
		
		Field[] declaredFieldsParent = testContext.getTestClass().getSuperclass().getDeclaredFields();
		Field[] declaredFieldsTestClass = testContext.getTestClass().getDeclaredFields();
		Field[] declaredFields = ArrayUtils.addAll(declaredFieldsParent, declaredFieldsTestClass);
		
		for(Field declaredField: declaredFields){
			Annotation[] fieldAnns = declaredField.getAnnotations();
			for(Annotation fieldAnn:fieldAnns){
				if(fieldAnn instanceof KBase){
					registerKieBase(testContext, (KBase) fieldAnn);
				}else if(fieldAnn instanceof KSession){
					registerStatefulKieSession(testContext, (KSession)fieldAnn);
				}else if(fieldAnn instanceof StatelessKSession){
					registerStatelessKieSession(testContext, (StatelessKSession)fieldAnn);
				}
			}
		}
	}

	public void validateKieContainer(Results results) {
		if(results.hasMessages(Level.ERROR)){
			String errorMessageConcat = results.getMessages(Level.ERROR).stream()
				.map(Message::toString)
				.collect(Collectors.joining(" : "));
			throw new RulesSupportRuntimeException(errorMessageConcat);
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		this.kieContainer = null;
		this.kieServices = null;
		testContext.markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
		
		if(logger != null){
			logger.close();
		}
	}
	
	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		LOG.info("\n"+testContext.getTestMethod().getName());
		LOG.info("=================");
	}
	
	private void registerKieBase(TestContext testContext, KBase kBaseAnn) {
		if(kBaseAnn != null){
			KieBase kieBase = null;
			if(StringUtils.isBlank(kBaseAnn.value())){
				kieBase = this.kieContainer.getKieBase();	
			}else{
				kieBase = this.kieContainer.getKieBase(kBaseAnn.value().trim());
			}
			Assert.notNull(kieBase, "KieBase must be present");
			registerSpringBean(testContext.getApplicationContext(), kBaseAnn.value().trim(), kieBase);
		}
	}
	
	private void registerStatefulKieSession(TestContext testContext, KSession kSessionAnn) {
		if(kSessionAnn != null){	
			KieSession kieSession = null;
			String kSessionAnnValue = kSessionAnn.value();
			if(StringUtils.isBlank(kSessionAnnValue)){
				kieSession = getDefaultStatefulKieSession();
				kSessionAnnValue = "kieSession";
			}else{
				kSessionAnnValue = kSessionAnnValue.trim();
				kieSession = getStatefulKieSessionFor(kSessionAnnValue);
			}
			
			eventListenerProcessor(testContext, kieSession);
			
			Assert.notNull(kieSession, "Stateful Kie session should be present");
			registerSpringBean(testContext.getApplicationContext(), kSessionAnnValue, kieSession);
		}
	}
	
	private void registerStatelessKieSession(TestContext testContext, StatelessKSession statelessKSessionAnn) {
		if(statelessKSessionAnn != null){	
			StatelessKieSession statelessKieSession = null;
			String statelessKSessionAnnValue = statelessKSessionAnn.value();
			if(StringUtils.isBlank(statelessKSessionAnnValue)){
				statelessKieSession = getDefaultStatelessKieSession();
				statelessKSessionAnnValue = "statelessKieSession";
			}else{
				statelessKSessionAnnValue = statelessKSessionAnnValue.trim();
				statelessKieSession = getStatelessKieSessionFor(statelessKSessionAnnValue);
			}
			
			eventListenerProcessor(testContext, statelessKieSession);
			
			Assert.notNull(statelessKieSession, "Stateless Kie session should be present");
			registerSpringBean(testContext.getApplicationContext(), statelessKSessionAnnValue, statelessKieSession);
		}
	}

	public void eventListenerProcessor(TestContext testContext, Object session) {
		EventListeners eventListeners = extractEventListenersContext(testContext);
		if(eventListeners.enabled()){
			if(StringUtils.isNotBlank(eventListeners.auditlogFileName())){
				logger = kieServices.getLoggers().newFileLogger(session instanceof KieSession?(KieSession)session:(StatelessKieSession)session, "target/"+eventListeners.auditlogFileName());
			}
		}
	}
	
	private String[] extractReleaseId(TestContext testContext) {
		KReleaseId kReleaseId;
		if(testContext.getTestClass().isAnnotationPresent(KReleaseId.class)){
			kReleaseId = testContext.getTestClass().getAnnotation(KReleaseId.class);
			
		}else {
			Assert.isTrue(testContext.getTestClass().getSuperclass().isAnnotationPresent(KReleaseId.class), "KReleaseId annotation is a must");
			kReleaseId = testContext.getTestClass().getSuperclass().getAnnotation(KReleaseId.class);
		}
		Assert.notNull(kReleaseId.groupId(), "Group id must be present");
		Assert.notNull(kReleaseId.artifactId(), "Artifact id must be present");
		Assert.notNull(kReleaseId.version(), "Version must be present");
		return new String[]{kReleaseId.groupId(), kReleaseId.artifactId(), kReleaseId.version()};
	}
	
	private EventListeners extractEventListenersContext(TestContext testContext){
		EventListeners eventListeners = testContext.getTestClass().getAnnotation(EventListeners.class);
		if(eventListeners != null){
			return eventListeners;
		}else{
			eventListeners = new EventListeners() {
				
				@Override
				public Class<? extends Annotation> annotationType() {
					return null;
				}
				
				@Override
				public boolean enabled() {
					return true;
				}
				
				@Override
				public String auditlogFileName() {
					return "rules-trace";
				}
			};
		}
		return eventListeners;
	}
	
	private void registerSpringBean(ApplicationContext applicationContext, String name, Object object){
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
		ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();
		beanFactory.registerSingleton(name, object);
	}
	
	private StatelessKieSession getDefaultStatelessKieSession(){
		return this.kieContainer.newStatelessKieSession();
	}
	
	private StatelessKieSession getStatelessKieSessionFor(String kieSessionName){
		return this.kieContainer.newStatelessKieSession(kieSessionName);
	}
	
	private KieSession getDefaultStatefulKieSession(){
		return this.kieContainer.newKieSession();
	}
	
	private KieSession getStatefulKieSessionFor(String kieSessionName){
		return this.kieContainer.newKieSession(kieSessionName);
	}
	
}