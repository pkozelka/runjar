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

    public static final String MAIN_CLASS_PROP = "runjar.main.class";
    public static final String ARGS_PREPEND_PROP = "runjar.args.prepend";
    public static final String SHUTDOWN_FILE_PROP = "runjar.shutdown.file";

    public final SimpleLogger logger;
    {
        logger = Boolean.getBoolean("runjar.verbose") ? SimpleLogger.VERBOSE : SimpleLogger.SILENT;
    }
    final boolean keep = Boolean.getBoolean("runjar.keep");

    private Properties properties;

    private File basedir;

    public RunjarProperties(Properties properties) {
        this.properties = properties;
    }

    public String getMainClass() {
        return properties.getProperty(MAIN_CLASS_PROP);
    }

    public String getShutdownFile() {
        return properties.getProperty(SHUTDOWN_FILE_PROP);
    }

    public List<String> getArgsPrepend() {
        return Utils.argsList(properties.getProperty(ARGS_PREPEND_PROP));
    }

    public File getBasedir() throws IOException {
        if (basedir == null) {
            final String basedirArg = System.getProperty("runjar.basedir");
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
            throw new IllegalStateException(MAIN_CLASS_PROP + " has not been defined. Check the existence of this property in " + runjarPropertiesFileName);
        }
        return result;
    }
}
