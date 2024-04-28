package com.weTalk.dto;

import java.io.Serializable;

/**
 * 实现Serializable接口的目的是为类可持久化，比如在网络传输或本地存储，为系统的分布和异构部署提供先决条件。
 * 若没有序列化，现在我们所熟悉的远程调用，对象数据库都不可能存在
 *
 * 因为该系统没有Session，所以把用户信息存储在Redis，需要把用户对象序列化
 */
public class TokenUserInfoDto implements Serializable {

    /**
     * serialVersionUID适用于java序列化机制。简单来说，JAVA序列化的机制是通过 判断类的serialVersionUID来验证的版本一致的。
     * serialVersionUID的存在是为了处理序列化和反序列化过程中的版本兼容性问题。
     * 在进行反序列化时，JVM会把传来的字节流中的serialVersionUID于本地相应实体类的serialVersionUID进行比较。
     * 如果相同说明是一致的，可以进行反序列化，否则会出现反序列化版本一致的异常，即是InvalidCastException。
     * 当一个类被序列化后，它的字节表示可能会存储在磁盘上或通过网络传输到不同的 JVM（Java 虚拟机）。
     * 在这种情况下，如果类的结构发生了变化，例如添加了新的字段或方法，那么反序列化时就可能出现版本不一致的问题。
     */
    private static final long serialVersionUID = 6007891589045841415L;

    private String token;
    private String userId;
    private String nickName;
    private Boolean isAdmin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
