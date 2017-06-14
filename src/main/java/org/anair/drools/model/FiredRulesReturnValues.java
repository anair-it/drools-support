package org.anair.drools.model;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.FactHandle;

public class FiredRulesReturnValues {
	
	private List<FactHandle> factHandles = new ArrayList<FactHandle>();
	private Integer numberOfRulesFired;
	private ExecutionResults executionResults;
	private List<String> executedRules;
	
	
	public ExecutionResults getExecutionResults() {
		return executionResults;
	}
	public void setExecutionResults(ExecutionResults executionResults) {
		this.executionResults = executionResults;
	}
	public List<FactHandle> getFactHandles() {
		return factHandles;
	}
	public void setFactHandles(List<FactHandle> factHandles) {
		this.factHandles = factHandles;
	}
	public Integer getNumberOfRulesFired() {
		return numberOfRulesFired;
	}
	public void setNumberOfRulesFired(Integer numberOfRulesFired) {
		this.numberOfRulesFired = numberOfRulesFired;
	}
	
	public void addFactHandle(FactHandle factHandle){
		this.factHandles.add(factHandle);
	}
	public List<String> getExecutedRules() {
		return executedRules;
	}
	public void setExecutedRules(List<String> executedRules) {
		this.executedRules = executedRules;
	}
	
}
