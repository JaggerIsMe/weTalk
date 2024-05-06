package com.weTalk.service;

import java.util.List;

import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.dto.UserContactSearchResultDto;
import com.weTalk.entity.enums.UserContactStatusEnum;
import com.weTalk.entity.query.UserContactQuery;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.vo.PaginationResultVO;


/**
 * 联系人 业务接口
 */
public interface UserContactService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContact> findListByParam(UserContactQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserContactQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserContact> findListByPage(UserContactQuery param);

	/**
	 * 新增
	 */
	Integer add(UserContact bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserContact> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserContact> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserContact bean,UserContactQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserContactQuery param);

	/**
	 * 根据UserIdAndContactId查询对象
	 */
	UserContact getUserContactByUserIdAndContactId(String userId,String contactId);


	/**
	 * 根据UserIdAndContactId修改
	 */
	Integer updateUserContactByUserIdAndContactId(UserContact bean,String userId,String contactId);


	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteUserContactByUserIdAndContactId(String userId,String contactId);

	/**
	 *搜索联系人或群
	 * @param userId 发起搜索的ID
	 * @param contactId 被搜索的ID
	 */
	UserContactSearchResultDto searchContact(String userId, String contactId);

	/**
	 * 申请添加好友或群组
	 * Integer 返回加入的类型JoinTypeEnum 是直接添加成功还是等待审核中
	 * 返回的值用于前端的页面跳转
	 * @param tokenUserInfoDto
	 * @param contactId
	 * @param applyInfo
	 * @return
	 */
	Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo);

	/**
	 * 添加联系人
	 * @param appleUserId
	 * @param receiveUserId
	 * @param contactId
	 * @param contactType
	 * @param applyInfo
	 */
	void addContact(String appleUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo);

	/**
	 * 删除或拉黑联系人
	 * @param userId
	 * @param contactId
	 * @param statusEnum
	 */
	void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);

	/**
	 * 添加机器人好友
	 * @param userId
	 */
	void addContact4Robot(String userId);

}