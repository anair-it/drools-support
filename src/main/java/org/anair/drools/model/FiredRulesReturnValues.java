package org.anair.drools.model;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.rule.FactHandle;

public class FiredRulesReturnValues {
	
	private List<FactHandle> factHandles = new ArrayList<FactHandle>();
	private Integer numberOfRulesFired;
	
	
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
	
}
