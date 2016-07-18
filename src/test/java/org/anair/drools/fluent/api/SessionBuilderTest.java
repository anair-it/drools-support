package org.anair.drools.fluent.api;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

public class SessionBuilderTest {
	
	private KieContainer mockKieContainer;
	private KieSession mockKieSession;
	private StatelessKieSession mockStatelessKieSession;
	private static final String RELEASE_ID = "foo:bar:1.0.0";
	private SessionBuilder sessionBuilder;
	
	@Before
	public void setup(){
		mockKieContainer = createMock(KieContainer.class);
		mockKieSession = createMock(KieSession.class);
		mockStatelessKieSession = createMock(StatelessKieSession.class);
		
		sessionBuilder = EasyMock.partialMockBuilder(SessionBuilder.class)
				.withConstructor(String.class).withArgs(RELEASE_ID)
				.addMockedMethod("registerKieContainer", String.class)
				.createMock();
		assertNotNull(sessionBuilder);
		sessionBuilder.kieContainer = mockKieContainer;
	}
	
	@Test
	public void fetchDefaultStatelessKieSession() {
		expect(mockKieContainer.newStatelessKieSession()).andReturn(mockStatelessKieSession);
		
		replay(mockStatelessKieSession, mockKieContainer);
		StatelessKieSession statelessKieSession = sessionBuilder.fetchStatelessKieSession();
		verify(mockStatelessKieSession, mockKieContainer);
		
		assertNotNull(statelessKieSession);
	}
	
	@Test
	public void fetchNamedStatelessKieSession() {
		final String SESSION_NAME = "mysession";
		
		expect(mockKieContainer.newStatelessKieSession(SESSION_NAME)).andReturn(mockStatelessKieSession);
		
		replay(mockKieContainer, mockStatelessKieSession);
		StatelessKieSession statelessKieSession = sessionBuilder
			.fetchStatelessKieSession(SESSION_NAME);
		verify(mockKieContainer, mockStatelessKieSession);
		
		assertNotNull(statelessKieSession);
	}
	
	@Test
	public void fetchNamedStatelessKieSession_NotFound() {
		final String SESSION_NAME = "mysession";
		
		expect(mockKieContainer.newStatelessKieSession(SESSION_NAME)).andReturn(null);
		
		replay(mockKieContainer, mockStatelessKieSession);
		StatelessKieSession statelessKieSession = sessionBuilder
			.fetchStatelessKieSession(SESSION_NAME);
		verify(mockKieContainer, mockStatelessKieSession);
		
		assertNull(statelessKieSession);
	}
	
	@Test
	public void fetchDefaultStatelfulKieSession() {
		expect(mockKieContainer.newKieSession()).andReturn(mockKieSession);
		
		replay(mockKieContainer, mockKieSession);
		KieSession statefulKieSession = sessionBuilder
			.fetchKieSession();
		verify(mockKieContainer, mockKieSession);
		
		assertNotNull(statefulKieSession);
	}
	
	@Test
	public void fetchNamedStatelfulKieSession() {
		final String SESSION_NAME = "mysession";
		
		expect(mockKieContainer.newKieSession(SESSION_NAME)).andReturn(mockKieSession);
		
		replay(mockKieContainer, mockKieSession);
		KieSession statefulKieSession = sessionBuilder
			.fetchKieSession(SESSION_NAME);
		verify(mockKieContainer, mockKieSession);
		
		assertNotNull(statefulKieSession);
	}
	
	@Test
	public void fetchNamedStatelfulKieSession_NotFound() {
		final String SESSION_NAME = "mysession";
		
		expect(mockKieContainer.newKieSession(SESSION_NAME)).andReturn(null);
		
		replay(mockKieContainer, mockKieSession);
		KieSession statefulKieSession = sessionBuilder
			.fetchKieSession(SESSION_NAME);
		verify(mockKieContainer, mockKieSession);
		
		assertNull(statefulKieSession);
	}

}