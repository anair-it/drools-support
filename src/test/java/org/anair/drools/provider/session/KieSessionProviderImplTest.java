package org.anair.drools.provider.session;

import static org.junit.Assert.*;

import org.anair.drools.fluent.api.SessionBuilder;
import org.anair.drools.provider.container.KieContainerProviderImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import static org.easymock.EasyMock.*;

public class KieSessionProviderImplTest {
	
	private KieSessionProviderImpl kieSessionProvider;
	private KieContainerProviderImpl mockKieContainerProvider;
	private KieContainer mockKieContainer;
	private KieSession mockKieSession;
	private StatelessKieSession mockStatelessKieSession;
	private static final String RELEASE_ID = "foo:bar:1.0";
	private static final String SESSION_NAME = "kbase.session";
	
	@Before
	public void setUp() throws Exception {
		mockKieContainerProvider = createMock(KieContainerProviderImpl.class);
		mockKieContainer = createMock(KieContainer.class);
		mockKieSession = createMock(KieSession.class);
		mockStatelessKieSession = createMock(StatelessKieSession.class);
		
		kieSessionProvider = new KieSessionProviderImpl(mockKieContainerProvider);
	}

	@Test
	public void getStatefulKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSession(SESSION_NAME)).andReturn(mockKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockKieSession);
		KieSession actualKieSession = kieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SESSION_NAME);
		verify(mockKieContainerProvider, mockKieContainer, mockKieSession);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getDefaultStatefulKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSession(SessionBuilder.DEFAULT_SESSION_NAME)).andReturn(mockKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockKieSession);
		KieSession actualKieSession = kieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME);
		verify(mockKieContainerProvider, mockKieContainer, mockKieSession);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getStatelessKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newStatelessKieSession(SESSION_NAME)).andReturn(mockStatelessKieSession);
		
		replay(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession);
		StatelessKieSession actualKieSession = kieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SESSION_NAME);
		verify(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getDefaultStatelessKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newStatelessKieSession(SessionBuilder.DEFAULT_SESSION_NAME)).andReturn(mockStatelessKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession);
		StatelessKieSession actualKieSession = kieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME);
		verify(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession);
		assertNotNull(actualKieSession);
	}
	
}
