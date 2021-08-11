package com.creditease.adx.clockwork.master;

import com.creditease.adx.clockwork.common.enums.TaskStatus;
import org.junit.Test;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午7:48 2020/12/12
 * @ Description：
 * @ Modified By：
 */
public class T {

    @Test
    public void getDescByValueTest() {
        String failed = TaskStatus.getDescByValue("failed");
        System.out.println(failed);
    }
}
