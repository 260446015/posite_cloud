package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/10 9:29
 **/
@RestController
@RequestMapping(value = "api")
public class FileController extends BaseController {

    @Resource
    private IFileService fileService;

    @PostMapping(value = "file/upload")
    public ApiResult upload(MultipartFile multipartFile) {
        String username = this.getCurrentUser().getUsername();
        if (StringUtils.isBlank(username)) {
            return error("用户名为空");
        }
        JobDTO jobDTO;
        try {
            jobDTO = fileService.uploadPhoneNum(multipartFile, username);
        } catch (CustomerException e) {
            return error("批量上传失败!");
        }
        return success(jobDTO);
    }
}
