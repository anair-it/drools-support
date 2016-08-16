# Drools Runtime and Test support tools
- Fluent APIs to interact with Kie Sessions and firing rules.
- Custom Spring TestExecutionListener to support unit tests

# Version
- JDK: >6
- Maven: 3.x
- Spring: > 3.2
- Drools: 6.4.0.Final

## Getting started
- Download this project
- ``mvn install`` this project
- Add the project as a dependency
- Change the kie version if required. Currently it points to community version: _6.4.0.Final_

## Fluent API usage
### Get a stateless Kie session:
- Import _drools-support-context.xml_ into your application spring context
- Create a RulesExecution Service class where you will acquire a session and fire rules
- Inject _KieSessionProvider_.
- If no session name is provided, the default session will be identified and returned
		
		StatelessKieSession statelessKieSession = new SessionBuilder(kieSessionProvider)
			.forKnowledgeModule("foo.bar:bar-knowledge:1.0.0")
			.pollingIntervalMillis(100) //Optional
			.fetchStatelessKieSession("bar.kbase.stateless.session");  //No-arg method will fetch default session
			
### Get a stateful Kie session:
		
		KieSession statefulKieSession = new SessionBuilder(kieSessionProvider)
			.forKnowledgeModule("foo.bar", "bar-knowledge", "1.0.0")
			.fetchKieSession("bar.kbase.stateful.session"); //No-arg method will fetch default session


### Fire rules on a stateless session

		new RulesExecution(statelessKieSession) //Constructor-arg accepts KieSession or StatelessKieSession
			.isBatchExecution(true) //Default is true. Applicable to only StatlessKieSession
			.addFacts(factList) //List of fact objects
			.addGlobal(globalVariable1, globalObject1) //Optional global
			.addGlobal(globalVariable2, globalObject2) //Optional global
			.addEventListeners(myAgendaListsner, myProcessListener) //Optional ArrayList/Array of Listeners
			.fireRules();

### Fire rules on a stateful session

		new RulesExecution(kieSession)
			.addFacts(fact1, fact2) //Array of fact objects
			.addGlobals(globalsMap) //Optional HashMap of globals
			.addEventListeners(myAgendaListsner, myProcessListener) //Optional ArrayList/Array of Listeners
			.forAgendaGroups("agenda-group-1", "agenda-group-2") //Optional array of agenda group names
			.fireRules();

- Add the following entries in _log4j.properties_:

		log4j.category.org.drools=INFO
		log4j.category.org.kie=INFO
		log4j.category.org.eclipse=ERROR
		log4j.category.org.anair=INFO
		log4j.category.org.springframework=ERROR
		log4j.category.org.apache=ERROR
		

## Audit and deep tracing
The _RulesExecution_ API has features to generate multiple audit logs. There are 2 types of audit logs.

### Audit log
Print rules that got fired for a transaction along with execution stats. This is helpful for Business and IT for audit logging/debugging. You may add application context information to identify the specific transaction that fired a set of rules. Below steps explain how to do this:

- Add the following entries in _log4j.properties_. Replace _var1_ and _var2_ with meaningful context variables. You may add/remove as many as required

		log4j.appender.rulesaudit=org.apache.log4j.RollingFileAppender
		log4j.appender.rulesaudit.File=${catalina.base}/logs/rules-audit.log
		log4j.appender.rulesaudit.MaxFileSize=10MB
		log4j.appender.rulesaudit.MaxBackupIndex=3
		log4j.appender.rulesaudit.layout=org.apache.log4j.PatternLayout
		log4j.appender.rulesaudit.layout.ConversionPattern=%d [%X{var1}][%X{var2}]- %m%n
		
		log4j.category.rules-audit=INFO,rulesaudit
		log4j.additivity.rules-audit=false

- Add the following calls to _RulesExecution_
			
		.addContext("user", "USER1") //Add diagnostic context to  rules logging
		.addContext("key", "123") //Add diagnostic context to  rules logging
		.fireRules(); 

- Run a test and you should see the file _rules-audit.log_ generated with log statements that look like:

		2016-07-29 12:10:08,516 [USER1][123]- Rule -> My rule 1  | Stats -> matchesCreated=1 matchesCancelled=0 matchesFired=1 firingTime=7ms
		2016-07-29 12:10:08,516 [USER1][123]- Rule -> My rule 2  | Stats -> matchesCreated=1 matchesCancelled=0 matchesFired=1 firingTime=1ms
		2016-07-29 12:10:08,516 [USER1][123]- Rule -> My rule 3  | Stats -> matchesCreated=1 matchesCancelled=0 matchesFired=1 firingTime=1ms
- Disable this feature by changing the log level of rules-audit to WARN/ERROR.

### Trace log
This IT specific feature generates a drools audit log with the rules that got executed along with the working memory data. Use this ONLY to debug production issues. To enable this feature:
- Add this in _log4j.properties_

		log4j.category.org.anair.drools.fluent.api.RulesExecution=TRACE
- Add the following calls to _RulesExecution_
			
		.auditTrace("/server/app/logs") //Pass the path a system property
		.fireRules();

- Run a test and you should see the file _rules-trace.log_ generated with xml content in /logs directory
- Open Drools Audit view in Eclipse
- Copy the file to your local and point to the file Audit view. The view displays activated rules and related working memory content. This is very useful for IT debugging.
			
## Unit testing Knowledge module
- Add _log4j.properties_ in src/test/resources
- Add the following entries in _log4j.properties_:

		log4j.category.org.drools=INFO
		log4j.category.org.kie=INFO
		log4j.category.org.eclipse=ERROR
		log4j.category.org.anair.drools=INFO
		log4j.category.org.springframework=ERROR

- Use the following annotations to help with Drools testing
	- _KReleaseId_: Enter knowledge module GAV at the Test class level
	- _KBase_: Create an attribute and with KBase annotation and provide the kbase name. You can have multiple KBase attributes 
	- _KSession_: Create an attribute and with KSession annotation and provide the stateful ksession name. You can have multiple KSession attributes
	- _StatelessKSession_: Create an attribute and with StatelessKSession annotation and provide the stateless session name. You can have multiple StatelessKSession attributes
	- _EventListeners_: Enable/disable auditing and tracing. By default it is enabled if you don't include the annotation. Trace file will be generated at _target/rules-trace.log_. Audit trail of the rules that got fired will be in the console log
- Add the Spring test execution listener __DroolsTestExecutionListener__


- Refer _SimpleTest.java_ for usage of _DroolsTestExecutionListener_ and annotations
- After executing a test, 
	- a drools trace audit file is generated at target/rules-trace.log. You may change the file name and path by using EventListeners annotation in the test
	- Open Drools Audit view in Eclipse
	- Drag rules-trace.log to the view
	- You should see all the rules that got executed along with the fact models that triggered the execution 
	- Rules that got fired along with performance stat is printed in the console
	
# Reference
- [Drools 6.4.0.Final reference](https://docs.jboss.org/drools/release/6.4.0.Final/drools-docs/html_single/#d0e1087)
- [Drools 6.4.0.Final Jira](https://docs.jboss.org/drools/release/6.4.0.Final/drools-docs/html_single/#d0e1087)
	
