package org.anair.drools.fluent.api;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;


public class SessionBuilder {

	private KieContainer kieContainer;
	private KieServices kieServices;
	
	public SessionBuilder() {
		this.kieServices = KieServices.Factory.get();
	}

	public SessionBuilder withReleaseId(String groupId, String artifactId, String version){
		this.kieContainer = this.kieServices.newKieContainer(this.kieServices.newReleaseId(groupId, artifactId, version));
		return this;
	}
	
	public KieSession fetchKieSession(String sessionName){
		if(StringUtils.isBlank(sessionName)){
			return this.kieContainer.newKieSession();
		}else{
			return this.kieContainer.newKieSession(sessionName);
		}
	}
	
	public StatelessKieSession fetchStatelessKieSession(String sessionName){
		if(StringUtils.isBlank(sessionName)){
			return this.kieContainer.newStatelessKieSession();
		}else{
			return this.kieContainer.newStatelessKieSession(sessionName);
		}
	}
}
