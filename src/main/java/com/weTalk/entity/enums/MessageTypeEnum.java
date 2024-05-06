package com.weTalk.entity.enums;

public enum MessageTypeEnum {

    INIT(0, "", "连接WebSocket获取消息"),
    ADD_FRIEND(1, "", "添加好友打招呼消息"),
    CHAT(2, "", "普通聊天消息"),
    GROUP_CREATE(3, "群组已创建，可以一起聊天了", "群组创建成功"),
    CONTACT_APPLY(4, "", "好友申请"),
    MEDIA_CHAT(5, "", "媒体文件"),
    FILE_UPLOAD(6, "", "文件上传完成"),
    FORCE_OFF_LINE(7, "", "强制下线"),
    GROUP_DISSOLUTION(8, "群组已被解散", "解散群组"),
    ADD_GROUP(9, "%s加入了群组", "加入群组"),
    GROUP_NAME_UPDATE(10, "", "更新群昵称"),
    LEAVE_GROUP(11, "%s退出了群组", "退出群组"),
    BE_REMOVE_GROUP(12, "%s被管理员移出群组", "被管理员移出群组");

    private Integer type;
    private String initMessage;
    private String desc;

    MessageTypeEnum(Integer type, String initMessage, String desc) {
        this.type = type;
        this.initMessage = initMessage;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getInitMessage() {
        return initMessage;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageTypeEnum getByType(Integer type) {
        for (MessageTypeEnum item : MessageTypeEnum.values()) {
            if (item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }

}
