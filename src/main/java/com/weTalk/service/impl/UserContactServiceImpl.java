package com.weTalk.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.dto.UserContactSearchResultDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.po.UserContactApply;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.query.*;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.GroupInfoMapper;
import com.weTalk.mappers.UserContactApplyMapper;
import com.weTalk.mappers.UserInfoMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.service.UserContactApplyService;
import com.weTalk.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.stereotype.Service;

import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.service.UserContactService;
import com.weTalk.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 联系人 业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContact> findListByParam(UserContactQuery param) {
        return this.userContactMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserContactQuery param) {
        return this.userContactMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserContact> findListByPage(UserContactQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserContact> list = this.findListByParam(param);
        PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserContact bean, UserContactQuery param) {
        StringTools.checkParam(param);
        return this.userContactMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserContactQuery param) {
        StringTools.checkParam(param);
        return this.userContactMapper.deleteByParam(param);
    }

    /**
     * 根据UserIdAndContactId获取对象
     */
    @Override
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId修改
     */
    @Override
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    /**
     * 搜索联系人或群
     *
     * @param userId    发起搜索的ID
     * @param contactId 被搜索的ID
     * @return
     */
    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
        UserContcatTypeEnum typeEnum = UserContcatTypeEnum.getByPrefix(contactId);
        if (null == typeEnum) {
            return null;
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (typeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
                if (null == userInfo) {
                    return null;
                }
                resultDto = CopyTools.copy(userInfo, UserContactSearchResultDto.class);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
                if (null == groupInfo) {
                    return null;
                }
                resultDto.setNickName(groupInfo.getGroupName());
                break;
        }
        resultDto.setContactType(typeEnum.toString());
        resultDto.setContactId(contactId);

        //如果查询的是自己
        if (userId.equals(contactId)) {
            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDto;
        }
        //查询是否是好友
        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
        resultDto.setStatus(userContact == null ? null : userContact.getStatus());

        return resultDto;
    }

    /**
     * 申请添加好友或群组
     *
     * @param tokenUserInfoDto
     * @param contactId
     * @param applyInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        UserContcatTypeEnum typeEnum = UserContcatTypeEnum.getByPrefix(contactId);
        if (null == typeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //申请人
        String applyUserId = tokenUserInfoDto.getUserId();
        //默认申请信息
        applyInfo = StringTools.isEmpty(applyInfo) ? String.format(Constants.APPLY_INFO_TEMPLATE, tokenUserInfoDto.getNickName()) : applyInfo;

        Long curTime = System.currentTimeMillis();

        Integer joinType = null;
        String receiveUserId = contactId;

        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(applyUserId, contactId);
        //查询是否已经添加对方为好友或是否已经加入该群聊
        if (null != userContact && UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            switch (typeEnum) {
                case USER:
                    throw new BusinessException("对方已经是你的好友");
                case GROUP:
                    throw new BusinessException("你已经加入该群聊");
            }
        }
        //查询是否已被对方拉黑
        if (null != userContact && ArraysUtil.contains(new Integer[]{UserContactStatusEnum.BLACK_BY_FRIEND.getStatus(), UserContactStatusEnum.BLACK_BY_FRIEND_FIRST.getStatus()}, userContact.getStatus())) {
            throw new BusinessException("对方已将你拉黑，无法添加");
        }

        //如果是申请加群操作，先判断该群的状态或存不存在
        //然后将接收申请通知的对象Id设为该群的群主Id，并设置加群方式类型
        if (UserContcatTypeEnum.GROUP == typeEnum) {
            GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
            if (groupInfo == null || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException("群聊不存在或已解散");
            }
            receiveUserId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        } else {
            //如果是申请加好友操作，获取好友信息，设置加好友方式类型
            UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
            if (null == userInfo) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            joinType = userInfo.getJoinType();
        }
        //如果加群方式类型或加好友方式类型是直接通过不用审核
        if (JoinTypeEnum.NO_APPLY.getType().equals(joinType)) {
            this.addContact(applyUserId, receiveUserId, contactId, typeEnum.getType(), applyInfo);
            return joinType;
        }

        UserContactApply dbApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
        //查询之前是否已经发送过申请
        if (null == dbApply) {
            //第一次发送申请
            UserContactApply contactApply = new UserContactApply();
            contactApply.setApplyUserId(applyUserId);
            contactApply.setContactType(typeEnum.getType());
            contactApply.setReceiveUserId(receiveUserId);
            contactApply.setLastApplyTime(curTime);
            contactApply.setContactId(contactId);
            contactApply.setStatus(UserContactApplyStatusEnum.WAIT.getStatus());
            contactApply.setApplyInfo(applyInfo);
            this.userContactApplyMapper.insert(contactApply);
        } else {
            //之前发送过申请，更新状态
            UserContactApply contactApply = new UserContactApply();
            contactApply.setStatus(UserContactApplyStatusEnum.WAIT.getStatus());
            contactApply.setLastApplyTime(curTime);
            contactApply.setApplyInfo(applyInfo);
            this.userContactApplyMapper.updateByApplyId(contactApply, dbApply.getApplyId());
        }

        if (null == dbApply || !UserContactApplyStatusEnum.WAIT.getStatus().equals(dbApply.getStatus())) {
            //TODO 发送WebSocket信息

        }

        return joinType;
    }

    /**
     * 添加联系人
     * @param appleUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     * @param applyInfo
     */
    @Override
    public void addContact(String appleUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        //加群 判断群有没有满
        if (UserContcatTypeEnum.GROUP.getType().equals(contactType)){
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = userContactMapper.selectCount(userContactQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count>=sysSettingDto.getMaxGroupMemberCount()){
                throw new BusinessException("该群成员已满，无法加入");
            }
        }
        Date curDate = new Date();
        //若同意，双方添加好友
        List<UserContact> contactList  = new ArrayList<>();
        //申请人添加对方
        UserContact userContact = new UserContact();
        userContact.setUserId(appleUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        contactList.add(userContact);
        //如果是申请好友，接受人也要添加申请人；如果是申请加群，则接受人不需要添加申请人
        if (UserContcatTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(receiveUserId);
            userContact.setContactId(appleUserId);
            userContact.setContactType(contactType);
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
        }
        //批量插入
        userContactMapper.insertOrUpdateBatch(contactList);

        //TODO 如果是好友，接受人也添加申请人为好友 添加缓存

        //TODO 创建会话 发送消息

    }

    /**
     * 删除或拉黑联系人
     *
     * @param userId
     * @param contactId
     * @param statusEnum
     */
    @Override
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
        //移除好友
        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());
        userContactMapper.updateByUserIdAndContactId(userContact, userId, contactId);

        //反过来 也要在好友的联系人列表中更新我的状态
        UserContact friendContact = new UserContact();
        if (UserContactStatusEnum.DEL_FRIEND == statusEnum) {
            friendContact.setStatus(UserContactStatusEnum.DEL_BY_FRIEND.getStatus());
        } else if (UserContactStatusEnum.BLACK_FRIEND == statusEnum) {
            friendContact.setStatus(UserContactStatusEnum.BLACK_BY_FRIEND.getStatus());
        }
        userContactMapper.updateByUserIdAndContactId(friendContact, contactId, userId);

        //TODO 从我的好友列表缓存中删除该好友

        //TODO 从好友的好友列表缓存中删除我

    }
}