package org.cucumbergrid.junit.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cucumber.api.SnippetType;
import cucumber.runtime.CucumberException;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;

public class CucumberGridRuntimeOptionsFactory {
    private Class clazz;
    private Class<? extends Annotation>[] annotationClasses;
    private boolean featuresSpecified = false;
    private boolean glueSpecified = false;
    private boolean formatSpecified = false;
    private List<String> args;

    public CucumberGridRuntimeOptionsFactory(Class clazz, Class<? extends Annotation>[] annotationClasses) {
        this.clazz = clazz;
        this.annotationClasses = annotationClasses;
    }

    public CucumberGridRuntimeOptionsFactory(List<String> args) {
        this.args = args;
    }

    public RuntimeOptions create() {
        if (args == null) {
            args = buildArgsFromOptions();
        }
        return new RuntimeOptions(args);
    }

    public ArrayList<String> buildArgsFromOptions() {
        ArrayList<String> args = new ArrayList<String>();

        for (Class classWithOptions = clazz; hasSuperClass(classWithOptions); classWithOptions = classWithOptions.getSuperclass()) {
            Annotation[] optionsArray = getOptions(classWithOptions);
            for (Annotation options : optionsArray) {
                if (options != null) {
                    addDryRun(options, args);
                    addMonochrome(options, args);
                    addTags(options, args);
                    addFormats(options, args);
                    addStrict(options, args);
                    addName(options, args);
                    addDotCucumber(options, args);
                    addSnippets(options, args);
                    addGlue(optionsArray, args);
                    addFeatures(optionsArray, args);
                }
            }
        }
        addDefaultFeaturePathIfNoFeaturePathIsSpecified(args, clazz);
        addDefaultGlueIfNoGlueIsSpecified(args, clazz);
        addDefaultFormatIfNoFormatIsSpecified(args);
        return args;
    }

    private void addDotCucumber(Annotation options, List<String> args) {
        String dotcucumber = this.invoke(options, "dotcucumber");
        if (!dotcucumber.isEmpty()) {
            args.add("--dotcucumber");
            args.add(dotcucumber);
        }
    }

    private void addName(Annotation options, List<String> args) {
        for (String name : this.<String[]>invoke(options, "name")) {
            args.add("--name");
            args.add(name);
        }
    }

    private void addSnippets(Annotation options, List<String> args) {
        args.add("--snippets");
        args.add(this.<SnippetType>invoke(options, "snippets").toString());
    }

    private void addDryRun(Annotation options, List<String> args) {
        if (this.<Boolean>invoke(options, "dryRun")) {
            args.add("--dry-run");
        }
    }

    private void addMonochrome(Annotation options, List<String> args) {
        if (this.<Boolean>invoke(options, "monochrome") || runningInEnvironmentWithoutAnsiSupport()) {
            args.add("--monochrome");
        }
    }

    private void addTags(Annotation options, List<String> args) {
        for (String tags : this.<String[]>invoke(options, "tags")) {
            args.add("--tags");
            args.add(tags);
        }
    }

    private void addFormats(Annotation options, List<String> args) {
        if (this.<String[]>invoke(options, "format").length != 0) {
            for (String format : this.<String[]>invoke(options, "format")) {
                args.add("--format");
                args.add(format);
            }
            formatSpecified = true;
        }
    }

    private void addDefaultFormatIfNoFormatIsSpecified(List<String> args) {
        if (!formatSpecified) {
            args.add("--format");
            args.add("null");
        }
    }

    private void addFeatures(Annotation[] optionsArray, List<String> args) {
        for (Annotation options : optionsArray) {
            if (options != null && this.<String[]>invoke(options, "features").length != 0) {
                Collections.addAll(args, this.<String[]>invoke(options, "features"));
                featuresSpecified = true;
            }
        }
    }

    private void addDefaultFeaturePathIfNoFeaturePathIsSpecified(List<String> args, Class clazz) {
        if (!featuresSpecified) {
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }

    private void addGlue(Annotation[] optionsArray, List<String> args) {
        for (Annotation options : optionsArray) {
            if (options != null && this.<String[]>invoke(options, "glue").length != 0) {
                for (String glue : this.<String[]>invoke(options, "glue")) {
                    args.add("--glue");
                    args.add(glue);
                }
                glueSpecified = true;
            }
        }
    }

    private void addDefaultGlueIfNoGlueIsSpecified(List<String> args, Class clazz) {
        if (!glueSpecified) {
            args.add("--glue");
            args.add(MultiLoader.CLASSPATH_SCHEME + packagePath(clazz));
        }
    }


    private void addStrict(Annotation options, List<String> args) {
        if (this.<Boolean>invoke(options, "strict")) {
            args.add("--strict");
        }
    }

    static String packagePath(Class clazz) {
        return packagePath(packageName(clazz.getName()));
    }

    static String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    static String packageName(String className) {
        return className.substring(0, Math.max(0, className.lastIndexOf(".")));
    }

    private boolean runningInEnvironmentWithoutAnsiSupport() {
        boolean intelliJidea = System.getProperty("idea.launcher.bin.path") != null;
        // TODO: What does Eclipse use?
        return intelliJidea;
    }

    private boolean hasSuperClass(Class classWithOptions) {
        return classWithOptions != Object.class;
    }

    private Annotation[] getOptions(Class<?> clazz) {
        Annotation[] annotations = new Annotation[annotationClasses.length];
        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = clazz.getAnnotation(annotationClasses[i]);
        }
        return annotations;
    }

    private <T> T invoke(Annotation options, String name) {
        try {
            Method method = options.annotationType().getMethod(name);
            return (T) method.invoke(options);
        } catch (NoSuchMethodException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e);
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        }
    }

}