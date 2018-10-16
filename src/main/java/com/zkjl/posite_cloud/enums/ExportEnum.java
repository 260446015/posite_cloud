package com.zkjl.posite_cloud.enums;

public enum ExportEnum {
    姓名("name", 0), 手机号("mobile", 1), 注册数量("registCount", 2), 注册App("data", 3), 危险积分("sorce", 4);

    private String filed;
    private Integer index;

    ExportEnum(String filed, Integer index) {
        this.filed = filed;
        this.index = index;
    }

    public static Integer getFiledDataIndex(String filed) {
        ExportEnum[] values = ExportEnum.values();
        for (ExportEnum exportEnum : values) {
            if (filed.equals(exportEnum.filed)) {
                return exportEnum.index;
            }
        }
        return null;
    }

    public static String[] getTitles(){
        ExportEnum[] values = ExportEnum.values();
        String[] titles  = new String[5];
        for (ExportEnum exportEnum : values) {
            titles[exportEnum.index] = exportEnum.name();
        }
        return titles;
    }
}
