package com.weTalk.entity.enums;

public enum GroupStatusEnum {
    NORMAL(1, "正常"),
    DISSOLUTION(0, "解散");

    private Integer status;
    private String decs;

    GroupStatusEnum(Integer status, String decs) {
        this.status = status;
        this.decs = decs;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDecs() {
        return decs;
    }

    public static GroupStatusEnum getByStatus(Integer status) {
        for (GroupStatusEnum item : GroupStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

}
