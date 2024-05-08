package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.query.UserInfoQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
public class AdminController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户信息
     *
     * @param userInfoQuery
     * @return
     */
    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO resultVO = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 更新用户状态
     *
     * @param request
     * @param status
     * @param userId
     * @return
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(HttpServletRequest request, @NotNull Integer status, @NotEmpty String userId) {
        TokenUserInfoDto adminUserDto = getTokenUserInfo(request);
        if (adminUserDto.getUserId().equals(userId)) {
            throw new BusinessException("不能更改自己的账号状态");
        }
        userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 强制用户下线
     *
     * @param request
     * @param userId
     * @return
     */
    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(HttpServletRequest request, @NotEmpty String userId) {
        TokenUserInfoDto adminUserDto = getTokenUserInfo(request);
        if (adminUserDto.getUserId().equals(userId)) {
            throw new BusinessException("不能强制自己下线");
        }
        userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }

}
