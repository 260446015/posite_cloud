package com.zkjl.posite_cloud.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author yindawei
 * @date 2018/8/14 10:40
 **/
public class RequestUtils {
    private static Logger logger = LoggerFactory.getLogger(RequestUtils.class);

    public static JSONObject sendPost(String url, String json) {
        JSONObject result = null;
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String body = EntityUtils.toString(entity, "utf-8");
                result = (JSONObject) JSONObject.parse(body);
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            logger.error("请求舆情接口出错", e);
        }
        return result;
    }
}
