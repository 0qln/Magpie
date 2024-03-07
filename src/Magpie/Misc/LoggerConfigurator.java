package Misc;

import java.io.IOException;
import java.util.logging.*;

public class LoggerConfigurator {
    public static Logger configureLogger(Class<?> clazz) {
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
            // Extracting only the log message without timestamp and class name
            return record.getMessage() + "\n";
        }
    }
}
