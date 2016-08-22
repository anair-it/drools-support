package org.anair.drools.fluent.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anair.drools.model.FiredRulesReturnValues;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.drools.core.management.KieSessionMonitoringImpl.AgendaStats;
import org.drools.core.management.KieSessionMonitoringImpl.AgendaStats.AgendaStatsData;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Helper class to prepare objects for rules execution and to fire rules.
 * 
 * @author anair
 *
 */
public class RulesExecution {
	protected static final String DEFAULT_RULES_TRACE_FILENAME = "/rules-trace";
	protected static final String NUMBER_OF_RULES_FIRED = "numberOfRulesFired";
	private static final String RULES_LOGGER_NAME = "rules-audit";

	private static Logger LOG = LoggerFactory.getLogger(RulesExecution.class);
	private static Logger RULES_LOG = LoggerFactory.getLogger(RULES_LOGGER_NAME);
		
	private Collection<Object> facts;
	private Map<String, Object> globals = new HashMap<String, Object>();
	private String[] agendaGroupNames;
	private Collection<EventListener> eventListeners;
	private KieSession kieSession;
	private StatelessKieSession statelessKieSession;
	private AgendaStats agendaStats = new AgendaStats();
	private boolean enableListeners = true;
	private KieRuntimeLogger logger = null;
	private Map<String, String> appContext = new HashMap<String, String>();
	
	public RulesExecution(KieSession kieSession){
		this.kieSession = kieSession;
		registerDefaultEventListeners(agendaStats);
	}
	
	public RulesExecution(StatelessKieSession statelessKieSession){
		this.statelessKieSession = statelessKieSession;
		registerDefaultEventListeners(agendaStats);
	}
	
	public RulesExecution auditTrace(String auditFilePath){
		if(LOG.isTraceEnabled()){
			auditFilePath += DEFAULT_RULES_TRACE_FILENAME;
			
			if(this.kieSession != null){
				this.logger = getKieServices().getLoggers().newFileLogger(this.kieSession, auditFilePath);	
			}else{
				this.logger = getKieServices().getLoggers().newFileLogger(this.statelessKieSession, auditFilePath);
			}
		}
		return this;
	}

	public KieServices getKieServices() {
		return KieServices.Factory.get();
	}
	
	@SuppressWarnings("unchecked")
	public RulesExecution addFacts(Object... facts){
		for(Object fact: facts){
			if(fact instanceof Collection){
				if(this.facts == null){
					this.facts = (Collection<Object>)fact;
				}else{
					this.facts.addAll((Collection<Object>)fact);
				}
			}else{
				if(this.facts == null){
					this.facts = new ArrayList<Object>(); 
				}
				this.facts.add(fact);
			}
		}
		return this;
	}
	
	public RulesExecution addContext(Map<String, String> context){
		this.appContext.putAll(context);
		return this;
	}
	
	public RulesExecution addContext(final String contextName, String contextValue){
		this.appContext.put(contextName, contextValue);
		return this;
	}
	
	public RulesExecution enableListeners(boolean enableListeners){
		this.enableListeners = enableListeners;
		return this;
	}
	
	public RulesExecution addEventListeners(EventListener... eventListeners){
		if(this.eventListeners == null){
			this.eventListeners = new ArrayList<EventListener>();
		}
		this.eventListeners.addAll(Arrays.asList(eventListeners));
		return this;
	}
	
	public RulesExecution addEventListeners(Collection<EventListener> eventListeners){
		if(this.eventListeners == null){
			this.eventListeners = eventListeners;
		}else{
			this.eventListeners.addAll(eventListeners);
		}
		return this;
	}
	
	public RulesExecution addGlobals(Map<String, Object> globals){
		this.globals.putAll(globals);
		return this;
	}
	
	public RulesExecution addGlobal(String globalVariable, Object globalObject){
		this.globals.put(globalVariable, globalObject);
		return this;
	}
	
