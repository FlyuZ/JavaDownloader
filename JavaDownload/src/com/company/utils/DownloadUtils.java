package com.company.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

public class DownloadUtils {
    public void download(String url, ExecutorService executor) throws Exception {
        String fileName = HttpUtls.getHttpFileName(url);
        long localFileSize = FileUtils.getFileContentLength(fileName);
        // 获取网络文件具体大小
        long httpFileContentLength = HttpUtls.getHttpFileContentLength(url);
//        System.out.println("本地文件大小" + localFileSize);
//        System.out.println("网络文件大小" + httpFileContentLength);
        if (localFileSize >= httpFileContentLength) {
            System.out.println("> " + fileName + "已经下载完毕，无需重新下载");
            return;
        }
        List<Future<Boolean>> futureList = new ArrayList<>();
        if (localFileSize > 0) {
            System.out.println("> 开始断点续传 " + fileName);
        } else {
            System.out.println("> 开始下载文件 " + fileName);
        }
        System.out.println("> 开始下载时间 " + LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        // 任务切分
        long size = httpFileContentLength / Configuration.DOWNLOAD_THREAD_NUM;
        long lastSize = httpFileContentLength - (httpFileContentLength / Configuration.DOWNLOAD_THREAD_NUM * (Configuration.DOWNLOAD_THREAD_NUM - 1));
        for (int i = 0; i < Configuration.DOWNLOAD_THREAD_NUM; i++) {
            long start = i * size;
            long downloadWindow = (i == Configuration.DOWNLOAD_THREAD_NUM - 1) ? lastSize : size;
            long end = start + downloadWindow;
            if (start != 0) {
                start++;
            }
            DownloadThread downloadThread = new DownloadThread(url, start, end, i);
            Future<Boolean> future = executor.submit(downloadThread);
            futureList.add(future);
        }
        LogThread logThread = new LogThread(httpFileContentLength);
        Future<Boolean> future = executor.submit(logThread);
        futureList.add(future);
        // 开始下载
        for (Future<Boolean> booleanFuture : futureList) {
            booleanFuture.get();
        }
        System.out.println("> 文件下载完毕 " + fileName + "，本次下载耗时：" + (System.currentTimeMillis() - startTime) / 1000 + "s");
        System.out.println("> 结束下载时间 " + LocalDateTime.now());
        // 文件合并
        boolean merge = merge(fileName);
        if (merge) {
            // 清理分段文件
            clearTemp(fileName);
        }
        System.out.println("> 本次文件下载结束");
        System.exit(0);
    }

    public boolean merge(String fileName) throws IOException {
        System.out.println("> 开始合并文件 " + fileName);
        byte[] buffer = new byte[1024 * 10];
        int len = -1;
        try (RandomAccessFile oSavedFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < Configuration.DOWNLOAD_THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + Configuration.FILE_TEMP_SUFFIX + i))) {
                    while ((len = bis.read(buffer)) != -1) { // 读到文件末尾则返回-1
                        oSavedFile.write(buffer, 0, len);
                    }
                }
            }
            System.out.println("> 文件合并完毕 " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void clearTemp(String fileName) {
        System.out.println("> 开始清理临时文件 " + fileName + Configuration.FILE_TEMP_SUFFIX + "0-" + (Configuration.DOWNLOAD_THREAD_NUM - 1));
        for (int i = 0; i < Configuration.DOWNLOAD_THREAD_NUM; i++) {
            File file = new File(fileName + Configuration.FILE_TEMP_SUFFIX + i);
            file.delete();
        }
        System.out.println("> 临时文件清理完毕 " + fileName + Configuration.FILE_TEMP_SUFFIX + "0-" + (Configuration.DOWNLOAD_THREAD_NUM - 1));
    }

    /**
     * 使用CheckedInputStream计算CRC
     */
    public static Long getCRC32(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1024];
        int cnt;
        while ((cnt = inputStream.read(bytes)) != -1) {
            crc.update(bytes, 0, cnt);
        }
        inputStream.close();
        return crc.getValue();
    }
}
