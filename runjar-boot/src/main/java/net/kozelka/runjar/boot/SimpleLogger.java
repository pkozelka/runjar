package net.kozelka.runjar.boot;

/**
 * @author Petr Kozelka
 */
interface SimpleLogger {
    void info(String format, Object... args);

    static final SimpleLogger VERBOSE = new SimpleLogger() {
        @Override
        public void info(String format, Object... args) {
            System.err.println(String.format(format, args));
        }
    };

    static final SimpleLogger SILENT = new SimpleLogger() {
        @Override
        public void info(String format, Object... args) {}
    };
}

