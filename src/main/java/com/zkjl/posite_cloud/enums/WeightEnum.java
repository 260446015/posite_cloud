package com.zkjl.posite_cloud.enums;

/**
 * @author yindawei
 * @date 2018/8/13 9:23
 **/
public enum WeightEnum {

    赌博("赌博", 10), 贷款("贷款", 7), 涉黄("涉黄", 5), 直播("直播", 1), 游戏("游戏", 2);

    private String str;
    private int sorce;
    WeightEnum(String str, int sorce) {
        this.str = str;
        this.sorce = sorce;
    }

    public static int getSorce(String str) {
        WeightEnum[] values = WeightEnum.values();
        for (WeightEnum weightEnum : values) {
            if (str.equals(weightEnum.str)) {
                return weightEnum.sorce;
            }
        }
        return 0;
    }

}
