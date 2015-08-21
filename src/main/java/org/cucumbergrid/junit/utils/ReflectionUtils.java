package org.cucumbergrid.junit.utils;

import java.lang.annotation.Annotation;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getDeclaredAnnotation(Class<?> clazz, Class<T> annotation) {
        Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (annotation.isInstance(declaredAnnotation)) {
                return (T) declaredAnnotation;
            }
        }
        return null;
    }
}
