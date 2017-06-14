package org.anair.drools.provider.container;

import static org.junit.Assert.*;

import org.anair.rules.exception.RulesSupportRuntimeException;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;

import static org.easymock.EasyMock.*;

public class KieContainerProviderImplTest {
	
	private KieContainerProviderImpl kieContainerProvider;
	private KieContainer mockKieContainer;
	private KieScanner mockKieScanner;
	private KieServices mockKieServices;
	private static final String RELEASE_ID = "foo:bar:1.0";
	
	@Before
	public void setUp() throws Exception {
		mockKieContainer = createMock(KieContainer.class);
		mockKieScanner = createMock(KieScanner.class);
		mockKieServices = createMock(KieServices.class);
		
		kieContainerProvider = new KieContainerProviderImpl();
		kieContainerProvider.setKieServices(mockKieServices);
	}

	@Test
	public void getKieContainer_New_NoPolling_NoValidationError() {
		expect(mockKieServices.newKieContainer(isA(ReleaseId.class))).andReturn(mockKieContainer);
		expect(mockKieContainer.verify()).andReturn(new ResultsImpl());
		replay(mockKieContainer, mockKieServices);
		
		KieContainer actualKieContainer = kieContainerProvider.getKieContainer(RELEASE_ID, 0);
		verify(mockKieContainer, mockKieServices);
		
		assertNotNull(actualKieContainer);
	}
	
	@Test
	public void getKieContainer_UseCache_NoPolling_NoValidationError() {
		expect(mockKieServices.newKieContainer(isA(ReleaseId.class))).andReturn(mockKieContainer);
		expect(mockKieContainer.verify()).andReturn(new ResultsImpl());
		replay(mockKieContainer, mockKieServices);
		
		KieContainer actualKieContainer = kieContainerProvider.getKieContainer(RELEASE_ID, 0);
		verify(mockKieContainer, mockKieServices);
		assertNotNull(actualKieContainer);
		
		//Call 2nd time to fetch KieContainer from cache
		actualKieContainer = kieContainerProvider.getKieContainer(RELEASE_ID, 0);
		assertNotNull(actualKieContainer);
	}
	
	@Test(expected=RulesSupportRuntimeException.class)
	public void getKieContainer_New_NoPolling_ValidationError() {
		expect(mockKieServices.newKieContainer(isA(ReleaseId.class))).andReturn(mockKieContainer);
		
		ResultsImpl results = new ResultsImpl();
		results.addMessage(Level.ERROR, "path", "error message");
		expect(mockKieContainer.verify()).andReturn(results);
		replay(mockKieContainer, mockKieServices);
		
		kieContainerProvider.getKieContainer(RELEASE_ID, 0);
		verify(mockKieContainer, mockKieServices);
	}
	
	@Test
	public void getKieContainer_New_Polling_NoValidationError() {
		expect(mockKieServices.newKieContainer(isA(ReleaseId.class))).andReturn(mockKieContainer);
		expect(mockKieContainer.verify()).andReturn(new ResultsImpl());
		expect(mockKieServices.newKieScanner(mockKieContainer)).andReturn(mockKieScanner);
		mockKieScanner.start(10);
		replay(mockKieContainer, mockKieServices, mockKieScanner);
		
		KieContainer actualKieContainer = kieContainerProvider.getKieContainer(RELEASE_ID, 10);
		verify(mockKieContainer, mockKieServices, mockKieScanner);
		
		assertNotNull(actualKieContainer);
	}

}
