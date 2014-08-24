import java.io.File;
import java.io.IOException;
import java.lang.System;

import org.codehaus.plexus.util.FileUtils;

class GracefulShutdown {
    public static void main(String[] args) throws IOException {
        final File signalFile = new File(args[0]);
        System.out.println("GracefulShutdown.main: writing to " + signalFile);
        FileUtils.fileWrite(signalFile, "shutdown action called");
    }
}
