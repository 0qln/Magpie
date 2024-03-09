package Misc;

import java.io.IOException;
import java.util.logging.*;

public class LoggerConfigurator {

    // TODO: singleton mechanism, such that this can only be set on program startup
    public static boolean loggingEnabled = false; 

    private static Logger nullLogger = Logger.getLogger(LoggerConfigurator.class.getName());

    static {
        nullLogger.setUseParentHandlers(false);
        nullLogger.addHandler(new NullHandler());
    }

    private static class NullHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public static Logger configureLogger(Class<?> clazz) {
        if (!loggingEnabled) {
            return nullLogger;
        }

        String logFileName = clazz + "_log.txt";
        Logger logger = Logger.getLogger(clazz.getName());
        try {
            Logger rootLogger = Logger.getLogger("");
            // Remove the default console handler
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }
            // Add handler to the log file
            FileHandler fileHandler = new FileHandler(logFileName, false);
            fileHandler.setFormatter(new CustomFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }

    public static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder(record.getMessage());
            if (record.getThrown() != null) {
                sb.append(record.getThrown().getClass()).append('\n');
                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    sb.append('\t');
                    sb.append(element.toString());
                    sb.append('\n');
                }
            }
            return sb.append('\n').toString();
        }
    }
}
