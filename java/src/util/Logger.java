package util;


/**A simple logger class.*/
public class Logger {

    /*Level Enum*/

    /**Enum for different logger levels.*/
    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    
    /*Variables*/

    /**The tag for this logger.*/
    private final String TAG;
    /**The current level for this logger.*/
    private Level logLevel;


    /*Constructor*/

    /**Creates a new logger with the given tag.
     * @param tag The tag to append to log messages from this logger.
    */
    public Logger(String tag) {

        //init

        this.TAG = tag;
        this.logLevel = Level.INFO;

    }


    /*Methods*/

    /**Logs a message to the console.
     * @param messageLevel The log level of the message.
     * @param message The message.
    */
    private void logMessage(Level messageLevel, String message) {

        //ignore messages with lower level than current level
        if(getLevelValue(messageLevel) < getLevelValue(logLevel)) {
            return;
        }

        if(messageLevel == Level.WARNING || messageLevel == Level.ERROR) {
            System.err.println("[" + messageLevel + "] " + TAG + ": " + message);
        }
        else {
            System.out.println("[" + messageLevel + "] " + TAG + ": " + message);
        }

    }

    /**Logs a debug message.*/
    public void debug(String message) {
        logMessage(Level.DEBUG, message);
    }

    /**Logs an info message.*/
    public void info(String message) {
        logMessage(Level.INFO, message);
    }

    /**Logs a warning message.*/
    public void warn(String message) {
        logMessage(Level.WARNING, message);
    }

    /**Logs an error message.*/
    public void error(String message) {
        logMessage(Level.ERROR, message);
    }

    /**Returns an integer value representing the given level.*/
    private int getLevelValue(Level level) {
        switch(level) {
            case DEBUG:
                return 0;
            case INFO:
                return 1;
            case WARNING:
                return 2;
            case ERROR:
                return 3;
            default:
                return -1;
        }
    }

    /**Sets the log level of this logger.*/
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

}
