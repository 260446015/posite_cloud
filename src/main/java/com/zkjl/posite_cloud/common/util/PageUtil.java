package com.zkjl.posite_cloud.common.util;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ydw
 * Created on 2018/6/23
 */
public class PageUtil {

    public static PageImpl<?> pageBeagin(int totalCount, int pageNum, int pageSize, List<?> data) {
        data = data.stream().skip(pageNum * pageSize).limit(pageSize).collect(Collectors.toList());
        /*data.forEach(action -> {
            Field[] fields = action.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                String s = fields[i].getGenericType().toString();
                if (Date.class.toString().equals(s)) {
                    String name = fields[i].getName();
                    char[] chars = name.toCharArray();
                    chars[0] -= 32;
                    String valueOf = String.valueOf(chars);
                    try {
                        Date invoke = (Date) action.getClass().getDeclaredMethod("get" + valueOf,null).invoke(action);
                        String formatString = DateUtils.getFormatString(invoke);
                        action.getClass().getDeclaredMethod("set" + valueOf,Date.class).invoke(action,formatString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        return new PageImpl<>(data, pageRequest, totalCount);
    }

}
