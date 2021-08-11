package com.creditease.adx.clockwork.common.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 7:31 下午 2020/12/2
 * @ Description：
 * @ Modified By：
 */
public class FileUtil {

    /**
     * 获取文件名称
     *
     * @param path path
     * @return
     */
    public static List<String> getPathFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        if (!file.exists()) {
            return files;
        }

        File[] tempList = file.listFiles();

        if (tempList != null && tempList.length != 0) for (File value : tempList) {
            if (value.isFile()) {
                files.add(value.getAbsolutePath());
            }

        }
        return files;
    }


}
