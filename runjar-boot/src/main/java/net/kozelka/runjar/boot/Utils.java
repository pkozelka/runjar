package net.kozelka.runjar.boot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Petr Kozelka
 */
public class Utils {
    static String commandToString(List<String> commandLineElements) {
        final StringBuilder sb = new StringBuilder();
        for (String cle : commandLineElements) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(cle);
        }
        return sb.toString();
    }

    static void deepDelete(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return !name.equals(".") && !name.equals("..");
                }
            });
            if (files != null) {
                for (File f : files) {
                    deepDelete(f);
                }
            }
            file.delete();
        }
    }

    static String classpath(List<File> cpeList) {
        final StringBuilder sb = new StringBuilder();
        for (File cpe : cpeList) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(cpe.getAbsolutePath());
        }
        return sb.toString();
    }

    static void extract(File zip, File dest, SimpleLogger logger, FileFilter fileFilter) throws IOException {
        try {
            final ZipFile zipFile = new ZipFile(zip);
            final Enumeration<? extends ZipEntry> ee = zipFile.entries();
            while (ee.hasMoreElements()) {
                final ZipEntry zipEntry =  ee.nextElement();
                final String uri = zipEntry.getName();
                if (uri.endsWith("/")) continue; // ignore dirs
                final File destFile = new File(dest, uri);
                if (! fileFilter.accept(destFile)) continue;
                logger.info("Extracting !/%s into %s", uri, destFile);
                destFile.getParentFile().mkdirs();
                final OutputStream os = new FileOutputStream(destFile);
                try {
                    copyFile(zipFile.getInputStream(zipEntry), os);
                    final long entryTime = zipEntry.getTime();
                    if (entryTime != -1) {
                        destFile.setLastModified(entryTime);
                    }
                } finally {
                    os.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copies file from source to destination
     *
     * @param is source input stream
     * @param os destination output stream
     */
    private static void copyFile(InputStream is, OutputStream os) throws IOException {

        final byte[] buf = new byte[4096];
        int cnt = is.read(buf, 0, buf.length);
        while (cnt >= 0) {
            os.write(buf, 0, cnt);
            cnt = is.read(buf, 0, buf.length);
        }
        os.flush();
    }

    /**
     * Executes given command with parameters
     *
     * @param command command to execute
     * @return command exit value
     */
    public static int execute(final File dir, String[] command) throws IOException, InterruptedException {

        class Rewriter extends Thread {
            private BufferedReader bufferedReader;
            private PrintStream printStream;

            public Rewriter(InputStream is, PrintStream printStream) {
                this.bufferedReader = new BufferedReader(new InputStreamReader(is));
                this.printStream = printStream;
            }

            public void run() {
                try {
                    String s;
                    while ((s = bufferedReader.readLine()) != null)
                        printStream.println(s);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }

        final Process proc = Runtime.getRuntime().exec(command, null, dir);
        final Thread stdThread = new Rewriter(proc.getInputStream(), System.out);
        final Thread errThread = new Rewriter(proc.getErrorStream(), System.err);
        stdThread.start();
        errThread.start();
        stdThread.join();
        errThread.join();
        proc.waitFor();
        return proc.exitValue();
    }

    static String replaceProperties(String arg, Properties props) {
        final StringBuilder sb = new StringBuilder();
        int n = arg.indexOf("${");
        int x = 0;
        while (n >= 0) {
            sb.append(arg.substring(x, n));
            final int n2 = arg.indexOf('}', n + 2);
            final String propName = arg.substring(n + 2, n2);
            final String propValue = props.getProperty(propName);
            sb.append(propValue);
            x = n2 + 1;
            n = arg.indexOf("${", x);
        }
        sb.append(arg.substring(x));
        return sb.toString();
    }

    static List<String> argsList(String ap) {
        if (ap == null) return Collections.emptyList();
        if (ap.length() < 2) return Collections.emptyList();
        final String[] args = ap.substring(1).split(ap.substring(0, 1));
        return Arrays.asList(args);
    }
}
