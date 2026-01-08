package com.whammich.invasion.util;

import com.whammich.invasion.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHelper {
    private static final Logger LOGGER = LogManager.getLogger(Reference.NAME);

    public static void info(Object info) {
        LOGGER.info(info);
    }

    public static void error(Object error) {
        LOGGER.error(error);
    }

    public static void debug(Object debug) {
        LOGGER.debug(debug);
    }
}
