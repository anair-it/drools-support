package org.anair.drools.provider.session;

import org.anair.drools.provider.container.KieContainerProvider;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieContainerSessionsPool;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieSessionProviderImpl implements KieSessionProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(KieSessionProviderImpl.class);
	private KieContainerProvider kieContainerProvider;
	
	
	public KieSessionProviderImpl(KieContainerProvider kieContainerProvider) {
		this.kieContainerProvider = kieContainerProvider;
	}

	@Override
	public KieSession getStatefulKieSession(String releaseId, long pollingIntervalMillis, String sessionName, int sessionPoolSize) {
		KieContainer kieContainer = kieContainerProvider.getKieContainer(releaseId, pollingIntervalMillis);
		return fetchKieSessionFromContainer(sessionName, kieContainer, sessionPoolSize);
	}

	@Override
	public StatelessKieSession getStatelessKieSession(String releaseId, long pollingIntervalMillis, String sessionName, int sessionPoolSize) {
		KieContainer kieContainer = kieContainerProvider.getKieContainer(releaseId, pollingIntervalMillis);
		return fetchStatelessKieSessionFromContainer(sessionName, kieContainer, sessionPoolSize);
	}
	
	private KieSession fetchKieSessionFromContainer(String sessionName, KieContainer kieContainer, int sessionPoolSize){
		KieSession kieSession;
		KieContainerSessionsPool sessionsPool = kieContainer.newKieSessionsPool(sessionPoolSize);
		if(StringUtils.isBlank(sessionName)){
			LOG.debug("Fetching default Stateful Kie Session...");
			kieSession = sessionsPool.newKieSession();
			LOG.debug("Fetched default Stateful Kie Session");
		}else{
			LOG.debug("Fetching Stateful Kie Session : {}...", sessionName);
			kieSession = sessionsPool.newKieSession(sessionName);
			LOG.debug("Fetched Stateful Kie Session : {}...", sessionName);
		}
		
		return kieSession;
	}
	
	private StatelessKieSession fetchStatelessKieSessionFromContainer(String sessionName, KieContainer kieContainer, int sessionPoolSize){
		StatelessKieSession statelessKieSession;
		KieContainerSessionsPool sessionsPool = kieContainer.newKieSessionsPool(sessionPoolSize);

		if(StringUtils.isBlank(sessionName)){
			LOG.debug("Fetching default Stateless Kie Session...");
			statelessKieSession =  sessionsPool.newStatelessKieSession();
			LOG.debug("Fetched default Stateless Kie Session");
		}else{
			LOG.debug("Fetching Stateless Kie Session: {}...", sessionName);
			statelessKieSession =  sessionsPool.newStatelessKieSession(sessionName);
			LOG.debug("Fetched Stateless Kie Session: {}", sessionName);
		}
		return statelessKieSession;
	}

}
