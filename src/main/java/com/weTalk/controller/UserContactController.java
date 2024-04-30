package com.weTalk.controller;

import com.sun.istack.internal.NotNull;
import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.dto.UserContactSearchResultDto;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.query.UserContactApplyQuery;
import com.weTalk.entity.query.UserContactQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.service.UserContactApplyService;
import com.weTalk.service.UserContactService;
import com.weTalk.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactApplyService userContactApplyService;

    /**
     * 搜索好友或群组
     *
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO search(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactSearchResultDto resultDto = this.userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        return getSuccessResponseVO(resultDto);
    }

    /**
     * 申请添加好友或群组
     *
     * @param request
     * @param contactId
     * @param applyInfo
     * @return
     */
    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest request, @NotEmpty String contactId, String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        Integer joinType = this.userContactService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        return getSuccessResponseVO(joinType);
    }

    /**
     * 加载获取申请列表
     *
     * @param request
     * @param pageNo
     * @return
     */
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResponseVO loadApply(HttpServletRequest request, Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());

        //为true则user_contact_apply表和user_info表(或group_info表)的链接查询
        //以获取申请人的nick_name(或群组的group_name)
        applyQuery.setQueryContactInfo(true);
        PaginationResultVO resultVO = userContactApplyService.findListByPage(applyQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 处理加好友或加群请求
     * @param request
     * @param applyId
     * @param status
     * @return
     */
    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(HttpServletRequest request, @NotNull Integer applyId, @NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        this.userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(), applyId, status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO loadContact(HttpServletRequest request, @NotEmpty String contactType) {
        UserContcatTypeEnum contactTypeEnum = UserContcatTypeEnum.getByName(contactType);
        if (null == contactTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(tokenUserInfoDto.getUserId());
        contactQuery.setContactType(contactTypeEnum.getType());
        if (UserContcatTypeEnum.USER == contactTypeEnum) {
            contactQuery.setQueryContactUserInfo(true);
        } else if (UserContcatTypeEnum.GROUP == contactTypeEnum) {
            contactQuery.setQueryGroupInfo(true);
            contactQuery.setQueryExcludeMyGroup(true);
        }
        contactQuery.setOrderBy("last_update_time desc");
        contactQuery.setQueryStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BY_FRIEND.getStatus(),
                UserContactStatusEnum.BLACK_BY_FRIEND.getStatus()
        });
        List<UserContact> contactList = userContactService.findListByParam(contactQuery);
        return getSuccessResponseVO(contactList);
    }

}
