package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.common.Constans;
import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IFileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yindawei
 * @date 2018/8/10 9:33
 **/
@Service
public class FileService implements IFileService {
    @Resource
    private ApiService apiService;
    @Override
    public JobDTO uploadPhoneNum(MultipartFile multipartFile, String username) throws CustomerException {
        if (! checkFile(multipartFile)) {
            return null;
        }
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        int uploadCount = 0;
        if(loginUser.getPermission().contains("upload1")){
            uploadCount = Integer.MAX_VALUE;
        }else if(loginUser.getPermission().contains("upload2")){
            uploadCount = Constans.UPLOAD_COUNT;
        }
        List<String> datas = readTxt(multipartFile);
        if(datas.size() > uploadCount){
            throw new CustomerException("您无权限上传超过规定数量的文件!");
        }
        JobDTO param = createParam(username, datas);
        return apiService.createJob(param);
    }

    private boolean checkFile(MultipartFile multipartFile) {
        //获得文件名
        String fileName = multipartFile.getOriginalFilename();
        String substring = fileName.substring(fileName.lastIndexOf(".") + 1);
        //判断文件是否是txt文件
        if (!substring.endsWith("txt")) {
            return false;
        }
        return true;
    }

    private static List<String> readTxt(MultipartFile file) throws CustomerException {
        List<String> datas = new ArrayList<>();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));//传参
            String line = "";
            Boolean flag = null;
            while ((line = reader.readLine()) != null) {
                String data = line.trim();
                datas.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return datas;
    }

    /**
     * 构造请求参数
     */
    private JobDTO createParam(String username,List<String> datas){
        JobDTO jobDTO = new JobDTO();
        jobDTO.setUsername(username);
        jobDTO.setDatas(StringUtils.join(datas,","));
        jobDTO.setStatus("start");
        return jobDTO;
    }

}
