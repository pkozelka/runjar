package net.kozelka.runjar.boot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Petr Kozelka
 */
public class RunjarProperties {

    public static final String RUNJAR_PROPERTIES_RES = "META-INF/runjar.properties";

    // properties accepted from META-INF/runjar.properties
    public static final String PROP_META_CLASS = "runjar.main.class";
    public static final String PROP_META_ARGS_PREPEND = "runjar.args.prepend";
    public static final String PROP_META_SHUTDOWN_FILE = "runjar.shutdown.file";

    // properties accepted from command line (JVM properties)
    public static final String PROP_CMDLINE_KEEP = "runjar.keep";
    public static final String PROP_CMDLINE_VERBOSE = "runjar.verbose";
    public static final String PROP_CMDLINE_BASEDIR = "runjar.basedir";
    public static final String PROP_CMDLINE_JVMARGS = "runjar.jvmargs";

    // properties passed on to application (JVM properties for nested app)
    public static final String PROP_APP_BASEDIR = "runjar.basedir";
    public static final String PROP_APP_FILE = "runjar.file";

    public final SimpleLogger logger;

    {
        logger = Boolean.getBoolean(PROP_CMDLINE_VERBOSE) ? SimpleLogger.VERBOSE : SimpleLogger.SILENT;
    }
    final boolean keep = Boolean.getBoolean(PROP_CMDLINE_KEEP);

    private Properties properties;

    private File basedir;

    public RunjarProperties(Properties properties) {
        this.properties = properties;
    }

    public String getMainClass() {
        return properties.getProperty(PROP_META_CLASS);
    }

    public String getShutdownFile() {
        return properties.getProperty(PROP_META_SHUTDOWN_FILE);
    }

    public List<String> getArgsPrepend() {
        return Utils.argsList(properties.getProperty(PROP_META_ARGS_PREPEND));
    }

    public List<String> getJvmArgs() {
        return Utils.argsList(properties.getProperty(PROP_CMDLINE_JVMARGS));
    }

    public File getBasedir() throws IOException {
        if (basedir == null) {
            final String basedirArg = System.getProperty(PROP_CMDLINE_BASEDIR);
            if (basedirArg == null) {
                basedir = File.createTempFile("runjar-",".tmp");
                basedir.delete();
            } else {
                basedir = new File(basedirArg);
            }
            basedir.mkdirs();
            if (!basedir.isDirectory()) {
                throw new IOException("Failed to create runjar.basedir: " + basedir);
            }
        }
        return basedir;
    }

    static RunjarProperties load() throws IOException {
        final String runjarPropertiesFileName = RUNJAR_PROPERTIES_RES;
        final Enumeration resEnum = ClassLoader.getSystemClassLoader().getResources(runjarPropertiesFileName);
        if (!resEnum.hasMoreElements()) {
            throw new FileNotFoundException(runjarPropertiesFileName);
        }
        final URL url = (URL) resEnum.nextElement();
        if (resEnum.hasMoreElements()) {
            final StringBuilder sb = new StringBuilder(url.toString());
            while (resEnum.hasMoreElements()) {
                sb.append(", ");
                sb.append(resEnum.nextElement());
            }
            throw new IllegalStateException(runjarPropertiesFileName + " is present multiple times: " + sb);
        }
        // ok, there is exactly one resource named $RUNJAR_PROPERTIES_RES
        final Properties props = new Properties();
        props.load(url.openStream());
        final RunjarProperties result = new RunjarProperties(props);
        if (result.getMainClass() == null) {
            throw new IllegalStateException(PROP_META_CLASS + " has not been defined. Check the existence of this property in " + runjarPropertiesFileName);
        }
        return result;
    }
}
