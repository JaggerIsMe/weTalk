package com.weTalk.utils;

import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.UserContactTypeEnum;
import com.weTalk.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;


public class StringTools {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof java.lang.String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof java.lang.String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 生成用户ID
     *
     * @return
     */
    public static String createUserId() {
        return UserContactTypeEnum.USER.getPrefix() + createRandomNum(Constants.LENGTH_11);
    }

    /**
     * 生成群组ID
     *
     * @return
     */
    public static String createGroupId() {
        return UserContactTypeEnum.GROUP.getPrefix() + createRandomNum(Constants.LENGTH_11);
    }

    /**
     * 生成纯数字字符串
     *
     * @param count
     * @return
     */
    public static String createRandomNum(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    /**
     * 生成字符串
     *
     * @param count
     * @return
     */
    public static String createRandomStr(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    /**
     * 使用MD5加密字符串
     *
     * @param originStr
     * @return
     */
    public static final String encodeByMd5(String originStr) {
        return StringTools.isEmpty(originStr) ? null : DigestUtils.md5Hex(originStr);
    }

    /**
     * 过滤一些Html标签
     * 防止注入
     *
     * @param content
     * @return
     */
    public static String cleanHtmlTag(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");
        return content;
    }

    /**
     * 生成两个用户之间的会话SessionID
     * @param userIds
     * @return
     */
    public static final String createChatSessionId4User(String[] userIds){
        Arrays.sort(userIds);
        return encodeByMd5(StringUtils.join(userIds, ""));
    }

    /**
     * 生成群聊的会话SessionID
     * @param groupId
     * @return
     */
    public static final String createChatSessionId4Group(String groupId){
        return encodeByMd5(groupId);
    }

}
