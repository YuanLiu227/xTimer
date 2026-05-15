package com.yuanliu.xtimer.dto;

import java.util.Map;

/**
 * ClassName:
 * Package: com.yuanliu.xtimer.dto
 * Description:
 *
 * @Author Yuan Liu
 * @Create 2026/5/12 14:14
 * @Version 1.0
 */
public class NotifyHTTPParam {
    private String method;
    private String url;
    private Map<String,String> header;
    private String body;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
