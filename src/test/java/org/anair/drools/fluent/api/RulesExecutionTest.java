package org.anair.drools.fluent.api;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaGroup;
import org.kie.api.runtime.rule.FactHandle;

public class RulesExecutionTest {
	
	private KieSession mockKieSession;
	private StatelessKieSession mockStatelessKieSession;
	private FactHandle mockFactHandle;
	private Agenda mockAgenda;
	private AgendaGroup mockAgendaGroup;
	private AgendaEventListener mockAgendaEventListener;
	private ProcessEventListener mockProcessEventListener;
	private RuleRuntimeEventListener mockRuleRuntimeEventListener;
	
	
	@Before
	public void setUp() throws Exception {
		mockKieSession = createMock(KieSession.class);
		mockStatelessKieSession = createMock(StatelessKieSession.class);
		mockFactHandle = createMock(FactHandle.class);
		mockAgenda = createMock(Agenda.class);
		mockAgendaGroup = createMock(AgendaGroup.class);
		mockAgendaEventListener = createMock(AgendaEventListener.class);
		mockProcessEventListener = createMock(ProcessEventListener.class);
		mockRuleRuntimeEventListener = createMock(RuleRuntimeEventListener.class);
	}

	@Test
	public void fireRules_OnStatelessSession_MinimalConfiguration() {
		mockStatelessKieSession.execute(Arrays.asList("fact1", "fact2"));
		expectLastCall();
		replay(mockStatelessKieSession);
		
		new RulesExecution(mockStatelessKieSession)
			.addFacts("fact1", "fact2")
			.fireRules();
		verify(mockStatelessKieSession);
	}
	
	@Test
	public void fireRules_OnStatelessSession_AllConfiguration() {
		mockStatelessKieSession.addEventListener(mockAgendaEventListener);
		mockStatelessKieSession.setGlobal("g1", "g1");
		mockStatelessKieSession.setGlobal("g2", "g2");
		mockStatelessKieSession.execute(Arrays.asList("fact1", "fact2"));
		List<EventListener> eventListeners = new ArrayList<EventListener>();
		eventListeners.add(mockAgendaEventListener);
	
		replay(mockStatelessKieSession, mockAgendaEventListener);
		
		new RulesExecution(mockStatelessKieSession)
			.addFacts("fact1", "fact2")
			.addGlobal("g1", "g1")
			.addGlobal("g2", "g2")
			.addEventListeners(eventListeners)
			.fireRules();
		verify(mockStatelessKieSession, mockAgendaEventListener);
	}
	
	@Test(expected=IllegalAccessError.class)
	public void fireRules_OnStatelessSession_NoFact() {
		new RulesExecution(mockStatelessKieSession)
			.fireRules();
	}
	
	@Test
	public void fireRules_OnStatefulSession_MinimalConfiguration() {
		expect(mockKieSession.insert("fact1")).andStubReturn(mockFactHandle);
		expect(mockKieSession.insert("fact2")).andStubReturn(mockFactHandle);
		expect(mockKieSession.fireAllRules()).andReturn(10);
		replay(mockKieSession, mockFactHandle);
		
		new RulesExecution(mockKieSession)
			.addFacts("fact1", "fact2")
			.fireRules();
		
		verify(mockKieSession, mockFactHandle);
	}
	
	@Test
	public void fireRules_OnStatefulSession_AllConfiguration() {
		mockKieSession.addEventListener(mockAgendaEventListener);
		mockKieSession.addEventListener(mockProcessEventListener);
		mockKieSession.addEventListener(mockRuleRuntimeEventListener);
		
		mockKieSession.setGlobal("g1", "g1");
		mockKieSession.setGlobal("g2", "g2");
		
		expect(mockKieSession.getAgenda()).andReturn(mockAgenda).times(2);
		expect(mockAgenda.getAgendaGroup("group1")).andStubReturn(mockAgendaGroup);
		expect(mockAgenda.getAgendaGroup("group2")).andStubReturn(mockAgendaGroup);
		mockAgendaGroup.setFocus();
		expectLastCall().times(2);
		
		expect(mockKieSession.insert("fact1")).andStubReturn(mockFactHandle);
		expect(mockKieSession.insert("fact2")).andStubReturn(mockFactHandle);
		expect(mockKieSession.fireAllRules()).andReturn(10);
		replay(mockKieSession, mockFactHandle, mockAgenda, mockAgendaGroup);
		
		Map<String, Object> globals = new HashMap<String, Object>();
		globals.put("g1", "g1");
		globals.put("g2", "g2");
		
		int rulesFiredCount = new RulesExecution(mockKieSession)
			.addFacts("fact1", "fact2")
			.addGlobals(globals)
			.addEventListeners(mockAgendaEventListener, mockProcessEventListener, mockRuleRuntimeEventListener)
			.forAgendaGroups("group1", "group2")
			.fireRules();
		
		verify(mockKieSession, mockFactHandle, mockAgenda, mockAgendaGroup);
		
		assertEquals(rulesFiredCount, 10);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fireRules_WithAgendaGroup_WithStatelessKieSession() {
		
		new RulesExecution(mockStatelessKieSession)
			.addFacts("fact1", "fact2")
			.forAgendaGroups("group1", "group2")
			.fireRules();
		
	}
	
	@Test(expected=IllegalAccessError.class)
	public void fireRules_OnStatefulSession_NoFact() {
		new RulesExecution(mockKieSession)
			.fireRules();
	}

}
