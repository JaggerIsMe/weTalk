package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.dto.UserContactSearchResultDto;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.service.UserContactApplyService;
import com.weTalk.service.UserContactService;
import com.weTalk.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactApplyService userContactApplyService;

    /**
     * 搜索好友或群组
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO search(HttpServletRequest request, @NotEmpty String contactId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactSearchResultDto resultDto = this.userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        return getSuccessResponseVO(resultDto);
    }

    /**
     * 申请添加好友或群组
     * @param request
     * @param contactId
     * @param applyInfo
     * @return
     */
    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest request, @NotEmpty String contactId, String applyInfo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        Integer joinType = this.userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        return getSuccessResponseVO(joinType);
    }

}
