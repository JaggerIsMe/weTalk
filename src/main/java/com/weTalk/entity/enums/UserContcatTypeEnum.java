package com.weTalk.entity.enums;

import com.weTalk.utils.StringTools;

/**
 * 联系人类型
 */
public enum UserContcatTypeEnum {
    USER(0, "U", "好友"),
    GROUP(1, "G", "群组");

    private Integer type;
    private String prefix;
    private String desc;

    UserContcatTypeEnum(Integer type, String prefix, String desc) {
        this.type = type;
        this.prefix = prefix;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据名字获取对应枚举值
     * 例子：
     * getByName("USER") 该方法返回USER对应枚举值
     * @param name
     * @return
     */
    public static UserContcatTypeEnum getByName(String name) {
        try {
            if (StringTools.isEmpty(name)) {
                return null;
            }
            return UserContcatTypeEnum.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据ID的前缀判断该实体是好友还是群组
     * 例子：
     * 当参数是 U1234567890时 会根据前缀 U 判断出当前实体是 USER
     * 当参数是 G1234567890时 会根据前缀 G 判断出当前实体是 GROUP
     * @param prefix
     * @return
     */
    public static UserContcatTypeEnum getByPrefix(String prefix) {
        try {
            if (StringTools.isEmpty(prefix) || prefix.trim().length() == 0) {
                return null;
            }
            prefix = prefix.substring(0, 1);
            for (UserContcatTypeEnum typeEnum : UserContcatTypeEnum.values()) {
                if (typeEnum.getPrefix().equals(prefix)) {
                    return typeEnum;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
