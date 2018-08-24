package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
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
    @Value(value = "${web.uploadpath}")
    private String uploadPath;

    @Override
    public JobDTO uploadPhoneNum(MultipartFile multipartFile, String username, String taskname) throws CustomerException {
        if (!checkFile(multipartFile)) {
            return null;
        }
        List<String> datas = readTxt(multipartFile);
        JobDTO param = createParam(username, datas, taskname);
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
    private JobDTO createParam(String username, List<String> datas, String taskname) {
        JobDTO jobDTO = new JobDTO();
        jobDTO.setUsername(username);
        jobDTO.setTaskname(taskname);
        jobDTO.setDatas(StringUtils.join(datas, ","));
        jobDTO.setStatus("start");
        return jobDTO;
    }

    @Override
    public String uploadImg(HttpServletRequest req, MultipartFile multipartFile) {
        String filePath = "";
        String myFileName = multipartFile.getOriginalFilename();// 文件原名称
        try {
            filePath = uploadPath + "/"+myFileName;
            filePath = filePath.replaceAll("\\/+","/");
            File sorceFile = new File(uploadPath);
            if(!sorceFile.exists()){
                sorceFile.mkdir();
            }
            File localFile = new File(filePath);
            multipartFile.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myFileName;
    }
}
