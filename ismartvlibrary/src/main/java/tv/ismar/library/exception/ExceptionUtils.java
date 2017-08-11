package tv.ismar.library.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import tv.ismar.library.statistics.LogEntity;
import tv.ismar.library.statistics.LogQueue;

public class ExceptionUtils {

    private static final String TAG = "LH/ExceptionUtils";

    private ExceptionUtils() {
    }

    public static void sendProgramError(Throwable ex) {
        String errorInfo = ex.getMessage();
        String errorStack = getTrace(ex);

        LogEntity logEntity = new LogEntity("program_error");
        LogEntity.LogContent logContent = new LogEntity.LogContent();
        logContent.setError_info(errorInfo);
        logContent.setError_stack(errorStack);
        logEntity.setLog_content(logContent);
        LogQueue.getInstance().put(logEntity);
        LogQueue.getInstance().emptyQueue();
    }

    public static void sendApiIinfo(LogEntity.LogContent logContent) {
        LogEntity logEntity = new LogEntity("api_info");
        logEntity.setLog_content(logContent);
        LogQueue.getInstance().put(logEntity);
    }

    public static void sendGsonError(String sourceGson, String error) {
        LogEntity logEntity = new LogEntity("gson_error");
        LogEntity.LogContent logContent = new LogEntity.LogContent();
        logContent.setError_source(sourceGson);
        logContent.setError_info(error);
        logEntity.setLog_content(logContent);
        LogQueue.getInstance().put(logEntity);
    }

    private static String getTrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }

}
