package org.anair.drools.provider.session;

import static org.junit.Assert.*;

import org.anair.drools.fluent.api.SessionBuilder;
import org.anair.drools.provider.container.KieContainerProviderImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.*;

import static org.easymock.EasyMock.*;

public class KieSessionProviderImplTest {
	
	private KieSessionProviderImpl kieSessionProvider;
	private KieContainerProviderImpl mockKieContainerProvider;
	private KieContainer mockKieContainer;
	private KieSession mockKieSession;
	private KieContainerSessionsPool mockKieContainerSessionsPool;
	private StatelessKieSession mockStatelessKieSession;
	private static final String RELEASE_ID = "foo:bar:1.0";
	private static final String SESSION_NAME = "kbase.session";
	
	@Before
	public void setUp() {
		mockKieContainerProvider = createMock(KieContainerProviderImpl.class);
		mockKieContainer = createMock(KieContainer.class);
		mockKieContainerSessionsPool = createMock(KieContainerSessionsPool.class);
		mockKieSession = createMock(KieSession.class);
		mockStatelessKieSession = createMock(StatelessKieSession.class);
		
		kieSessionProvider = new KieSessionProviderImpl(mockKieContainerProvider);
	}

	@Test
	public void getStatefulKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSessionsPool(10)).andReturn(mockKieContainerSessionsPool);
		expect(mockKieContainerSessionsPool.newKieSession(SESSION_NAME)).andReturn(mockKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockKieSession, mockKieContainerSessionsPool);
		KieSession actualKieSession = kieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SESSION_NAME, 10);
		verify(mockKieContainerProvider, mockKieContainer, mockKieSession, mockKieContainerSessionsPool);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getDefaultStatefulKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSessionsPool(10)).andReturn(mockKieContainerSessionsPool);
		expect(mockKieContainerSessionsPool.newKieSession(SessionBuilder.DEFAULT_SESSION_NAME)).andReturn(mockKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockKieSession, mockKieContainerSessionsPool);
		KieSession actualKieSession = kieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME, 10);
		verify(mockKieContainerProvider, mockKieContainer, mockKieSession, mockKieContainerSessionsPool);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getStatelessKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSessionsPool(10)).andReturn(mockKieContainerSessionsPool);
		expect(mockKieContainerSessionsPool.newStatelessKieSession(SESSION_NAME)).andReturn(mockStatelessKieSession);
		
		replay(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession, mockKieContainerSessionsPool);
		StatelessKieSession actualKieSession = kieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SESSION_NAME, 10);
		verify(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession, mockKieContainerSessionsPool);
		assertNotNull(actualKieSession);
	}
	
	@Test
	public void getDefaultStatelessKieSession_New() {
		expect(mockKieContainerProvider.getKieContainer(RELEASE_ID, 100)).andReturn(mockKieContainer);
		expect(mockKieContainer.newKieSessionsPool(10)).andReturn(mockKieContainerSessionsPool);
		expect(mockKieContainerSessionsPool.newStatelessKieSession(SessionBuilder.DEFAULT_SESSION_NAME)).andReturn(mockStatelessKieSession);
		replay(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession, mockKieContainerSessionsPool);
		StatelessKieSession actualKieSession = kieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME, 10);
		verify(mockKieContainerProvider, mockKieContainer, mockStatelessKieSession, mockKieContainerSessionsPool);
		assertNotNull(actualKieSession);
	}
	
}
