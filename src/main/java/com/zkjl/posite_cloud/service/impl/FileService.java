package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.domain.dto.JobDTO;
import com.zkjl.posite_cloud.exception.CustomerException;
import com.zkjl.posite_cloud.service.IFileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger logger = LoggerFactory.getLogger(FileService.class);

    @Override
    public JobDTO uploadPhoneNum(MultipartFile multipartFile, String username, String taskname) throws CustomerException {
        String checkFile = checkFile(multipartFile);
        List<String> datas;
        if (checkFile.equalsIgnoreCase("txt")) {
            datas = readTxt(multipartFile);
        }else{
            datas = readExcel(multipartFile);
        }
        JobDTO param = createParam(username, datas, taskname);
        return apiService.createJob(param);
    }

    private String checkFile(MultipartFile file) throws CustomerException {
        String fileType;
        //判断文件是否存在
        //获得文件名
        String fileName = file.getOriginalFilename();
        String substring = fileName.substring(fileName.lastIndexOf(".") + 1);
        //判断文件是否是excel文件
        if (substring.equalsIgnoreCase("txt") || substring.equalsIgnoreCase("csv")) {
            logger.info("正在对txt文件进行读取");
            fileType = "txt";
        } else if (substring.equalsIgnoreCase("doc") || substring.equalsIgnoreCase("docx")) {
            logger.info("正在对word文件进行读取");
            fileType = "word";
        } else if (substring.equalsIgnoreCase("xls") || substring.equalsIgnoreCase("xlsx")) {
            logger.info("正在对excel文件进行读取");
            fileType = "excel";
        } else {
            throw new CustomerException(fileName + "文件格式不支持");
        }
        return fileType;
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

    private static List<String> readExcel(MultipartFile file) {
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //根据每个sheet返回不同sheet的结合
        List<String> list = new ArrayList<>();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                Sheet sheet = workbook.getSheetAt(sheetNum);
                Boolean flag = null;
                for (Row row : sheet) {
                    String[] cells;
                    try {
                        cells = new String[row.getLastCellNum()];
                    } catch (Exception e) {
                        continue;
                    }

                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
                        try {
                            cell.setCellType(CellType.STRING);
                        } catch (Exception e) {
                        }
                        cells[i] = (cell == null ? " " : cell.getStringCellValue());
                    }
                    String join = StringUtils.join(cells, "`");
                    list.add(join);
                }
            }
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;

    }

    private static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith("xls")) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith("xlsx")) {
                //2007
//				workbook = new XSSFWorkbook(is);
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return workbook;
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
            filePath = uploadPath + "/" + myFileName;
            filePath = filePath.replaceAll("\\/+", "/");
            File sorceFile = new File(uploadPath);
            if (!sorceFile.exists()) {
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
