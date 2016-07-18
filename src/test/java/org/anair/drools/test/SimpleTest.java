package org.anair.drools.test;


import org.anair.drools.fluent.api.RulesExecution;
import org.anair.drools.test.annotation.StatelessKSession;
import org.anair.drools.test.listener.DroolsTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.cdi.KReleaseId;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DroolsTestExecutionListener.class})
@KReleaseId(groupId="foo.bar", artifactId="bar-knowledge", version="1.0.0")
public class SimpleTest {
	@StatelessKSession("bar.kbase.stateless.session")
	private StatelessKieSession statelessKieSession;
	
	@KSession("bar.kbase.stateful.session")
	private KieSession statefulKieSession;
	
	@Test
	public void statelessSession_test_sample(){
		new RulesExecution(statelessKieSession)
			.addFacts("factString1", "factString2")
			.addGlobal("globalVariable1", "g1")
			.addGlobal("globalVariable1", "g1")
			.fireRules();
	}
}
