package net.kozelka.runjar.boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * A custom action to be performed before removing temporary directory
 */
class CustomShutdownAction implements Runnable {
    public static final String RUNJAR_SHUTDOWN_CLASS = "runjar.shutdown.class";
    public static final String RUNJAR_SHUTDOWN_ARGS = "runjar.shutdown.args";
    private File shutdownFile;

    private final Invoker invoker;
    private final ExecutionRequest originalRequest;

    public CustomShutdownAction(Invoker invoker, ExecutionRequest originalRequest) {
        this.invoker = invoker;
        this.originalRequest = originalRequest;
    }

    public void setShutdownFile(File shutdownFile) {
        this.shutdownFile = shutdownFile;
    }

    @Override
    public void run() {
        if (shutdownFile.exists()) {
            try {
                final ExecutionRequest shutdownRequest = new ExecutionRequest(originalRequest);
                final Properties properties = new Properties();
                properties.load(new FileInputStream(shutdownFile));
                final List<String> shutdownArgs = Utils.argsList(properties.getProperty(RUNJAR_SHUTDOWN_ARGS));
                shutdownRequest.setArgs(shutdownArgs);
                final String cls = properties.getProperty(RUNJAR_SHUTDOWN_CLASS);
                if (cls != null) {
                    shutdownRequest.setMainClass(cls);
                }
                System.err.println("Trying to shutdown gracefully...");
                final int exitCode = invoker.invoke(shutdownRequest);
                System.err.println("STOP command returned with exitCode=" + exitCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
