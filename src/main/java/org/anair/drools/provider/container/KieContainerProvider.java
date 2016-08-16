package org.anair.drools.provider.container;

import org.kie.api.runtime.KieContainer;

/**
 * Get KieContainer using knowledge module release Id
 * Validate KieContainer for ERRORs
 * Add KieContainer to cache
 * Turn on interval based knowledge module scanning if polling interval is > 0 millis
 * 
 * @author anair
 * 
 */
public interface KieContainerProvider {

	KieContainer getKieContainer(String releaseId, long pollingIntervalMillis);
	
}
