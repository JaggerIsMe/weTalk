package com.weTalk.entity.constants;

import com.weTalk.entity.enums.UserContcatTypeEnum;

public class Constants {

    public static final String ROBOT_UID = UserContcatTypeEnum.USER.getPrefix() + "_Robot";

    public static final String REDIS_KEY_CHECK_CODE = "weTalk:checkCode:";

    public static final String REDIS_KEY_WS_USER_HEARTBEAT = "weTalk:ws:user:heartbeat:";

    public static final String REDIS_KEY_WS_TOKEN = "weTalk:ws:token:";

    public static final String REDIS_KEY_WS_TOKEN_USERID = "weTalk:ws:token:userid:";

    public static final String REDIS_KEY_SYS_SETTING = "weTalk:syssetting:";

    public static final String FILE_FOLDER_FILE = "file/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String IMAGE_SUFFIX = ".png";

    public static final String COVER_IMAGE_SUFFIX = "_cover.png";

    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String REGEX_PASSWORD = "^(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    public static final String APP_UPDATE_FOLDER = "/app/";

    public static final String APP_EXE_SUFFIX = ".exe";

    public static final String APP_NAME = "WeTalkSetup.";
    //用户联系人列表

    public static final String REDIS_KEY_USER_CONTACT = "weTalk:ws:user:contact:";

    public static final Integer REDIS_TIME_1MIN = 60;

    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_TIME_1MIN * 60 * 24;

    public static final Integer REDIS_KEY_TOKEN_EXPIRES = REDIS_KEY_EXPIRES_DAY * 2;

    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    public static final Integer LENGTH_11 = 11;

    public static final Integer LENGTH_20 = 20;

    public static final Long MILLI_SECOND_3DAYS_AGO = 60 * 60 * 24 * 3 * 1000L;

}
