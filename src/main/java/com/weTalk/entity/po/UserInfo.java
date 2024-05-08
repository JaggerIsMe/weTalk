package com.weTalk.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weTalk.entity.enums.DateTimePatternEnum;
import com.weTalk.entity.enums.OnlineTypeEnum;
import com.weTalk.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户信息表
 */
public class UserInfo implements Serializable {


    /**
     * 用户ID
     */
    private String userId;

    /**
     * 邮箱
     */
    private String email;

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
     * 密码
     */
    private String password;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 地区名
     */
    private String areaName;

    /**
     * 地区编号
     */
    private String areaCode;

    /**
     * 最后下线时间-精确到毫秒
     */
    private Long lastOffTime;

    /**
     * 用来标识用户的在线状态
     * lastLoginTime>lastOffTime时，该用户在线
     * <p>
     * 该属性不在数据库设计中
     */
    private Integer onlineType;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Integer getJoinType() {
        return this.joinType;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getSex() {
        return this.sex;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getPersonalSignature() {
        return this.personalSignature;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaName() {
        return this.areaName;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setLastOffTime(Long lastOffTime) {
        this.lastOffTime = lastOffTime;
    }

    public Long getLastOffTime() {
        return this.lastOffTime;
    }

    public Integer getOnlineType() {
        if (null != lastLoginTime && lastLoginTime.getTime() > lastOffTime) {
            return OnlineTypeEnum.ONLINE.getOnlineType();
        } else {
            return OnlineTypeEnum.OFFLINE.getOnlineType();
        }
    }

    public void setOnlineType(Integer onlineType) {
        this.onlineType = onlineType;
    }

    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，邮箱:" + (email == null ? "空" : email) + "，昵称:" + (nickName == null ? "空" : nickName) + "，0:直接加入 1:同意后加好友:" + (joinType == null ? "空" : joinType) + "，性别 0:女 1:男:" + (sex == null ? "空" : sex) + "，密码:" + (password == null ? "空" : password) + "，个性签名:" + (personalSignature == null ? "空" : personalSignature) + "，状态:" + (status == null ? "空" : status) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，最后登录时间:" + (lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，地区名:" + (areaName == null ? "空" : areaName) + "，地区编号:" + (areaCode == null ? "空" : areaCode) + "，最后下线时间-精确到毫秒:" + (lastOffTime == null ? "空" : lastOffTime);
    }
}
