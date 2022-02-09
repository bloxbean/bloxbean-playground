package com.bloxbean.playground.hashcash;

import io.micronaut.http.HttpRequest;

public class RequestUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String getClientIpAddress(HttpRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeaders().get(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        System.out.println(request.getRemoteAddress().getHostString());
        return request.getRemoteAddress().getHostString();
//        return request.getRemoteAddr();
    }

}
