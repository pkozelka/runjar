package net.kozelka.runjar.boot;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Launches an executable jar containing its dependency libraries.
 *
 * @author Petr Kozelka
 */
public class RunjarBoot {

    public static void main(String[] args) throws Exception {
        final File runjarfile = new File(System.getProperty("java.class.path")); // we assume simple cli execution
        final List<File> classpath = new ArrayList<File>();
        classpath.add(runjarfile);

        final RunjarProperties props = RunjarProperties.load();
        //TODO: add support for --version, --help and --runjar here - nothing is unpacked yet; maybe also --bash_completion somehow
        final File basedir = props.getBasedir();
        Utils.extract(runjarfile, basedir, props.logger, new FileFilter() {
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

        // These properties will be available to the application
        final Properties jvmProperties = new Properties();
        jvmProperties.setProperty(RunjarProperties.PROP_APP_FILE, runjarfile.getAbsolutePath());
        jvmProperties.setProperty(RunjarProperties.PROP_APP_BASEDIR, basedir.getAbsolutePath());

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

        final SimpleLogger logger = props.logger;
        logger.info("Invoking %s.main %s", props.getMainClass(), allArgs);
        logger.info("  classpath: %s", classpath);

        final RunjarShutdownHook shutdownHook = new RunjarShutdownHook();
        shutdownHook.setLogger(logger);
        if (!props.keep) {
            shutdownHook.setDirToDelete(basedir);
        }
        final String shutdownFile = props.getShutdownFile();
        if (shutdownFile != null) {
            final CustomShutdownAction customAction = new CustomShutdownAction(invoker, request);
            customAction.setShutdownFile(new File(Utils.replaceProperties(shutdownFile, jvmProperties)));
            shutdownHook.setCustomAction(customAction);
        }
        if (!shutdownHook.isEmpty()) {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        final int exitcode = invoker.invoke(request);
        logger.info("Exit code is: %d", exitcode);
        if (exitcode != 0) {
            System.exit(exitcode);
        }
    }

}
