package com.company;

import com.company.utils.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {

    public static void main(String[] args) throws Exception {
        // 下载线程池
        ExecutorService executor = Executors.newFixedThreadPool(Configuration.DOWNLOAD_THREAD_NUM + 1);
        DownloadUtils downloadUtils = new DownloadUtils();
        downloadUtils.download(Configuration.url, executor);
    }
}
