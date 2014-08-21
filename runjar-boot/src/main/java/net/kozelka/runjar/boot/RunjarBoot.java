package net.kozelka.runjar.boot;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Launches an executable jar containing its dependency libraries.
 * <p>Libraries must be located directly in the toplevel directory "lib" (nested dirs are not supported). They are extracted into temporary files that are deleted when JVM exits.
 * <p>For verbose processing of the boot, specify JVM property "<code>runjar.verbose</code>" with value "<b>true</b>".
 * It prints list of extracted libraries and synopsis of executed main method.
 * </p>
 * <p>Example:<br/>
 * <pre>java -Drunjar.verbose=true -jar myconfig.runjar</pre>
 * </p>
 * <p>The jar must contain a META-INF/MANIFEST.MF file where the boot reads its configuration.
 * Following properties can be defined there:
 * <ul>
 * <li><b>Main-Class</b> (required) - must be "net.kozelka.runjar.boot.RunjarBoot"</li>
 * <li><b>runjar-main-class</b> (required) - the class to be executed</li>
 * <li><b>runjar-args</b> (default="") - space separated default list of arguments to be passed to above method.
 * Note that user can override this list by specifying one or more args on commandline</li>
 * </ul>
 * </p>
 * <p>
 * So, the runjar archive structure looks like this:
 * <pre>
 * META-INF/MANIFEST.MF
 * net.kozelka.runjar.boot.RunjarBoot.class
 * lib/mylibrary1.jar
 * lib/someotherlibrary.jar
 * ...
 * </pre>
 * </p>
 * <p>net.kozelka.runjar.boot.RunjarBoot sets the property <code>runjar.file</code> to the original executable jar.
 * Applications can use it to detect that they run in a RunJar and to extract some more resources from there.</p>
 * <p>Setting runjar.keep to true causes the temporary directory to not be deleted when execution ends</p>
 *
 * @author Petr Kozelka
 */
public class RunjarBoot {

    /**
     * Starts net.kozelka.runjar.boot.RunjarBoot from java command
     *
     * @param args command line arguments
     * @throws Exception when anything fails
     */
    public static void main(String[] args) throws Exception {
        final File runjarfile = new File(System.getProperty("java.class.path")); // we assume simple cli execution
        final List<File> classpath = new ArrayList<File>();
        classpath.add(runjarfile);

        final RunjarProperties props = RunjarProperties.load();
        //TODO: add support for --version, --help and --runjar here - nothing is unpacked yet; maybe also --bash_completion somehow
        final File basedir = File.createTempFile("runjar-",".tmp");
        basedir.delete();
        basedir.mkdirs();
        props.setBasedir(basedir);
        Utils.extract(runjarfile, basedir, props.verbose, new FileFilter() {
            private final File runjarLib = new File(basedir, "lib");
            private final String metainfPrefix = "META-INF" + File.separatorChar;

            @Override
            public boolean accept(File pathname) {
                final String name = pathname.getName();
                if (name.endsWith(".class")) {
                    return false;
                }
                final String absolutePath = pathname.getAbsolutePath();
                final String relPath = absolutePath.substring(basedir.getAbsolutePath().length() + 1);
                // metainf is not extracted
                if (relPath.startsWith(metainfPrefix)) {
                    return false;
                }
                // "dot" directories are not extracted
                if (relPath.startsWith(".")) {
                    return false;
                }
                if (runjarLib.equals(pathname.getParentFile())) {
                    classpath.add(pathname);
                }
                return true;
            }
        });

        //TODO: somehow, reintroduce the option to perform an action upon exit, to support graceful JBoss shutdown (needed on Windows where it otherwise keeps running)

        final Properties jvmProperties = new Properties();
        jvmProperties.setProperty("runjar.file", runjarfile.getAbsolutePath());
        jvmProperties.setProperty("runjar.basedir", basedir.getAbsolutePath());

        final ExecutionRequest request = new ExecutionRequest();
        request.setJvmProperties(jvmProperties);
        request.setMainClass(props.getMainClass());
        request.setClasspath(classpath);
        final List<String> allArgs = new ArrayList<String>();
        for (String arg : props.getArgsPrepend()) {
            allArgs.add(Utils.replaceProperties(arg, jvmProperties));
        }
        if (args != null && args.length > 0) {
            allArgs.addAll(Arrays.asList(args));
        }
        request.setArgs(allArgs);
        final Invoker invoker = new ForkedInvoker();

        if (props.verbose) {
            System.out.println("Invoking " + props.getMainClass() + ".main " + Arrays.asList(allArgs));
            System.out.println("  classpath: " + classpath);
        }

        final RunjarShutdownHook shutdownHook = new RunjarShutdownHook();
        if (!props.keep) {
            shutdownHook.setDirToDelete(basedir);
        }
        final String shutdownFile = props.getShutdownFile();
        if (shutdownFile != null) {
            final CustomShutdownAction customAction = new CustomShutdownAction(invoker, request);
            customAction.setShutdownFile(new File(Utils.replaceProperties(shutdownFile, jvmProperties)));
            shutdownHook.setCustomAction(customAction);
            shutdownHook.setVerbose(props.verbose);
        }
        if (!shutdownHook.isEmpty()) {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        final int exitcode = invoker.invoke(request);
        if (props.verbose) {
            System.out.println("Exit code is: " + exitcode);
        }
        if (exitcode != 0) {
            System.exit(exitcode);
        }
    }

}
