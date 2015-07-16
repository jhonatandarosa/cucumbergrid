package org.cucumbergrid.junit.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CucumberGridHub {

    int port() default 26000;
    int discoveryServicePort() default 26001;
    String[] clusterBy() default {};
}
