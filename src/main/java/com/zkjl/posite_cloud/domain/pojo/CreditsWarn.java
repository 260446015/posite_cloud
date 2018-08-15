package com.zkjl.posite_cloud.domain.pojo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/8/14 17:40
 **/
@Document(collection = "creditswarn")
@Data
public class CreditsWarn implements Serializable {
    private static final long serialVersionUID = 488033841205616894L;

    @Id
    private String id;
    /**
     * 赌博
     */
    private JSONObject gamble;
    /**
     * 贷款
     */
    private JSONObject loans;
    /**
     * 涉黄
     */
    private JSONObject yellow;
    /**
     * 直播
     */
    private JSONObject living;
    /**
     * 游戏
     */
    private JSONObject game;
    /**
     * 红色警戒界限
     */
    private Integer redSorce;
    /**
     * 橙色警戒界限
     */
    private Integer yellowSorce;
    /**
     * 蓝色警戒界限
     */
    private Integer blueSorce;
}
