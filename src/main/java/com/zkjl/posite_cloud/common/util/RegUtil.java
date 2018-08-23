package com.zkjl.posite_cloud.common.util;

/**
 * @author yindawei
 * @date 2018/8/23 15:57
 **/
public class RegUtil {
    public static boolean checkMobile(String str){
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if(str.matches(regex)){
            return true;
        }
        return false;
    }
}
