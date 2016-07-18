package org.anair.drools.test.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.anair.drools.test.annotation.StatelessKSession;
import org.apache.commons.lang3.StringUtils;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.cdi.KBase;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.cdi.KSession;
import org.kie.api.event.rule.AfterMatchFiredEvent;
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

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		String releaseId = extractReleaseId(testContext);
		KieServices kieServices = KieServices.Factory.get();
		this.kieContainer = kieServices.newKieContainer(new ReleaseIdImpl(releaseId));
		
		Field[] declaredFields = testContext.getTestClass().getDeclaredFields();
		for(Field declaredField: declaredFields){
			Annotation[] fieldAnns = declaredField.getAnnotations();
			for(Annotation fieldAnn:fieldAnns){
				if(fieldAnn.annotationType().getName().equals(KBase.class.getName())){
					registerKieBase(testContext, (KBase) fieldAnn);
				}else if(fieldAnn.annotationType().getName().equals(KSession.class.getName())){
					registerStatefulKieSession(testContext, (KSession)fieldAnn);
				}else if(fieldAnn.annotationType().getName().equals(StatelessKSession.class.getName())){
					registerStatelessKieSession(testContext, (StatelessKSession)fieldAnn);
				}
			}
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		this.kieContainer = null;
		testContext.markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
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
			Assert.notNull(kieBase);
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
			Assert.notNull(statelessKieSession, "Stateless Kie session should be present");
			registerSpringBean(testContext.getApplicationContext(), statelessKSessionAnnValue, statelessKieSession);
		}
	}

	private String extractReleaseId(TestContext testContext) {
		Assert.isTrue(testContext.getTestClass().isAnnotationPresent(KReleaseId.class));
		KReleaseId kReleaseId = testContext.getTestClass().getAnnotation(KReleaseId.class);
		String releaseId = kReleaseId.groupId()+":"+kReleaseId.artifactId()+":"+kReleaseId.version();
		Assert.notNull(releaseId);
		return releaseId;
	}
	
	private void registerSpringBean(ApplicationContext applicationContext, String name, Object object){
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
		ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();
		beanFactory.registerSingleton(name, object);
		
	}
	
	private StatelessKieSession getDefaultStatelessKieSession(){
		StatelessKieSession statelessKieSession = this.kieContainer.newStatelessKieSession();
		registerEventListener(statelessKieSession);
		return statelessKieSession;
	}
	
	private StatelessKieSession getStatelessKieSessionFor(String kieSessionName){
		StatelessKieSession statelessKieSession = this.kieContainer.newStatelessKieSession(kieSessionName);
		registerEventListener(statelessKieSession);
		return statelessKieSession;
	}
	
	private KieSession getDefaultStatefulKieSession(){
		KieSession statefulKieSession = this.kieContainer.newKieSession();
		registerEventListener(statefulKieSession);
		return statefulKieSession;
	}
	
	private KieSession getStatefulKieSessionFor(String kieSessionName){
		KieSession statefulKieSession = this.kieContainer.newKieSession(kieSessionName);
		registerEventListener(statefulKieSession);
		return statefulKieSession;
	}
	
	private void registerEventListener(Object session){
		if (session instanceof KieSession){
			((KieSession) session).addEventListener(new DefectReportAgendaListener());
		}else{
			((StatelessKieSession) session).addEventListener(new DefectReportAgendaListener());
		}
	}
	
	private class DefectReportAgendaListener extends DefaultAgendaEventListener {
		private Logger LOG = LoggerFactory.getLogger(DefectReportAgendaListener.class);

		@Override
	    public void afterMatchFired(AfterMatchFiredEvent event) {
			LOG.info("-> Rule: " + event.getMatch().getRule().getName());
	    }
	}
}