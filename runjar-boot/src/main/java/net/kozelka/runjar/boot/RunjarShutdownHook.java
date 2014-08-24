package net.kozelka.runjar.boot;

import java.io.File;

/**
 * @author Petr Kozelka
 */
class RunjarShutdownHook extends Thread {
    private File dirToDelete;
    private Runnable customAction;
    private SimpleLogger logger;

    public void setDirToDelete(File dirToDelete) {
        this.dirToDelete = dirToDelete;
    }

    public void setCustomAction(Runnable customAction) {
        this.customAction = customAction;
    }

    public boolean isEmpty() {
        return dirToDelete == null && customAction == null;
    }

    public void setLogger(SimpleLogger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        if (customAction != null) {
            customAction.run();
        }
        if (dirToDelete != null) {
            deepDeleteTryHard(dirToDelete);
        }
    }

    private void deepDeleteTryHard(File root) {
        logInfo("Deleting temporary files in %s", root.toString());
        Utils.deepDelete(root);
        final long limit = System.currentTimeMillis() + 1000;
        while (root.exists() && System.currentTimeMillis()<limit) {
            try {
                // let other threads work more for a while (and release locks)
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // try again
            Utils.deepDelete(root);
        }
        if (root.exists()) {
            logError("Some temporary files could not be deleted - giving up");
        }
    }

    private void logInfo(String msg, Object ... args) {
        logger.info(msg, args);
    }

    private void logError(String msg) {
        System.err.println(msg);
    }
}
