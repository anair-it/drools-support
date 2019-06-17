package org.anair.drools.fluent.api;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import org.anair.drools.provider.session.KieSessionProvider;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

public class SessionBuilderTest {
	
	private KieSessionProvider mockKieSessionProvider;
	private KieSession mockKieSession;
	private StatelessKieSession mockStatelessKieSession;
	private static final String RELEASE_ID = "foo:bar:1.0.0";
	private static final String SESSION_NAME = "kbase.session";
	
	
	@Before
	public void setup(){
		mockKieSessionProvider = createMock(KieSessionProvider.class);
		mockKieSession = createMock(KieSession.class);
		mockStatelessKieSession = createMock(StatelessKieSession.class);
	}
	
	@Test
	public void fetchDefaultStatelessKieSessionWithPollingInterval() {
		expect(mockKieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME, 10)).andReturn(mockStatelessKieSession);
		replay(mockKieSessionProvider, mockStatelessKieSession);
		StatelessKieSession statelessKieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
				.sessionPoolSize(10)
			.fetchStatelessKieSession();
		verify(mockKieSessionProvider, mockStatelessKieSession);
		
		assertNotNull(statelessKieSession);
	}
	
	@Test
	public void fetchNamedStatelessKieSession() {
		expect(mockKieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SESSION_NAME, 10)).andReturn(mockStatelessKieSession);
		
		replay(mockKieSessionProvider, mockStatelessKieSession);
		StatelessKieSession statelessKieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
			.fetchStatelessKieSession(SESSION_NAME);
		verify(mockKieSessionProvider, mockStatelessKieSession);
		
		assertNotNull(statelessKieSession);
	}
	
	@Test
	public void fetchNamedStatelessKieSession_NotFound() {
		expect(mockKieSessionProvider.getStatelessKieSession(RELEASE_ID, 100, SESSION_NAME, 20)).andReturn(null);
		
		replay(mockKieSessionProvider);
		StatelessKieSession statelessKieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
				.sessionPoolSize(20)
			.fetchStatelessKieSession(SESSION_NAME);
		verify(mockKieSessionProvider);
		
		assertNull(statelessKieSession);
	}
	
	@Test
	public void fetchDefaultStatelfulKieSession() {
		expect(mockKieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SessionBuilder.DEFAULT_SESSION_NAME, 10)).andReturn(mockKieSession);
		
		replay(mockKieSessionProvider, mockKieSession);
		KieSession kieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
			.fetchKieSession();
		verify(mockKieSessionProvider, mockKieSession);
		
		assertNotNull(kieSession);
	}
	
	@Test
	public void fetchNamedStatelfulKieSession() {
		expect(mockKieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SESSION_NAME, 10)).andReturn(mockKieSession);
		
		replay(mockKieSessionProvider, mockKieSession);
		KieSession kieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
			.fetchKieSession(SESSION_NAME);
		verify(mockKieSessionProvider, mockKieSession);
		
		assertNotNull(kieSession);
	}
	
	@Test
	public void fetchNamedStatelfulKieSession_NotFound() {
		expect(mockKieSessionProvider.getStatefulKieSession(RELEASE_ID, 100, SESSION_NAME, 10)).andReturn(null);
		
		replay(mockKieSessionProvider);
		KieSession kieSession = new SessionBuilder(mockKieSessionProvider)
			.forKnowledgeModule(RELEASE_ID)
			.pollingIntervalMillis(100)
			.fetchKieSession(SESSION_NAME);
		verify(mockKieSessionProvider);
		
		assertNull(kieSession);
	}

}