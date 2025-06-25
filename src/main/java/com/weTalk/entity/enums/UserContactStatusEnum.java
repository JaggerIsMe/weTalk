package com.weTalk.entity.enums;

import com.weTalk.utils.StringTools;

/**
 * 联系人状态类型
 */
public enum UserContactStatusEnum {
    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL_FRIEND(2, "已删除的好友"),
    DEL_BY_FRIEND(3, "已被该好友删除"),
    BLACK_FRIEND(4, "已拉黑的好友"),
    BLACK_BY_FRIEND(5, "已被该好友拉黑"),
    BLACK_BY_FRIEND_FIRST(6, "申请时被该好友拉黑");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public static UserContactStatusEnum getByName(String name) {
        try {
            if (StringTools.isEmpty(name)) {
                return null;
            }
            return UserContactStatusEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
