package com.weTalk.entity.enums;

public enum OnlineTypeEnum {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线");

    private Integer onlineType;
    private String desc;

    OnlineTypeEnum(Integer onlineType, String desc) {
        this.onlineType = onlineType;
        this.desc = desc;
    }

    public Integer getOnlineType() {
        return onlineType;
    }

    public String getDesc() {
        return desc;
    }

    public static OnlineTypeEnum getByOnlineType(Integer onlineType) {
        for (OnlineTypeEnum item : OnlineTypeEnum.values()) {
            if (item.getOnlineType().equals(onlineType)) {
                return item;
            }
        }
        return null;
    }

}
