package org.anair.drools.provider.session;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

/**
 * Get KieSession/StatelessKeySession by session Name
 * Add session to cache
 * 
 * @author anair
 * 
 */
public interface KieSessionProvider {

	KieSession getStatefulKieSession(String releaseId, long pollingIntervalMillis, String sessionName);
	StatelessKieSession getStatelessKieSession(String releaseId, long pollingIntervalMillis, String sessionName);
}
