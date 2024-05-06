package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.config.AppConfig;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.AppUpdateFileTypeEnum;
import com.weTalk.entity.po.AppUpdate;
import com.weTalk.entity.vo.AppUpdateVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.service.AppUpdateService;
import com.weTalk.utils.CopyTools;
import com.weTalk.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;

@RestController
@RequestMapping("/update")
public class UpdateController extends ABaseController {

    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppConfig appConfig;

    /**
     * 客户端检测更新
     *
     * @param appVersion 这里不能用@NotEmpty来判空，因为当没有可更新版本时，appVersion为空
     * @param uid
     * @return
     */
    @RequestMapping("/checkVersion")
    @GlobalInterceptor
    public ResponseVO checkVersion(String appVersion, String uid) {
        //没有可更新版本
        if (StringTools.isEmpty(appVersion)) {
            return getSuccessResponseVO(null);
        }
        AppUpdate appUpdate = appUpdateService.getLatestUpdate(appVersion, uid);
        if (null == appUpdate) {
            return getSuccessResponseVO(null);
        }

        AppUpdateVO updateVO = CopyTools.copy(appUpdate, AppUpdateVO.class);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())) {
            File file = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + appUpdate.getId() + appUpdate.getId() + Constants.APP_EXE_SUFFIX);
            updateVO.setSize(file.length());
        } else {
            updateVO.setSize(0L);
        }
        updateVO.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));
        String fileName = Constants.APP_NAME + appUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        updateVO.setFileName(fileName);
        return getSuccessResponseVO(updateVO);
    }


}
