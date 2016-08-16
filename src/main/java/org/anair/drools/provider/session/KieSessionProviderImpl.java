package org.anair.drools.provider.session;

import org.anair.drools.provider.container.KieContainerProvider;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieSessionProviderImpl implements KieSessionProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(KieSessionProviderImpl.class);
	private KieContainerProvider kieContainerProvider;
	
	
	public KieSessionProviderImpl(KieContainerProvider kieContainerProvider) {
		this.kieContainerProvider = kieContainerProvider;
	}

	@Override
	public KieSession getStatefulKieSession(String releaseId, long pollingIntervalMillis, String sessionName) {
		KieContainer kieContainer = kieContainerProvider.getKieContainer(releaseId, pollingIntervalMillis);
		return fetchKieSessionFromContainer(sessionName, kieContainer);
	}

	@Override
	public StatelessKieSession getStatelessKieSession(String releaseId, long pollingIntervalMillis, String sessionName) {
		KieContainer kieContainer = kieContainerProvider.getKieContainer(releaseId, pollingIntervalMillis);
		return fetchStatelessKieSessionFromContainer(sessionName, kieContainer);
	}
	
	private KieSession fetchKieSessionFromContainer(String sessionName, KieContainer kieContainer){
		KieSession kieSession = null;
		if(StringUtils.isBlank(sessionName)){
			LOG.debug("Fetching default Stateful Kie Session...");
			kieSession = kieContainer.newKieSession();
			LOG.debug("Fetched default Stateful Kie Session");
		}else{
			LOG.debug("Fetching Stateful Kie Session : {}...", sessionName);
			kieSession = kieContainer.newKieSession(sessionName);
			LOG.debug("Fetched Stateful Kie Session : {}...", sessionName);
		}
		
		return kieSession;
	}
	
	private StatelessKieSession fetchStatelessKieSessionFromContainer(String sessionName, KieContainer kieContainer){
		StatelessKieSession statelessKieSession =  null;
		
		if(StringUtils.isBlank(sessionName)){
			LOG.debug("Fetching default Stateless Kie Session...");
			statelessKieSession =  kieContainer.newStatelessKieSession();
			LOG.debug("Fetched default Stateless Kie Session");
		}else{
			LOG.debug("Fetching Stateless Kie Session: {}...", sessionName);
			statelessKieSession =  kieContainer.newStatelessKieSession(sessionName);
			LOG.debug("Fetched Stateless Kie Session: {}", sessionName);
		}
		return statelessKieSession;
	}

}
