package com.creditease.adx.clockwork.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @ClassName: DagCheckRangeInfo
 * @Author: ltb
 * @Date: 2021/3/11:3:03 下午
 * @Description:
 */
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DagCheckRangeInfo {
    private boolean isrange;
    private String taskInfos;
}