	public RulesExecution forAgendaGroups(String... agendaGroupNames){
		this.agendaGroupNames = agendaGroupNames;
		return this;
	}
	
	public FiredRulesReturnValues fireRules(){
		FiredRulesReturnValues firedRulesReturnValues = null;
		try{
			if(RULES_LOG.isInfoEnabled()){
				auditLoggingContext();
			}
			firedRulesReturnValues = fireRules(true);
		}finally{
			fireRulesPostProcessor();
		}
		
		return firedRulesReturnValues;
	}

	private void fireRulesPostProcessor() {
		printDefaultAgendaStats();
		if(this.logger != null){
			this.logger.close();
		}
		if(RULES_LOG.isInfoEnabled()){
			if(this.appContext != null && ! appContext.isEmpty()){
				for(String key: appContext.keySet()){
					MDC.remove(key);
				}
			}
		}
	}

	private void auditLoggingContext() {
		if(appContext != null && ! appContext.isEmpty()){
			for(Map.Entry<String, String> entry: appContext.entrySet()){
				MDC.put(entry.getKey(), StringUtils.defaultIfBlank(entry.getValue(), StringUtils.SPACE));
			}
		}
	}
	
	public FiredRulesReturnValues fireRules(boolean enableBatchExecution){
		FiredRulesReturnValues firedRulesReturnValues = new FiredRulesReturnValues();
		if(this.facts ==  null || this.facts.isEmpty()){
			throw new IllegalAccessError("Cannot fire rules without facts. Set atleast 1 fact");
		}
		
		if(! enableListeners){
			this.eventListeners.clear();
		}
		
		if(this.kieSession != null){
			firedRulesReturnValues = fireKieSessionRules();
		}else if(this.statelessKieSession != null){
			if(enableBatchExecution){
				firedRulesReturnValues = fireStatelessKieSessionRulesBatchExecution();	
			}else{
				fireStatelessKieSessionRules();
			}
		}
		
		return firedRulesReturnValues;
	}
	
	private FiredRulesReturnValues fireKieSessionRules() {
		LOG.trace("Preparing to fire rules on a Stateful Kie Session...");
		
		FiredRulesReturnValues firedRulesReturnValues = new FiredRulesReturnValues();
		
		for(Map.Entry<String, Object> entry: this.globals.entrySet()){
			this.kieSession.setGlobal(entry.getKey(), entry.getValue());
		}
		for(Object fact: this.facts){
			FactHandle factHandle = this.kieSession.insert(fact);
			firedRulesReturnValues.addFactHandle(factHandle);
		}
		
		if(this.eventListeners != null){
			for(EventListener eventListener: this.eventListeners){
				if(eventListener instanceof RuleRuntimeEventListener){
					this.kieSession.addEventListener((RuleRuntimeEventListener) eventListener);
				}else if(eventListener instanceof AgendaEventListener){
					this.kieSession.addEventListener((AgendaEventListener) eventListener);
				}else if(eventListener instanceof ProcessEventListener){
					this.kieSession.addEventListener((ProcessEventListener) eventListener);
				}
			}
		}
		
		if(agendaGroupNames != null && agendaGroupNames.length > 0){
			for(String agendaGroupName: this.agendaGroupNames){
				this.kieSession.getAgenda().getAgendaGroup(agendaGroupName).setFocus();
			}
		}
		
		StopWatch sw = null;
		if(LOG.isDebugEnabled()){
			sw = new StopWatch();
			sw.start();
		}
		int numberOfRulesFired = this.kieSession.fireAllRules();
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired {} rules on a Stateful Kie Session. Execution time: {}", numberOfRulesFired, sw.toString());
		}
		firedRulesReturnValues.setNumberOfRulesFired(numberOfRulesFired);
		
