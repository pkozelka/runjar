import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;

class HelloWorld {
    public static void main(String[] args) throws IOException {
        final File signalFile = new File(args[0]);
        System.out.println("HelloWorld.main: writing to " + signalFile);
        FileUtils.fileWrite(signalFile, "Hi there!");

        final File shutdownPropsFile = new File(System.getProperty("runjar.basedir"), ".shutdown");

        final Properties shProps = new Properties();
        shProps.setProperty("runjar.shutdown.class", GracefulShutdown.class.getName());
        shProps.setProperty("runjar.shutdown.args", "," + signalFile.getAbsolutePath() + ".shutdown-was-called");
        final FileWriter fileWriter = new FileWriter(shutdownPropsFile);
        shProps.store(fileWriter, "Graceful shutdown params");
        fileWriter.close();
    }
}
