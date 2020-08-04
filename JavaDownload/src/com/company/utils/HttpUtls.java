package com.company.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface HttpUtls {
    static HttpURLConnection getHttpUrlConnection(String url) throws Exception {
        HttpURLConnection httpConnection = null;
        URL httpUrl = new URL(url);
        httpConnection = (HttpURLConnection) httpUrl.openConnection();
        httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
        httpConnection.setReadTimeout(2000);
        return httpConnection;
    }

    static HttpURLConnection getHttpUrlConnection(String url, long start, Long end) throws Exception {
        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
        if (end != null) {
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + start + "-" + end + "/*");
        } else {
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + start + "-");
        }
        return httpUrlConnection;
    }

    static long getHttpFileContentLength(String url) throws Exception {
        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
        int contentLength = httpUrlConnection.getContentLength();
        httpUrlConnection.disconnect();
        return contentLength;
    }

    static String getHttpFileEtag(String url) throws Exception {
        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
        Map<String, List<String>> headerFields = httpUrlConnection.getHeaderFields();
        List<String> eTagList = headerFields.get("ETag");
        return eTagList.get(0);
    }

    static String getHttpFileName(String url) {
        int indexOf = url.lastIndexOf("/");
        return url.substring(indexOf + 1);
    }
}
