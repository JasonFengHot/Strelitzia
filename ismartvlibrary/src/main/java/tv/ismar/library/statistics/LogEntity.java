package tv.ismar.library.statistics;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;

/**
 * Created by huibin on 6/9/17.
 */

public class LogEntity implements Serializable {
    private static final long serialVersionUID = 2299319508839163476L;

    private LogContent log_content;
    private String log_type;
    private String create_time;

    public LogEntity(String log_type) {
        this.log_type = log_type;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        this.create_time = formatter.format(TrueTime.now());
    }

    public LogContent getLog_content() {
        return log_content;
    }

    public void setLog_content(LogContent log_content) {
        this.log_content = log_content;
    }


    public static class LogContent implements Serializable{
        private static final long serialVersionUID = -7084462912628469249L;
        /**
         * api_info
         */
        private String request_url;
        private String method;
        private String type;
        private String request_parameters;
        private String request_result;
        private String result_type;


        /**
         * error_info
         */
        private String error_info;

        /**
         * program_error
         */
        private String error_stack;


        /**
         * gson_error
         */
        private String error_source;


        public String getResult_type() {
            return result_type;
        }

        public void setResult_type(String result_type) {
            this.result_type = result_type;
        }

        public String getRequest_url() {
            return request_url;
        }

        public void setRequest_url(String request_url) {
            this.request_url = request_url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRequest_parameters() {
            return request_parameters;
        }

        public void setRequest_parameters(String request_parameters) {
            this.request_parameters = request_parameters;
        }

        public String getRequest_result() {
            return request_result;
        }

        public void setRequest_result(String request_result) {
            this.request_result = request_result;
        }

        public String getError_info() {
            return error_info;
        }

        public void setError_info(String error_info) {
            this.error_info = error_info;
        }

        public String getError_stack() {
            return error_stack;
        }

        public void setError_stack(String error_stack) {
            this.error_stack = error_stack;
        }

        public String getError_source() {
            return error_source;
        }

        public void setError_source(String error_source) {
            this.error_source = error_source;
        }
    }
}
