package org.anair.drools.fluent.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anair.drools.model.FiredRulesReturnValues;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.drools.core.management.GenericKieSessionMonitoringImpl.AgendaStats;
import org.drools.core.management.GenericKieSessionMonitoringImpl.AgendaStats.AgendaStatsData;
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
 * Builder class to prepare objects for rules execution and to fire rules.
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
	private Map<String, Object> globals = new HashMap<>();
	private String[] agendaGroupNames;
	private Collection<EventListener> eventListeners;
	private KieSession kieSession;
	private StatelessKieSession statelessKieSession;
	private AgendaStats agendaStats = new AgendaStats();
	private boolean enableListeners = true;
	private KieRuntimeLogger logger = null;
	private Map<String, String> appContext = new HashMap<>();
	private FiredRulesReturnValues firedRulesReturnValues;
	
	public RulesExecution(KieSession kieSession){
		this();
		this.kieSession = kieSession;
	}
	
	public RulesExecution(StatelessKieSession statelessKieSession){
		this();
		this.statelessKieSession = statelessKieSession;
	}

	private RulesExecution(){
		this.firedRulesReturnValues = new FiredRulesReturnValues();
		if(RULES_LOG.isInfoEnabled()){
			agendaStats = new AgendaStats();
			agendaStats.reset();
			registerDefaultEventListeners(agendaStats);
		}
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
					this.facts = new ArrayList<>(); 
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
			this.eventListeners = new ArrayList<>();
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
		try{
			if(RULES_LOG.isInfoEnabled()){
				auditLoggingContext();
			}
			fireRules(true);
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
		if(RULES_LOG.isInfoEnabled() && MapUtils.isNotEmpty(this.appContext)){
			appContext.forEach((key,value) -> MDC.remove(key));
		}
	}

	private void auditLoggingContext() {
		if(MapUtils.isNotEmpty(this.appContext)){
			appContext.forEach((key,value) -> MDC.put(key, StringUtils.defaultIfBlank(value, StringUtils.SPACE)));
		}
	}
	
	public FiredRulesReturnValues fireRules(boolean enableBatchExecution){
		if(CollectionUtils.isEmpty(facts)){
			throw new IllegalAccessError("Cannot fire rules without facts. Set atleast 1 fact");
		}
		
		if(! enableListeners){
			this.eventListeners.clear();
		}
		
		if(this.kieSession != null){
			fireKieSessionRules();
		}else if(this.statelessKieSession != null){
			if(enableBatchExecution){
				fireStatelessKieSessionRulesBatchExecution();	
			}else{
				fireStatelessKieSessionRules();
			}
		}
		return firedRulesReturnValues;
	}
	
	private void fireKieSessionRules() {
		LOG.trace("Preparing to fire rules on a Stateful Kie Session...");
		
		this.globals.forEach((k,v)->this.kieSession.setGlobal(k, v));

		this.facts.forEach(fact->{
			FactHandle factHandle = this.kieSession.insert(fact);
			firedRulesReturnValues.addFactHandle(factHandle);
		});
		
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
		
		if(ArrayUtils.isNotEmpty(agendaGroupNames)){
			for(int i=agendaGroupNames.length;i>0;i--){
				this.kieSession.getAgenda().getAgendaGroup(agendaGroupNames[i-1]).setFocus();
			}
		}
		
		StopWatch sw = new StopWatch();
		if(LOG.isDebugEnabled()){
			sw.start();
		}
		int numberOfRulesFired = this.kieSession.fireAllRules();
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired {} rules on a Stateful Kie Session. Execution time: {}", numberOfRulesFired, sw.toString());
		}
		firedRulesReturnValues.setNumberOfRulesFired(numberOfRulesFired);
	}
	
	private void fireStatelessKieSessionRules(){
		LOG.trace("Preparing to fire rules on a Stateless Kie Session...");
		
		this.globals.forEach((k,v)->this.statelessKieSession.setGlobal(k, v));
		
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
		
		StopWatch sw = new StopWatch();
		if(LOG.isDebugEnabled()){
			sw.start();
		}
		this.statelessKieSession.execute(facts);
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired rules on a Stateless Kie Session. Execution time: {}", sw.toString());
		}
	}
	
	private void fireStatelessKieSessionRulesBatchExecution(){
		LOG.trace("Preparing to fire rules on a Stateless Kie Session - batch command mode...");
		
		KieServices kieServices = getKieServices();
		@SuppressWarnings("rawtypes")
		List<Command> commands = new ArrayList<Command>();
		
		this.globals.forEach((k,v)->commands.add(kieServices.getCommands().newSetGlobal(k, v, true)));
		
		this.facts.forEach(fact->{
			commands.add(kieServices.getCommands().newInsert(fact));
		});
		
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
		
		if(ArrayUtils.isNotEmpty(agendaGroupNames)){
			for(int i=agendaGroupNames.length;i>0;i--){
				kieServices.getCommands().newAgendaGroupSetFocus(agendaGroupNames[i-1]);
			}
		}
		
		commands.add(kieServices.getCommands().newFireAllRules(NUMBER_OF_RULES_FIRED));
		
		StopWatch sw = new StopWatch();
		if(LOG.isDebugEnabled()){
			sw.start();
		}
		
		ExecutionResults executionResults = statelessKieSession.execute(kieServices.getCommands().newBatchExecution(commands));
		firedRulesReturnValues.setExecutionResults(executionResults);
		firedRulesReturnValues.setNumberOfRulesFired((Integer)executionResults.getValue(NUMBER_OF_RULES_FIRED));
		
		if(LOG.isDebugEnabled()){
			sw.stop();
			LOG.debug("Fired {} rules on a Stateless Kie Session in batch mode. Execution time: {}", firedRulesReturnValues.getNumberOfRulesFired(), sw.toString());
		}
	}
	
	private void registerDefaultEventListeners(EventListener... eventListeners){
		this.addEventListeners(eventListeners);
	}
	
	private void printDefaultAgendaStats(){
		if(RULES_LOG.isInfoEnabled()){
			for(Map.Entry<String, AgendaStatsData> entry: this.agendaStats.getRulesStats().entrySet()){
				RULES_LOG.info("{} | {}", entry.getKey(), entry.getValue());
				firedRulesReturnValues.getExecutedRules().add(entry.getKey());
			}
		}
	}
}
