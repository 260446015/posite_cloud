package com.zkjl.posite_cloud.common;

public abstract class Constans {

    /**
     * 映射表
     */
    public static final String T_USER = "user";
    public static final String T_MOBILEDATAS = "mobiledatas";
    public static final String T_CREDITSWARN = "creditswarn";

    /**
     * 定义大小批量界限
     */
    public static final Integer BATCH_COUNT_MIN = 50;

    /**
     * 舆情URL
     */
    public static final String SENTIMENT_URL = "http://114.55.179.202:8199/restserver/index/query/fullQuery";

    /**
     * 设置二级用户上传数量
     */
    public static final Integer UPLOAD_COUNT = 500;

}
