package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.query.GroupInfoQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    /**
     * 获取所有群组列表
     * @param query
     * @return
     */
    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery query) {
        query.setOrderBy("create_time desc");
        //进行子查询获取群主昵称
        query.setQueryGroupOwnerName(true);
        //进行子查询获取群员总数
        query.setQueryMemberCount(true);
        PaginationResultVO resultVO = groupInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 管理员解散群组
     * @param groupId
     * @return
     */
    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId) {
        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if (null == groupInfo){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId);
        return getSuccessResponseVO(null);
    }

}
