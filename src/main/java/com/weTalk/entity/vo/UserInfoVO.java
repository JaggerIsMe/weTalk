package com.weTalk.entity.vo;

import java.io.Serializable;

public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 4607694350655801016L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:直接加入 1:同意后加好友
     */
    private Integer joinType;

    /**
     * 性别 0:女 1:男
     */
    private Integer sex;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    private String token;

    private Boolean isAdmin;

    private Integer contactStatus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Integer getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(Integer contactStatus) {
        this.contactStatus = contactStatus;
    }
}
