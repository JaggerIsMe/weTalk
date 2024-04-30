package com.weTalk.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.weTalk.entity.enums.UserContactApplyStatusEnum;

import java.io.Serializable;


/**
 * 联系人申请
 */
public class UserContactApply implements Serializable {


    /**
     * 自增ID
     */
    private Integer applyId;

    /**
     * 申请人ID
     */
    private String applyUserId;

    /**
     * 接收人ID
     */
    private String receiveUserId;

    /**
     * 联系人类型 0:好友 1:群组(加好友申请还是加群申请)
     */
    private Integer contactType;

    /**
     * 联系人群组ID
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private Long lastApplyTime;

    /**
     * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
     */
    private Integer status;

    /**
     * 申请信息
     */
    private String applyInfo;

    /**
     * 该属性是user_contact_apply表和user_info表(或group_info表)的链接查询后
     * 获得的申请人的nick_name(或群组的group_name)
     * <p>
     * 该属性不在数据库表设计里
     */
    private String contactName;

    /**
     * 该属性用于给前端返回处理后的信息
     * 比如拒绝某个用户的申请， 前端会在该用户后面显示一个已拒绝的信息
     * <p>
     * 该属性不在数据库表设计里
     */
    private String statusName;

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public Integer getApplyId() {
        return this.applyId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserId() {
        return this.applyUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getReceiveUserId() {
        return this.receiveUserId;
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    public Integer getContactType() {
        return this.contactType;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactId() {
        return this.contactId;
    }

    public void setLastApplyTime(Long lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    public Long getLastApplyTime() {
        return this.lastApplyTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public String getApplyInfo() {
        return this.applyInfo;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getStatusName() {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum == null ? null : statusEnum.getDesc();
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Override
    public String toString() {
        return "自增ID:" + (applyId == null ? "空" : applyId) + "，申请人ID:" + (applyUserId == null ? "空" : applyUserId) + "，接收人ID:" + (receiveUserId == null ? "空" : receiveUserId) + "，联系人类型 0:好友 1:群组(加好友申请还是加群申请):" + (contactType == null ? "空" : contactType) + "，联系人群组ID:" + (contactId == null ? "空" : contactId) + "，最后申请时间:" + (lastApplyTime == null ? "空" : lastApplyTime) + "，状态 0:待处理 1:已同意 2:已拒绝 3:已被黑:" + (status == null ? "空" : status) + "，申请信息:" + (applyInfo == null ? "空" : applyInfo);
    }
}
