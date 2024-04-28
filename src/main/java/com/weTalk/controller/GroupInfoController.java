package com.weTalk.controller;

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

	@RequestMapping("/saveGroup")
	@GlobalInterceptor
	public ResponseVO saveGroup(HttpServletRequest request,
								String groupId,
								@NotEmpty String groupName,
								String groupNotice,
								@NotNull Integer joinType,
								MultipartFile avatarFile,
								MultipartFile avatarCover){
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);


		return getSuccessResponseVO(null);
	}

}