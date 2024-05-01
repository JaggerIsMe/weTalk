package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.entity.vo.UserInfoVO;
import com.weTalk.service.UserInfoService;
import com.weTalk.utils.CopyTools;
import com.weTalk.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 更新用户信息
     * @param request
     * @param userInfo
     * @param avatarFile
     * @param avatarCover
     * @return
     * @throws IOException
     */
    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResponseVO saveUserInfo(HttpServletRequest request, UserInfo userInfo,
                                   MultipartFile avatarFile,
                                   MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        //这些置空的操作是为了防止恶意注入导致用户某些字段值被篡改
        userInfo.setPassword(null);
        userInfo.setStatus(null);
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);

        this.userInfoService.updateByUserInfo(userInfo, avatarFile, avatarCover);
        //修改保存后，返回新的个人信息
        return getUserInfo(request);
    }

    /**
     * 更改密码
     * @param request
     * @param password
     * @return
     */
    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request,
                                     @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringTools.encodeByMd5(password));
        this.userInfoService.updateUserInfoByUserId(userInfo, tokenUserInfoDto.getUserId());
        //TODO 强制退出 重新登录

        return getSuccessResponseVO(null);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

        //TODO 强制退出 关闭WebSocket链接

        return getSuccessResponseVO(null);
    }

}
