package com.weTalk.entity.enums;

public enum BeautyAccountStatusEnum {
    UNUSED(0, "未使用"),
    USED(1, "已使用");

    private Integer status;
    private String desc;

    BeautyAccountStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据状态获取枚举信息
     * @param status
     * @return
     */
    public static BeautyAccountStatusEnum getByStatus(Integer status) {
        for (BeautyAccountStatusEnum item : BeautyAccountStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

}
