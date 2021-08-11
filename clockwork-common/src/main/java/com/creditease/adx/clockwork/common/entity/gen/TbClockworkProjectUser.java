package com.creditease.adx.clockwork.common.entity.gen;

import java.util.Date;

public class TbClockworkProjectUser {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.project_id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private Long projectId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.user_name
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private String userName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.create_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private String createBy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.create_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.update_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private String updateBy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_clockwork_project_user.update_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    private Date updateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.id
     *
     * @return the value of tb_clockwork_project_user.id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.id
     *
     * @param id the value for tb_clockwork_project_user.id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.project_id
     *
     * @return the value of tb_clockwork_project_user.project_id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.project_id
     *
     * @param projectId the value for tb_clockwork_project_user.project_id
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.user_name
     *
     * @return the value of tb_clockwork_project_user.user_name
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public String getUserName() {
        return userName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.user_name
     *
     * @param userName the value for tb_clockwork_project_user.user_name
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.create_by
     *
     * @return the value of tb_clockwork_project_user.create_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.create_by
     *
     * @param createBy the value for tb_clockwork_project_user.create_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.create_time
     *
     * @return the value of tb_clockwork_project_user.create_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.create_time
     *
     * @param createTime the value for tb_clockwork_project_user.create_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.update_by
     *
     * @return the value of tb_clockwork_project_user.update_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public String getUpdateBy() {
        return updateBy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.update_by
     *
     * @param updateBy the value for tb_clockwork_project_user.update_by
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_clockwork_project_user.update_time
     *
     * @return the value of tb_clockwork_project_user.update_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_clockwork_project_user.update_time
     *
     * @param updateTime the value for tb_clockwork_project_user.update_time
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_clockwork_project_user
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TbClockworkProjectUser other = (TbClockworkProjectUser) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_clockwork_project_user
     *
     * @mbg.generated Tue Aug 04 15:51:46 CST 2020
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}