# Drools Runtime and Test support tools
- Fluent APIs to interact with Kie Sessions and firing rules.
- Custom Spring TestExecutionListener for unit tests

## Getting started
- Download this project
- ``mvn install`` this project
- Add the project as a dependency
- Change the kie version if required. Currently it points to _6.4.0.Final_

## Fluent API usage
### Get a stateless Kie session:
		
		StatelessKieSession statelessKieSession = new SessionBuilder("foo.bar:bar-knowledge:1.0.0")
			.fetchStatelessKieSession("bar.kbase.stateless.session");
			
### Get a stateful Kie session:
		
		KieSession statefulKieSession = new SessionBuilder("foo.bar", "bar-knowledge", "1.0.0")
			.fetchKieSession("bar.kbase.stateful.session");

### Fire rules on a stateless session

		new RulesExecution(statelessKieSession) //Constructor-arg accepts KieSession or StatelessKieSession
			.addFacts(facts) //ArrayList/Array if fact objects
			.addGlobal(globalVariable1, globalObject1) //Optional global
			.addGlobal(globalVariable2, globalObject2) //Optional global
			.addEventListeners(myAgendaListsner, myProcessListener) //Optional ArrayList/Array of Listeners 
			.fireRules();

### Fire rules on a stateful session

		new RulesExecution(kieSession)
			.addFacts(facts) // ArrayList/Array if fact objects
			.addGlobals(globalsMap) //Optional HashMap of globals
			.addEventListeners(myAgendaListsner, myProcessListener) //Optional ArrayList/Array of Listeners
			.forAgendaGroups("agenda-group-1", "agenda-group-2") //Optional array of agenda group names
			.fireRules();
			
## Unit testing Drools rules
- Add _log4j.properties_ in src/test/resources
- Add the following entries in _log4j.properties_:

		log4j.category.org.drools=INFO
		log4j.category.org.anair=INFO
- Refer _SimpleTest.java_ for usage of _DroolsTestExecutionListener_ and other annotations
- After running tests, the log will show the rules that got fired. There is a default Agenda listener that does that.
