package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author yindawei
 * @date 2018/10/10 16:30
 **/
@Document(collection = "notice")
@Data
public class Notice implements Serializable {

    private static final long serialVersionUID = -6553881767403987823L;

    @Id
    private String id;
    private String title;
    private String content;
    private String creationTime;
}
