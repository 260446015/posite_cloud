package com.zkjl.posite_cloud.service;

import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yindawei
 * @date 2018/8/10 9:31
 **/
public interface IFileService {

    JobDTO uploadPhoneNum(MultipartFile multipartFile, String username, String taskname) throws CustomerException;

    String uploadImg(HttpServletRequest req, MultipartFile multipartFile);
}
