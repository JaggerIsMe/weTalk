package com.weTalk.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 复制对象
 */
public class CopyTools {

    public static <T, S> List<T> copyList(List<S> sList, Class<T> tClass) {
        List<T> tList = new ArrayList<T>();
        for (S s : sList) {
            T t = null;
            try {
                t = tClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            tList.add(t);
        }
        return tList;
    }

    public static <T, S> T copy(S s, Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }

}
