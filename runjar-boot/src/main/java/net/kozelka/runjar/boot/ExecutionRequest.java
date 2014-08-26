package net.kozelka.runjar.boot;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author Petr Kozelka
 */
public class ExecutionRequest {
    private List<String> jvmArgs;
    private Properties jvmProperties;
    private List<File> classpath;
    private String mainClass;
    private List<String> args;

    public ExecutionRequest() {
    }

    public ExecutionRequest(ExecutionRequest orig) {
        this.jvmArgs = orig.jvmArgs;
        this.jvmProperties = orig.jvmProperties;
        this.classpath = orig.classpath;
        this.mainClass = orig.mainClass;
        this.args = orig.args;
    }

    public Properties getJvmProperties() {
        return jvmProperties;
    }

    public void setJvmProperties(Properties jvmProperties) {
        this.jvmProperties = jvmProperties;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public List<File> getClasspath() {
        return classpath;
    }

    public void setClasspath(List<File> classpath) {
        this.classpath = classpath;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(List<String> jvmArgs) {
        this.jvmArgs = jvmArgs;
    }
}
