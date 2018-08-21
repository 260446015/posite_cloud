package com.zkjl.posite_cloud.controller;

import com.zkjl.posite_cloud.common.ApiResult;
import com.zkjl.posite_cloud.common.SystemControllerLog;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IFileService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/10 9:29
 **/
@RestController
@RequestMapping(value = "api")
public class FileController extends BaseController {

    @Resource
    private IFileService fileService;

    /**
     * 文件上传
     * @param multipartFile
     * @return
     */
    @PostMapping(value = "file/upload")
    @RequiresPermissions(value = {"upload1","upload2"},logical = Logical.OR)
    @SystemControllerLog(description = "添加采集-文件上传")
    @ApiOperation(value = "文件上传")
    public ApiResult upload(HttpServletRequest req) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) req;
        List multipartFiles = multipartRequest
                .getFiles("file");// 得到所有的文件
        MultipartFile multipartFile = null;
        String taskname = req.getParameter("taskname");
        for (Object multipartFile1 : multipartFiles) {
            multipartFile = (MultipartFile) multipartFile1;
            if (multipartFile.getSize() <= 0L) {
                return null;
            }
        }
        String username = this.getCurrentUser().getUsername();
        if (StringUtils.isBlank(username)) {
            return error("用户名为空");
        }
        JobDTO jobDTO;
        try {
            jobDTO = fileService.uploadPhoneNum(multipartFile, username,taskname);
        } catch (CustomerException e) {
            return error("批量上传失败!"+e.getMessage());
        }
        return success(jobDTO);
    }
}
