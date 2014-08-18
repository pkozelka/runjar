package net.kozelka.runjar.boot;

import java.io.IOException;

/**
 * @author Petr Kozelka
 */
public interface Invoker {
    /**
     * Invokes the application.
     * @param executionRequest -
     * @return exit code to be passed up to the system via {@link System#exit(int)}
     */
    public int invoke(ExecutionRequest executionRequest) throws IOException, InterruptedException;
}
