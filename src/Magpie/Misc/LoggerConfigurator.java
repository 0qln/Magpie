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
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
