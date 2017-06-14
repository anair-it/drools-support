package org.anair.drools.provider.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.anair.rules.exception.RulesSupportRuntimeException;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieContainerProviderImpl implements KieContainerProvider {
	private static Logger LOG = LoggerFactory.getLogger(KieContainerProviderImpl.class);
	private final Map<ReleaseId, KieContainer> kieContainerCache = new ConcurrentHashMap<ReleaseId, KieContainer>();
	private KieServices kieServices;
	
	@Override
	public KieContainer getKieContainer(String releaseId, long pollingIntervalMillis) {
		ReleaseId releaseIdObj = new ReleaseIdImpl(releaseId);
		
		if (!kieContainerCache.containsKey(releaseIdObj)) {
			if(this.kieServices == null){
				this.kieServices = KieServices.Factory.get();
			}
			LOG.debug("Kie Container not found in cache. Acquiring...");
			KieContainer kieContainer = this.createKieContainer(releaseIdObj);
			if(kieContainer == null){
				throw new RuntimeException("Kie Container not found for knowledge module: {}");
			}
			LOG.debug("Acquired Kie Container");
			validateKieContainer(kieContainer);
			
			kieContainerCache.put(releaseIdObj, kieContainer);
			
			if(pollingIntervalMillis > 0){
				LOG.debug("Acquiring Kie Scanner...");
				startScan(kieServices.newKieScanner(kieContainer), pollingIntervalMillis);
			}
			return kieContainer;
		}
		
		return kieContainerCache.get(releaseIdObj);
	}

	private KieContainer createKieContainer(ReleaseId releaseId) {
		return kieServices.newKieContainer(releaseId);
	}
	
	private void validateKieContainer(KieContainer kieContainer) {
		LOG.debug("Validating Kie Container");
		Results results = kieContainer.verify();
		if(results.hasMessages(Level.ERROR)){
			String errorMessageConcat = results.getMessages(Level.ERROR).stream()
					.map(Message::toString)
					.collect(Collectors.joining(":"));
			LOG.error("Found ERRORs while validating Kie Container: {}", errorMessageConcat);
			throw new RulesSupportRuntimeException(errorMessageConcat);
		}else{
			LOG.debug("No ERRORs found while Validating Kie Container");
		}
	}
	
	private void startScan(KieScanner kieScanner, long pollingIntervalMillis){
		LOG.debug("Kie component scan starts every {}", pollingIntervalMillis);
		kieScanner.start(pollingIntervalMillis);
		LOG.debug("Kie component scan done");
	}

	public void setKieServices(KieServices kieServices) {
		this.kieServices = kieServices;
	}
	
}
