package com.company.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

public class LogThread implements Callable<Boolean> {
    public static AtomicLong DOWNLOAD_SIZE = new AtomicLong();
    public static AtomicLong DOWNLOAD_FINISH = new AtomicLong();
    private long httpFileContentLength;

    public LogThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    @Override
    public Boolean call() throws Exception {
        double size = 0;
        while (DOWNLOAD_FINISH.get() != Configuration.DOWNLOAD_THREAD_NUM) {
            double downloadSize = DOWNLOAD_SIZE.get();
            // 速度 = 大小/ 时间
            double speed = ((downloadSize - size) / 1024d) / 1d;
            size = downloadSize;
            double surplusTime = 0;
            if(speed > 0.001){
                surplusTime = (httpFileContentLength - downloadSize) / 1024d / speed;
            }
            Double fileSize = downloadSize / 1024d / 1024d;
            String speedLog = "> 已经下载大小 " + String.format("%.2f", fileSize) + "mb,当前下载速度:" + (int) speed + "kb/s" + ",估计剩余时间:" + String.format("%.1f", surplusTime) + "s";
            System.out.println(speedLog);
            Thread.sleep(1000);
        }
        return true;
    }
}
