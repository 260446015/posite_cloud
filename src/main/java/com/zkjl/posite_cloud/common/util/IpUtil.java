package com.zkjl.posite_cloud.common.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * @author yindawei
 * @date 2018/8/22 10:36
 **/
public class IpUtil {
    public static String getIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (java.net.UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress = inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getMyIP() throws IOException {
        InputStream ins = null;
        try {
            String l = "http://2018.ip138.com/ic.asp";
            URL url = new URL(l);
            URLConnection con = url.openConnection();
            ins = con.getInputStream();
            InputStreamReader isReader = new InputStreamReader(ins, "gb2312");
            BufferedReader bReader = new BufferedReader(isReader);
            StringBuffer webContent = new StringBuffer();
            String str = null;
            while ((str = bReader.readLine()) != null) {
                webContent.append(str);
            }
            System.out.println(webContent);
            int start = webContent.indexOf("[") + 1;
            int end = webContent.indexOf("]");
            return webContent.substring(start, end);
        } finally {
            if (ins != null) {
                ins.close();
            }
        }
    }

    /**
     * 读取
     *
     * @param rd
     * @return
     * @throws IOException
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 创建链接
     *
     * @param url
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = JSONObject.parseObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    /**
     * 百度获取城市信息
     *
     * @param ip
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static String baiduGetCityCode(String ip) throws JSONException, IOException {

//        String ip = "";
        //百度ak，自己去申请就可以
        String ak = "QczmwEiQ1VwUuZoqZtlNAj8UuqBk3xl2";
        //这里调用百度的ip定位api服务 详见 http://api.map.baidu.com/lbsapi/cloud/ip-location-api.htm
        JSONObject json = readJsonFromUrl("http://api.map.baidu.com/location/ip?ip=" + ip + "&ak=" + ak + "&coor=bd09ll");
        Object obj = ((JSONObject) json.get("content")).get("address_detail");
        String s = obj.toString();
        JSONObject j = JSONObject.parseObject(s);
        int city_code = (int) j.get("city_code");
        String code = String.valueOf(city_code);
        String city = j.getString("city");
        return city;
    }

    /**
     * 高德获取城市信息
     *
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static String guideGetCityCode() throws JSONException, IOException {

        String ip = "";
        //高德key
        String key = "";
        JSONObject json = readJsonFromUrl("http://restapi.amap.com/v3/ip?ip=" + ip + "&key=" + key + "");
        //String province = (String) json.get("province");
        // String city = (String) json.get("city");
        String adcode = (String) json.get("adcode");
        return adcode;
    }
}
