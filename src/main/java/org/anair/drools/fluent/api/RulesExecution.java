package org.anair.drools.fluent.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

public class RulesExecution {
		
	private List<Object> facts = new ArrayList<Object>();
	private Map<String, Object> globals = new HashMap<String, Object>();
	private String[] agendaGroupNames;
	private List<EventListener> eventListeners = new ArrayList<EventListener>();
	private KieSession kieSession;
	private StatelessKieSession statelessKieSession;
	
	
	public RulesExecution(KieSession kieSession) {
		this.kieSession = kieSession;
	}
	
	public RulesExecution(StatelessKieSession statelessKieSession) {
		this.statelessKieSession = statelessKieSession;
	}

	public RulesExecution addFacts(Object... facts){
		this.facts.addAll(Arrays.asList(facts));
		return this;
	}
	
	public RulesExecution addFacts(List<Object> facts){
		this.facts.addAll(facts);
		return this;
	}
	
	public RulesExecution addEventListeners(EventListener... eventListeners){
		this.eventListeners.addAll(Arrays.asList(eventListeners));
		return this;
	}
	
	public RulesExecution addEventListeners(List<EventListener> eventListeners){
		this.eventListeners.addAll(eventListeners);
		return this;
	}
	
	public RulesExecution addGlobals(Map<String, Object> globals){
		this.globals.putAll(globals);
		return this;
	}
	
	public RulesExecution addGlobal(String globalVariable, Object gloablObject){
		this.globals.put(globalVariable, gloablObject);
		return this;
	}
	
	public RulesExecution forAgendaGroups(String... agendaGroupNames){
		if(this.kieSession == null){
			throw new IllegalArgumentException("Cannot set Agenda group without a Kie Session");
		}
		this.agendaGroupNames = agendaGroupNames;
		return this;
	}
	
	public int fireRules(){
		if(this.kieSession != null){
			return fireKieSessionRules();
		}else if(this.statelessKieSession != null){
			fireStatelessKieSessionRules();
		}
		
		return 0;
	}

	private int fireKieSessionRules() {
		for(Map.Entry<String, Object> entry: globals.entrySet()){
			kieSession.setGlobal(entry.getKey(), entry.getValue());
		}
		for(Object fact: facts){
			kieSession.insert(fact);
		}
		
		for(EventListener eventListener: eventListeners){
			if(eventListener instanceof RuleRuntimeEventListener){
				kieSession.addEventListener((RuleRuntimeEventListener) eventListener);
			}else if(eventListener instanceof AgendaEventListener){
				kieSession.addEventListener((AgendaEventListener) eventListener);
			}else if(eventListener instanceof ProcessEventListener){
				kieSession.addEventListener((ProcessEventListener) eventListener);
			}
		}
		
		for(String agendaGroupName: this.agendaGroupNames){
			kieSession.getAgenda().getAgendaGroup(agendaGroupName).setFocus();
		}
		return kieSession.fireAllRules();
	}
	
	private void fireStatelessKieSessionRules(){
		for(Map.Entry<String, Object> entry: globals.entrySet()){
			this.statelessKieSession.setGlobal(entry.getKey(), entry.getValue());
		}
		
		for(EventListener eventListener: eventListeners){
			if(eventListener instanceof RuleRuntimeEventListener){
				this.statelessKieSession.addEventListener((RuleRuntimeEventListener) eventListener);
			}else if(eventListener instanceof AgendaEventListener){
				this.statelessKieSession.addEventListener((AgendaEventListener) eventListener);
			}else if(eventListener instanceof ProcessEventListener){
				this.statelessKieSession.addEventListener((ProcessEventListener) eventListener);
			}
		}
		
		this.statelessKieSession.execute(facts);
	}
}
