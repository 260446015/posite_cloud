package com.zkjl.posite_cloud.enums;

public enum MarkEnum {
    未处理(0),处理中(1),已处理(2),已通报(3);

    private Integer handleMark;
    MarkEnum(Integer handleMark) {
        this.handleMark = handleMark;
    }

    public static String getHandleMappingValue(Integer handleMark){
        MarkEnum[] values = MarkEnum.values();
        for (MarkEnum value : values) {
            if (value.handleMark == handleMark){
                return value.name();
            }
        }
        return null;
    }
}