		return firedRulesReturnValues;
	}
	
	private void fireStatelessKieSessionRules(){
		LOG.trace("Preparing to fire rules on a Stateless Kie Session...");
		
		for(Map.Entry<String, Object> entry: this.globals.entrySet()){
			this.statelessKieSession.setGlobal(entry.getKey(), entry.getValue());
		}
		
		if(this.eventListeners != null){
			for(EventListener eventListener: this.eventListeners){
				if(eventListener instanceof RuleRuntimeEventListener){
					this.statelessKieSession.addEventListener((RuleRuntimeEventListener) eventListener);
				}else if(eventListener instanceof AgendaEventListener){
					this.statelessKieSession.addEventListener((AgendaEventListener) eventListener);
				}else if(eventListener instanceof ProcessEventListener){
					this.statelessKieSession.addEventListener((ProcessEventListener) eventListener);
				}
			}
		}
		
		StopWatch sw = null;
		if(LOG.isDebugEnabled()){
			sw = new StopWatch();
			sw.start();
		}
		this.statelessKieSession.execute(facts);
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired rules on a Stateless Kie Session. Execution time: {}", sw.toString());
		}
	}
	
	private FiredRulesReturnValues fireStatelessKieSessionRulesBatchExecution(){
		LOG.trace("Preparing to fire rules on a Stateless Kie Session - batch command mode...");
		
		FiredRulesReturnValues firedRulesReturnValues = new FiredRulesReturnValues();
		KieServices kieServices = getKieServices();
		@SuppressWarnings("rawtypes")
		List<Command> commands = new ArrayList<Command>();
		
		for(Map.Entry<String, Object> entry: this.globals.entrySet()){
			commands.add(kieServices.getCommands().newSetGlobal(entry.getKey(), entry.getValue(), true));
		}
		
		for(Object fact: this.facts){
			commands.add(kieServices.getCommands().newInsert(fact));
		}
		
		if(this.eventListeners != null){
			for(EventListener eventListener: this.eventListeners){
				if(eventListener instanceof RuleRuntimeEventListener){
					this.statelessKieSession.addEventListener((RuleRuntimeEventListener) eventListener);
				}else if(eventListener instanceof AgendaEventListener){
					this.statelessKieSession.addEventListener((AgendaEventListener) eventListener);
				}else if(eventListener instanceof ProcessEventListener){
					this.statelessKieSession.addEventListener((ProcessEventListener) eventListener);
				}
			}
		}
		
		if(agendaGroupNames != null && agendaGroupNames.length > 0){
			for(String agendaGroupName: this.agendaGroupNames){
				kieServices.getCommands().newAgendaGroupSetFocus(agendaGroupName);
			}
		}
		
		commands.add(kieServices.getCommands().newFireAllRules(NUMBER_OF_RULES_FIRED));
		
		StopWatch sw = null;
		if(LOG.isDebugEnabled()){
			sw = new StopWatch();
			sw.start();
		}
		
		ExecutionResults executionResults = statelessKieSession.execute(kieServices.getCommands().newBatchExecution(commands));
		firedRulesReturnValues.setExecutionResults(executionResults);
		firedRulesReturnValues.setNumberOfRulesFired((Integer)executionResults.getValue(NUMBER_OF_RULES_FIRED));
		
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired {} rules on a Stateless Kie Session in batch mode. Execution time: {}", firedRulesReturnValues.getNumberOfRulesFired(), sw.toString());
		}
		
		return firedRulesReturnValues;
	}
	
	private void registerDefaultEventListeners(EventListener... eventListeners){
		this.addEventListeners(eventListeners);
	}
	
	private void printDefaultAgendaStats(){
		if(RULES_LOG.isInfoEnabled()){
			RULES_LOG.info(this.agendaStats.getConsolidatedStats().toString());
			for(Map.Entry<String, AgendaStatsData> entry: this.agendaStats.getRulesStats().entrySet()){
				RULES_LOG.info("Rule -> {} | Stats -> {}", entry.getKey(), entry.getValue());
			}
		}
	}
}
