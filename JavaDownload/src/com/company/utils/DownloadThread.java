package com.company.utils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

public class DownloadThread implements Callable<Boolean> {
    // 每次读取的数据块大小
    private static int BYTE_SIZE = 1024 * 100;
    // 下载链接
    private String url;
    // 下载开始位置
    private long startPos;
    // 要下载的文件区块大小
    private Long endPos;
    // 标识多线程下载切分的第几部分
    private Integer part;

    public DownloadThread(String url, long startPos, long endPos, Integer part) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
    }

    @Override
    public Boolean call() throws Exception {
        if (url == null || url.trim().equals("")) {
            throw new RuntimeException("> 下载路径不正确");
        }
        // 文件名
        String httpFileName = HttpUtls.getHttpFileName(url);
        if (part != null) {
            httpFileName = httpFileName + Configuration.FILE_TEMP_SUFFIX + part;
        }
        // 本地文件大小
        long localFileContentLength = FileUtils.getFileContentLength(httpFileName);
        if (localFileContentLength >= endPos - startPos) {
            System.out.println("> " + httpFileName + " 已经下载完毕，无需重复下载");
            LogThread.DOWNLOAD_FINISH.addAndGet(1);
            return true;
        }
        HttpURLConnection httpUrlConnection = HttpUtls.getHttpUrlConnection(url, startPos + localFileContentLength, endPos);

        // 获得输入流
        //()是对资源的申请，如果{}中的代码出项了异常，资源就会被关闭
        try (InputStream input = httpUrlConnection.getInputStream();
             BufferedInputStream bis = new BufferedInputStream(input);
             RandomAccessFile oSavedFile = new RandomAccessFile(httpFileName, "rw")) {
            // 文件写入开始位置 localFileContentLength
            oSavedFile.seek(localFileContentLength);
            LogThread.DOWNLOAD_SIZE.addAndGet(localFileContentLength);
            byte[] buffer = new byte[BYTE_SIZE]; // 数组大小可根据文件大小设置a
            int len = -1;
            while ((len = bis.read(buffer)) != -1) { // 读到文件末尾则返回-1
                oSavedFile.write(buffer, 0, len);
                LogThread.DOWNLOAD_SIZE.addAndGet(len);
            }
        } catch (FileNotFoundException e) {
            System.out.println("\n> 下载文件路径不存在 " + url);
            return false;
        } catch (Exception e) {
            System.out.println("\n> 下载出现异常");
            e.printStackTrace();
            return false;
        } finally {
            httpUrlConnection.disconnect();
            LogThread.DOWNLOAD_FINISH.addAndGet(1);
        }
        return true;
    }
}
