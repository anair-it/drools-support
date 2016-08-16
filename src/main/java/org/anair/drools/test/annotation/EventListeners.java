package org.anair.drools.test.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
public @interface EventListeners {
	boolean enabled() default true;
	String auditlogFileName() default "rules-trace";
}
