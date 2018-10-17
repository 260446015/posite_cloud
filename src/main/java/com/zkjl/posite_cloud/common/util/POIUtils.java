package com.zkjl.posite_cloud.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.enums.ExportEnum;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class POIUtils {

    private POIFSFileSystem fs;
    private HSSFWorkbook wb;
    private HSSFSheet sheet;
    private HSSFRow row;
    private XSSFWorkbook xwb;
    private XSSFSheet xsheet;
    private XSSFRow xrow;
//	@Autowired
//	private BaseElasticsearch baseElasticsearch;
//	@Autowired
//	private IndustryClassRepository industryClassRepository;

    /**
     * 读取xls Excel表格表头的内容
     *
     * @return String 表头内容的数组
     */
    public String[] readExcelTitleXls(InputStream is) {
        try {
            fs = new POIFSFileSystem(is);
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        String[] title = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            // title[i] = getStringCellValue(row.getCell((short) i));
            title[i] = getCellFormatValueXls(row.getCell(i));
        }
        return title;
    }

    /**
     * 读取xlsx Excel表格表头的内容
     *
     * @return String 表头内容的数组
     */
    public String[] readExcelTitleXlsx(InputStream is) {
        try {
            xwb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        xsheet = xwb.getSheetAt(0);
        xrow = xsheet.getRow(0);
        // 标题总列数
        int colNum = xrow.getPhysicalNumberOfCells();
        System.out.println("colNum:" + colNum);
        String[] title = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            // title[i] = getStringCellValue(row.getCell((short) i));
            title[i] = getCellFormatValueXlsx(xrow.getCell(i));
        }
        return title;
    }

    /**
     * 读取Excel数据内容
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public Map<Integer, String> readExcelContentXls(InputStream is, String split) {
        Map<Integer, String> content = new HashMap<Integer, String>();
        String str = "";
        try {
            fs = new POIFSFileSystem(is);
            wb = new HSSFWorkbook(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sheet = wb.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            while (j < colNum) {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                str += (getCellFormatValueXls(row.getCell(j)).equals("") ? " "
                        : getCellFormatValueXls(row.getCell(j))) + split;
                j++;
            }
            content.put(i, str);
            str = "";
        }
        return content;
    }

    /**
     * 读取Xlsx Excel数据内容
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public Map<Integer, String> readExcelContentXlsx(InputStream is, String split) {
        Map<Integer, String> content = new HashMap<Integer, String>();
        String str = "";
        try {
            xwb = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        xsheet = xwb.getSheetAt(0);
        // 得到总行数
        int rowNum = xsheet.getLastRowNum();
        xrow = xsheet.getRow(0);
        int colNum = xrow.getPhysicalNumberOfCells();
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
            xrow = xsheet.getRow(i);
            int j = 0;
            while (j < colNum) {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                str += (getCellFormatValueXlsx(xrow.getCell((short) j)).equals("") ? " "
                        : getCellFormatValueXlsx(xrow.getCell((short) j))) + split;
                j++;
            }
            content.put(i, str);
            str = "";
        }
        return content;
    }

    /**
     * 获取单元格数据内容为字符串类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private String getStringCellValueXls(HSSFCell cell) {
        String strCell = "";
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING:
                strCell = cell.getStringCellValue();
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                strCell = String.valueOf(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
                break;
        }
        if (strCell.equals("") || strCell == null) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }

    /**
     * 获取单元格数据内容为日期类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private String getDateCellValueXls(HSSFCell cell) {
        String result = "";
        try {
            int cellType = cell.getCellType();
            if (cellType == HSSFCell.CELL_TYPE_NUMERIC) {
                Date date = cell.getDateCellValue();
                result = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate();
            } else if (cellType == HSSFCell.CELL_TYPE_STRING) {
                String date = getStringCellValueXls(cell);
                result = date.replaceAll("[年月]", "-").replace("日", "").trim();
            } else if (cellType == HSSFCell.CELL_TYPE_BLANK) {
                result = "";
            }
        } catch (Exception e) {
            System.out.println("日期格式不正确!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private String getCellFormatValueXls(HSSFCell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        // 方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();

                        // 方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);

                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }

    /**
     * 根据XSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private String getCellFormatValueXlsx(XSSFCell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case XSSFCell.CELL_TYPE_NUMERIC:
                case XSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        // 方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();

                        // 方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);

                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case XSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }

    public static void writeExceXlsx(List<JSONObject> arr, String[] titles, HttpServletResponse response, HttpServletRequest request) throws Exception {
        //创建工作簿
        XSSFWorkbook workBook = new XSSFWorkbook();
        //创建工作表
        XSSFSheet sheet = workBook.createSheet("重点人员");
        XSSFRow titleRow = sheet.createRow(0);

        for (int i = 0; i < titles.length; i++) {
            XSSFCell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(titles[i]);
        }
        //创建单元格，操作第三行第三列
        for (int i = 1; i <= arr.size(); i++) {
            //创建行
            XSSFRow row = sheet.createRow(i);
            String temp = "";
            String value = "";
            try {
                JSONObject integerStringMap = arr.get(i - 1);
                for (Map.Entry<String, Object> entry : integerStringMap.entrySet()) {
                    temp = entry.getKey();
                    try {
                        value = entry.getValue().toString();
                    } catch (NullPointerException e) {
                    }
                    Integer filedDataIndex = ExportEnum.getFiledDataIndex(temp);
                    List<String> webnames = new ArrayList<>();
                    if (null != filedDataIndex) {
                        XSSFCell cell = row.createCell(filedDataIndex);
                        if (temp.equals("data")) {
                            JSONArray datas = (JSONArray) entry.getValue();
                            for (Object data : datas) {
                                JSONObject action = new JSONObject((Map<String, Object>) data);
                                String webname = action.getString("webname");
                                webnames.add(webname);
                            }
                            Object[] objects = webnames.toArray();
                            value = Arrays.toString(objects);
                        }
                        cell.setCellValue(value);
                    }
                }
            } catch (Exception e) {
                System.out.println(temp);
                e.printStackTrace();
            }
        }

        setExportHeader(response, request, workBook);
        workBook.close();//记得关闭工作簿

    }

    private static void setExportHeader(HttpServletResponse response, HttpServletRequest request, XSSFWorkbook workBook) {
        String fileName = DateUtils.getFormatStringByDay(Calendar.getInstance().getTime());
        OutputStream os = null;
        try {
            os = response.getOutputStream();
//            response.reset();
            // 设定输出文件头
//            response.setHeader("Content-disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "UTF-8") + ".xls");
            // 定义输出类型
//            response.setContentType("application/x-download;charset=utf-8");
            setHeader(request, response, fileName + ".xlsx");
            Map<String, String> header = new HashMap<>();
//            ExcelUtil.exportExcel(header,report,os);
            workBook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("11111");
    }

    private static void setHeader(HttpServletRequest request, HttpServletResponse response, String fileName) {
        try {
            if (response != null) {
                String filename = "";
                if (isIE(request)) {
                    filename = new String(fileName);
                    filename = URLEncoder.encode(filename, "UTF-8");
                } else {
                    filename = new String((fileName).getBytes(), "iso-8859-1");
                }
                String header = "attachment; filename=\"" + filename + "\"";
                response.reset();
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.setHeader("Content-Disposition", header);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isIE(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent").toUpperCase();
        boolean isIE = ((agent != null && agent.indexOf("MSIE") != -1)
                || (null != agent && -1 != agent.indexOf("LIKE GECKO"))); // 判断版本,后边是判断IE11的

        return isIE;
    }
}
