package com.creditease.adx.clockwork.common.pojo;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:04 2020/9/20
 * @ Description：TbClockworkDagPojo
 * @ Modified By：
 */
public class TbClockworkDagPojo extends TbClockworkDag {


    /**
     * 用户角色
     */
    private String roleName;

    /**
     * 创建者
     */
    private String createUser;

    @Override
    public String getName() {
        if( super.getName() == null ){
            return "NaN";
        }
        return super.getName();
    }

    @Override
    public String getDescription() {
        if( super.getDescription() == null ){
            return "NaN";
        }
        return super.getDescription();
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


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
}
