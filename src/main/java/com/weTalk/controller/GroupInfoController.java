package com.weTalk.controller;

import java.io.IOException;
import java.util.List;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.query.GroupInfoQuery;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 群组信息 Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
public class GroupInfoController extends ABaseController{

	@Resource
	private GroupInfoService groupInfoService;

	/**
	 * 创建或修改群组
	 * @param request
	 * @param groupId
	 * @param groupName
	 * @param groupNotice
	 * @param joinType
	 * @param avatarFile
	 * @param avatarCover
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/saveGroup")
	@GlobalInterceptor
	public ResponseVO saveGroup(HttpServletRequest request,
								String groupId,
								@NotEmpty String groupName,
								String groupNotice,
								@NotNull Integer joinType,
								MultipartFile avatarFile,
								MultipartFile avatarCover) throws IOException {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		GroupInfo groupInfo = new GroupInfo();
		groupInfo.setGroupId(groupId);
		groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfo.setGroupName(groupName);
		groupInfo.setGroupNotice(groupNotice);
		groupInfo.setJoinType(joinType);
		this.groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);

		return getSuccessResponseVO(null);
	}

	/**
	 * 获取我创建的群组
	 * @param request
	 * @return
	 */
	@RequestMapping("/loadMyGroup")
	@GlobalInterceptor
	public ResponseVO loadMyGroup(HttpServletRequest request) {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
		groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfoQuery.setOrderBy("create_time desc");
		List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
		return getSuccessResponseVO(groupInfoList);
	}

}