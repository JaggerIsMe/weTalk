package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.entity.po.UserInfoBeauty;
import com.weTalk.entity.query.UserInfoBeautyQuery;
import com.weTalk.entity.query.UserInfoQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.service.UserInfoBeautyService;
import com.weTalk.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
public class BeautyInfoController extends ABaseController {

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    /**
     * 获取靓号列表
     *
     * @param beautyQuery
     * @return
     */
    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery beautyQuery) {
        beautyQuery.setOrderBy("id desc");
        PaginationResultVO resultVO = userInfoBeautyService.findListByPage(beautyQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 新增、修改、保存靓号
     * @param beautyAcc
     * @return
     */
    @RequestMapping("/saveBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautAccount(UserInfoBeauty beautyAcc) {
        userInfoBeautyService.saveAccount(beautyAcc);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除靓号
     * @param id
     * @return
     */
    @RequestMapping("/delBeautAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautAccount(@NotNull Integer id) {
        userInfoBeautyService.deleteUserInfoBeautyById(id);
        return getSuccessResponseVO(null);
    }

}
