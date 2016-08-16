package org.anair.drools.fluent.api;

import org.anair.drools.provider.session.KieSessionProvider;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;


/**
 * Helper class to fetch Stateful/Stateless Kie Session based on Knowledge module release Id, session name
 * 
 * @author anair
 *
 */
public class SessionBuilder {
	
	public static final String DEFAULT_SESSION_NAME = "DEFAULT";
	private KieSessionProvider kieSessionProvider;
	private long pollingIntervalMillis = 0;
	private String releaseId;
	
	public SessionBuilder(KieSessionProvider kieSessionProvider) {
		this.kieSessionProvider = kieSessionProvider;
	}
	
	public SessionBuilder forKnowledgeModule(String groupId, String artifactId, String version) {
		return forKnowledgeModule(groupId+":"+artifactId+":"+version);
	}

	public SessionBuilder forKnowledgeModule(String releaseId) {
		this.releaseId = releaseId;
		return this;
	}
	
	public SessionBuilder pollingIntervalMillis(long pollingIntervalMillis) {
		this.pollingIntervalMillis = pollingIntervalMillis;
		return this;
	}
	
	public KieSession fetchKieSession(String sessionName){
		return this.kieSessionProvider.getStatefulKieSession(releaseId, pollingIntervalMillis, sessionName);
	}
	
	public KieSession fetchKieSession(){
		return fetchKieSession(DEFAULT_SESSION_NAME);
	}
	
	public StatelessKieSession fetchStatelessKieSession(String sessionName){
		return this.kieSessionProvider.getStatelessKieSession(releaseId, pollingIntervalMillis, sessionName);
	}
	
	public StatelessKieSession fetchStatelessKieSession(){
		return fetchStatelessKieSession(DEFAULT_SESSION_NAME);
	}
	
}
