package com.company.utils;

import java.io.File;

public interface FileUtils {
    static long getFileContentLength(String name) {
        File file = new File(name);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
