package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.config.AppConfig;
import com.weTalk.dto.SysSettingDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.query.GroupInfoQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.redis.RedisComponent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    /**
     * 获取系统设置
     *
     * @return
     */
    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting() {
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }

    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SysSettingDto sysSettingDto,
                                     MultipartFile robotAvatar,
                                     MultipartFile robotAvatarCover) throws IOException {
        if (null != robotAvatar) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            robotAvatar.transferTo(new File(filePath));
            robotAvatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }

}
