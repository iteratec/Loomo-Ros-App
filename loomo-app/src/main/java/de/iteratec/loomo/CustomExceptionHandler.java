package de.iteratec.loomo;

import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * import .service.backend.BackendService;
 * import .service.system.RestartService;
 * import .util.MemoryLeakUtil;
 * import .util.StringUtils;
 */

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    private static final String LOG_TAG = "CustomExceptionHandler";

    //private static final LogUtil     logger = LogUtil.getLogger(CustomExceptionHandler.class);

    //private final BackendService     backendService;
    //private final RestartService     restartService;

    @SuppressWarnings("unused")
    private UncaughtExceptionHandler defaultExceptionHandler;

    public CustomExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler) {
        this.defaultExceptionHandler = defaultExceptionHandler;
        //this.restartService = restartService;
        //this.backendService = backendService;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //String stackTrace = StringUtils.getStackTrace(ex);
        Log.e(LOG_TAG, "Caugth unexpected error.", ex);
        //backendService.queueException("Exception happened that causes an application restart.", ex);

        //MemoryLeakUtil.saveMemoryDumpToDisk(MemoryLeakUtil.ERROR_DUMP_HPROF);

        //restartService.restartAppAfterException(2000, false);
    }

}