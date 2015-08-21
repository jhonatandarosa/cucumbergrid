package org.cucumbergrid.junit.runner;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CucumberGridNode {

    String hub() default "";
    int port() default 26000;
    int discoveryServicePort() default 26001;
    int discoveryServiceTimeout() default 60000;
    Class<? extends NodePropertyRetriever> retriever() default NodePropertyRetriever.class;
}
