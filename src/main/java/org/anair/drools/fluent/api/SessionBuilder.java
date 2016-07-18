package org.anair.drools.fluent.api;

import org.apache.commons.lang3.StringUtils;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;


public class SessionBuilder {

	protected KieContainer kieContainer;
	
	public SessionBuilder(String groupId, String artifactId, String version) {
		this.kieContainer = registerKieContainer(groupId+":"+artifactId+":"+version);
	}

	public SessionBuilder(String releaseId) {
		this.kieContainer = registerKieContainer(releaseId);
	}

	public KieContainer registerKieContainer(String releaseId) {
		KieServices kieServices = KieServices.Factory.get();
		return kieServices.newKieContainer(new ReleaseIdImpl(releaseId));
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
