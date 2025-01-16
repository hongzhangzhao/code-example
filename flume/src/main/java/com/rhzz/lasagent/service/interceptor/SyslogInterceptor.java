package com.rhzz.lasagent.service.interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.List;

public class SyslogInterceptor implements Interceptor {

    @Override
    public void initialize() {
    }

    @Override
    public Event intercept(Event event) {
//        String logMessage = new String(event.getBody());
//        System.out.println("--------- interceptro-------------");
//        System.out.println(">>>>>>>>>: " + logMessage);
//        if (logMessage.contains("ERROR")) {
//            event.getHeaders().put("logType", "error");
//        } else if (logMessage.contains("INFO")) {
//            event.getHeaders().put("logType", "info");
//        }
        return event;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        for (Event event : events) {
            intercept(event);
        }
        return events;
    }

    @Override
    public void close() {
    }

    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new SyslogInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }

}
