package com.zkjl.posite_cloud.domain.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author ydw
 * Created on 2018/5/22
 */
@Data
@Document(collection = "role")
public class Role implements Serializable{

    private static final long serialVersionUID = -6818460794496468926L;
    @Id
    private String id;
    private String rolename;
    private String permission;


}
