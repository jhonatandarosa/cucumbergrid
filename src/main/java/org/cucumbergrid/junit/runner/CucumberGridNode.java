package org.cucumbergrid.junit.runner;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CucumberGridNode {

    String hub();
    int port() default 26000;
    int selectTimeout() default 1000;
    int connectTimeout() default 5000;
    int maxRetries() default 10;
}
