import java.io.File;
import java.io.IOException;
import java.lang.System;

import org.codehaus.plexus.util.FileUtils;

class HelloWorld {
    public static void main(String[] args) throws IOException {
        final File signalFile = new File(args[0]);
        System.out.println("HelloWorld.main: writing to " + signalFile);
        FileUtils.fileWrite(signalFile, "Hi there!");
    }
}
