package com.weTalk.entity.enums;

public enum OperationTypeEnum {

    REMOVE(0, "移除"),
    ADD(1, "添加");

    private Integer operationType;
    private String desc;

    OperationTypeEnum(Integer operationType, String desc) {
        this.operationType = operationType;
        this.desc = desc;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public String getDesc() {
        return desc;
    }

    public static OperationTypeEnum getByOperationType(Integer operationType) {
        for (OperationTypeEnum item : OperationTypeEnum.values()) {
            if (item.getOperationType().equals(operationType)) {
                return item;
            }
        }
        return null;
    }

}
