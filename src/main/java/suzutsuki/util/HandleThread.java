package suzutsuki.util;

import org.slf4j.Logger;

public class HandleThread {
    public static void error(Runnable runnable, Logger logger) {
        try {
            runnable.run();
        } catch (Throwable error) {
            logger.error(error.getMessage(), error);
        }
    }
}
