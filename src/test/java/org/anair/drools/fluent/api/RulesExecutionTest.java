package org.anair.drools.fluent.api;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anair.drools.model.FiredRulesReturnValues;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.ExecutionResults;
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
		RulesExecution rulesExecution = new RulesExecution(mockStatelessKieSession);
		
		mockStatelessKieSession.addEventListener(isA(AgendaEventListener.class));
		mockStatelessKieSession.execute(Arrays.asList("fact1", "fact2"));
		expectLastCall();
		replay(mockStatelessKieSession);
		FiredRulesReturnValues firedRulesReturnValues = rulesExecution
			.addFacts("fact1", "fact2")
			.auditTrace("target")
			.fireRules(false);
		verify(mockStatelessKieSession);
		assertNull(firedRulesReturnValues.getNumberOfRulesFired());
		assertTrue(firedRulesReturnValues.getFactHandles().isEmpty());
	}
	
	@Test
	public void fireRules_OnStatelessSession_AllConfiguration() {
		mockStatelessKieSession.addEventListener(mockAgendaEventListener);
		mockStatelessKieSession.addEventListener(isA(AgendaEventListener.class));
		mockStatelessKieSession.setGlobal("g1", "g1");
		mockStatelessKieSession.setGlobal("g2", "g2");
		mockStatelessKieSession.execute(Arrays.asList("fact1", "fact2"));
		List<EventListener> eventListeners = new ArrayList<EventListener>();
		eventListeners.add(mockAgendaEventListener);
	
		replay(mockStatelessKieSession, mockAgendaEventListener);
		
		List<Object> factList = new ArrayList<Object>();
		factList.add("fact1");
		factList.add("fact2");
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockStatelessKieSession)
			.addFacts(factList)
			.addGlobal("g1", "g1")
			.addGlobal("g2", "g2")
			.addContext("form", "MD115")
			.addContext("form1", null)
			.addEventListeners(eventListeners)
			.fireRules(false);
		verify(mockStatelessKieSession, mockAgendaEventListener);
		
		assertNull(firedRulesReturnValues.getNumberOfRulesFired());
		assertTrue(firedRulesReturnValues.getFactHandles().isEmpty());
	}
	
	@Test
	public void fireRules_OnStatelessSession_AllConfiguration_disableListeners() {
		mockStatelessKieSession.setGlobal("g1", "g1");
		mockStatelessKieSession.setGlobal("g2", "g2");
		mockStatelessKieSession.execute(Arrays.asList("fact1", "fact2"));
		List<EventListener> eventListeners = new ArrayList<EventListener>();
		eventListeners.add(mockAgendaEventListener);
	
		replay(mockStatelessKieSession);
		
		Map<String,String> contextMap = new HashMap<String, String>();
		contextMap.put("var1", "MD115");
		contextMap.put("var2", null);
		contextMap.put("var3", "    ");
		contextMap.put("var4", "");
		
		List<Object> factList = new ArrayList<Object>();
		factList.add("fact1");
		factList.add("fact2");
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockStatelessKieSession)
			.addFacts(factList)
			.addGlobal("g1", "g1")
			.addGlobal("g2", "g2")
			.addContext(contextMap)
			.addEventListeners(eventListeners)
			.enableListeners(false)
			.fireRules(false);
		verify(mockStatelessKieSession);
		
		assertNull(firedRulesReturnValues.getNumberOfRulesFired());
		assertTrue(firedRulesReturnValues.getFactHandles().isEmpty());
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
		mockKieSession.addEventListener(isA(AgendaEventListener.class));
		expect(mockKieSession.fireAllRules()).andReturn(10);
		replay(mockKieSession, mockFactHandle);
		
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockKieSession)
			.addFacts("fact1", "fact2")
			.fireRules();
		
		verify(mockKieSession, mockFactHandle);
		
		assertEquals(10, firedRulesReturnValues.getNumberOfRulesFired().intValue());
		assertEquals(2, firedRulesReturnValues.getFactHandles().size());
	}
	
	@Test
	public void fireRules_OnStatefulSession_AllConfiguration() {
		mockKieSession.addEventListener(mockAgendaEventListener);
		mockKieSession.addEventListener(mockProcessEventListener);
		mockKieSession.addEventListener(mockRuleRuntimeEventListener);
		mockKieSession.addEventListener(isA(AgendaEventListener.class));
		
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
		
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockKieSession)
			.addFacts("fact1", "fact2")
			.addGlobals(globals)
			.addEventListeners(mockAgendaEventListener, mockProcessEventListener, mockRuleRuntimeEventListener)
			.forAgendaGroups("group1", "group2")
			.fireRules();
		
		verify(mockKieSession, mockFactHandle, mockAgenda, mockAgendaGroup);
		
		assertEquals(10, firedRulesReturnValues.getNumberOfRulesFired().intValue());
		assertEquals(2, firedRulesReturnValues.getFactHandles().size());
	}
	
	@Test
	public void fireRules_OnStatelessSession_BatchExecution_MinimalConfiguration() {
		ExecutionResults mockExectionResults = createMock(ExecutionResults.class);
		expect(mockExectionResults.getValue(RulesExecution.NUMBER_OF_RULES_FIRED)).andReturn(10);
		mockStatelessKieSession.addEventListener(isA(AgendaEventListener.class));
		expect(mockStatelessKieSession.execute(isA(BatchExecutionCommand.class))).andReturn(mockExectionResults);
		replay(mockStatelessKieSession, mockExectionResults);
		
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockStatelessKieSession)
			.addFacts("fact1", "fact2")
			.fireRules();
		
		verify(mockStatelessKieSession, mockExectionResults);
		
		assertEquals(10, firedRulesReturnValues.getNumberOfRulesFired().intValue());
	}
	
	@Test
	public void fireRules_OnStatelessSession_BatchExecution_AllConfiguration() {
		mockStatelessKieSession.addEventListener(mockAgendaEventListener);
		ExecutionResults mockExectionResults = createMock(ExecutionResults.class);
		mockStatelessKieSession.addEventListener(isA(AgendaEventListener.class));
		expect(mockExectionResults.getValue(RulesExecution.NUMBER_OF_RULES_FIRED)).andReturn(10);
		expect(mockStatelessKieSession.execute(isA(BatchExecutionCommand.class))).andReturn(mockExectionResults);
		replay(mockStatelessKieSession, mockExectionResults);
		
		Map<String, Object> globals = new HashMap<String, Object>();
		globals.put("g1", "g1");
		globals.put("g2", "g2");
		
		FiredRulesReturnValues firedRulesReturnValues = new RulesExecution(mockStatelessKieSession)
			.addFacts("fact1", "fact2")
			.addGlobals(globals)
			.addEventListeners(mockAgendaEventListener)
			.forAgendaGroups("group1", "group2")
			.fireRules();
		
		verify(mockStatelessKieSession, mockExectionResults);
		
		assertEquals(10, firedRulesReturnValues.getNumberOfRulesFired().intValue());
	}
	
	@Test(expected=IllegalAccessError.class)
	public void fireRules_OnStatefulSession_NoFact() {
		new RulesExecution(mockKieSession)
			.fireRules();
	}

}
