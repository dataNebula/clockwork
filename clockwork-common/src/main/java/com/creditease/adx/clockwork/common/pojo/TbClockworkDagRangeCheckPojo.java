package com.creditease.adx.clockwork.common.pojo;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheck;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @ClassName: TbClockworkDagRangeCheckPojo
 * @Author: ltb
 * @Date: 2021/3/10:1:50 下午
 * @Description:
 */
public class TbClockworkDagRangeCheckPojo extends TbClockworkDagRangeCheck {
    /*
     * 角色（逗号隔开）
     */
    private String roleName;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * @Description 查询的时间范围
     */
    private Date beginDate;
    private Date endDate;

    /**
     * 是否是管理员角色
     *
     * @return isAdmin
     */
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    /**
     * 是否是管理员角色
     *
     * @param isAdmin
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getUpdateTime() {
        return super.getUpdateTime();
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
