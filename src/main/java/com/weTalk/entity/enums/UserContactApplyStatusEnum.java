package com.weTalk.entity.enums;

import com.weTalk.utils.StringTools;

public enum UserContactApplyStatusEnum {
    WAIT(0, "待处理"),
    PASS(1, "已同意"),
    REJECT(2, "已拒绝"),
    BLACK(3, "已被拉黑");

    private Integer status;
    private String desc;

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactApplyStatusEnum getByName(String name) {
        try {
            if (StringTools.isEmpty(name)) {
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status){
        for (UserContactApplyStatusEnum item : UserContactApplyStatusEnum.values()) {
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }

}
