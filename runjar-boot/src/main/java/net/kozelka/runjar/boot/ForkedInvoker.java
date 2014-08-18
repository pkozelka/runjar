package net.kozelka.runjar.boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Petr Kozelka
 */
public class ForkedInvoker implements Invoker {
    @Override
    public int invoke(ExecutionRequest request) throws IOException, InterruptedException {
        final String javaBinary = System.getProperty("java.home") + "/bin/java";
        final String classpathStr = Utils.classpath(request.getClasspath());
        // build invocation command
        final List<String> runCommand = new ArrayList<String>();
        runCommand.add(javaBinary);
        if (classpathStr.length() > 0) {
            //TODO: instead, generate JAR with custom manifest to make sure it works well also on windows!!!
            runCommand.add("-classpath");
            runCommand.add(classpathStr);
        }
        final Properties jvmProperties = request.getJvmProperties();
        for (Map.Entry<Object, Object> entry : jvmProperties.entrySet()) {
            runCommand.add("-D" + entry.getKey() + "=" + entry.getValue());
        }
        runCommand.add(request.getMainClass());
        runCommand.addAll(request.getArgs());

        return Utils.execute(null, runCommand.toArray(new String[runCommand.size()]));
    }
}
