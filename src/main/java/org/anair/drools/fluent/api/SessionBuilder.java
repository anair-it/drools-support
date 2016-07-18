package org.anair.drools.fluent.api;

import org.apache.commons.lang3.StringUtils;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;


public class SessionBuilder {

	private KieContainer kieContainer;
	
	public SessionBuilder(String groupId, String artifactId, String version) {
		KieServices kieServices = KieServices.Factory.get();
		this.kieContainer = kieServices.newKieContainer(kieServices.newReleaseId(groupId, artifactId, version));
	}
	
	public SessionBuilder(String releaseId) {
		KieServices kieServices = KieServices.Factory.get();
		this.kieContainer = kieServices.newKieContainer(new ReleaseIdImpl(releaseId));
	}

	public KieSession fetchKieSession(String sessionName){
		if(StringUtils.isBlank(sessionName)){
			return this.kieContainer.newKieSession();
		}else{
			return this.kieContainer.newKieSession(sessionName);
		}
	}
	
	public KieSession fetchKieSession(){
		return fetchKieSession(null);
	}
	
	public StatelessKieSession fetchStatelessKieSession(String sessionName){
		if(StringUtils.isBlank(sessionName)){
			return this.kieContainer.newStatelessKieSession();
		}else{
			return this.kieContainer.newStatelessKieSession(sessionName);
		}
	}
	
	public StatelessKieSession fetchStatelessKieSession(){
		return fetchStatelessKieSession(null);
	}
}
