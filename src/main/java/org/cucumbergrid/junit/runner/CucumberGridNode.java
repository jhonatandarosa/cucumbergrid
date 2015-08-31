package org.cucumbergrid.junit.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.notification.RunListener;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CucumberGridNode {

    String hub() default "";
    int port() default 26000;
    int discoveryServicePort() default 26001;
    int discoveryServiceTimeout() default 300000;// 5 minutes
    Class<? extends NodePropertyRetriever> retriever() default NodePropertyRetriever.class;

    Class<? extends RunListener>[] listeners() default RunListener.class;
}
