package com.weTalk.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.query.*;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.GroupInfoMapper;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.mappers.UserInfoMapper;
import com.weTalk.service.UserContactService;
import com.weTalk.websocket.MessageHandler;
import jodd.util.ArraysUtil;
import org.springframework.stereotype.Service;

import com.weTalk.entity.po.UserContactApply;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.mappers.UserContactApplyMapper;
import com.weTalk.service.UserContactApplyService;
import com.weTalk.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 联系人申请 业务接口实现
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService {

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private MessageHandler messageHandler;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContactApply> findListByParam(UserContactApplyQuery param) {
        return this.userContactApplyMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserContactApplyQuery param) {
        return this.userContactApplyMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserContactApply> list = this.findListByParam(param);
        PaginationResultVO<UserContactApply> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContactApply bean) {
        return this.userContactApplyMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserContactApply bean, UserContactApplyQuery param) {
        StringTools.checkParam(param);
        return this.userContactApplyMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserContactApplyQuery param) {
        StringTools.checkParam(param);
        return this.userContactApplyMapper.deleteByParam(param);
    }

    /**
     * 根据ApplyId获取对象
     */
    @Override
    public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.selectByApplyId(applyId);
    }

    /**
     * 根据ApplyId修改
     */
    @Override
    public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
        return this.userContactApplyMapper.updateByApplyId(bean, applyId);
    }

    /**
     * 根据ApplyId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.deleteByApplyId(applyId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId获取对象
     */
    @Override
    public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId修改
     */
    @Override
    public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean, applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
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
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
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
        if (UserContactTypeEnum.GROUP == typeEnum) {
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
            userContactService.addContact(applyUserId, receiveUserId, contactId, typeEnum.getType(), applyInfo);
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
            //发送WebSocket信息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setMessageContent(applyInfo);
            messageSendDto.setContactId(receiveUserId);
            messageHandler.sendMessage(messageSendDto);
        }

        return joinType;
    }

    /**
     * 处理加好友或加群请求
     *
     * @param userId
     * @param applyId
     * @param status
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, Integer applyId, Integer status) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (null == statusEnum || UserContactApplyStatusEnum.WAIT == statusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //申请接收人不是当前用户
        UserContactApply applyInfo = this.userContactApplyMapper.selectByApplyId(applyId);
        if (null == applyInfo || !userId.equals(applyInfo.getReceiveUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserContactApply updateInfo = new UserContactApply();
        updateInfo.setStatus(statusEnum.getStatus());
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setApplyId(applyId);
        applyQuery.setStatus(UserContactApplyStatusEnum.WAIT.getStatus());
        /**
         * 乐观锁
         * 该SQL语句为：
         * update user_contact_apply set status = #{}, last_apply_time = #{} where apply_id = #{} and status = 0
         * 由最后的限制条件and status = 0；相当于一个版本号，限制了该字段的值只能从0修改为其它值
         */
        Integer count = userContactApplyMapper.updateByParam(updateInfo, applyQuery);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (UserContactApplyStatusEnum.PASS == statusEnum) {
            userContactService.addContact(applyInfo.getApplyUserId(), applyInfo.getReceiveUserId(), applyInfo.getContactId(), applyInfo.getContactType(), applyInfo.getApplyInfo());
            return;
        }

        if (UserContactApplyStatusEnum.BLACK == statusEnum) {
            Date curDate = new Date();
            UserContact userContact = new UserContact();
            userContact.setUserId(applyInfo.getApplyUserId());
            userContact.setContactId(applyInfo.getContactId());
            userContact.setContactType(applyInfo.getContactType());
            userContact.setCreateTime(curDate);
            //在待处理申请列表里（也就是在新的朋友列表里）拉黑对方的请求
            userContact.setStatus(UserContactStatusEnum.BLACK_BY_FRIEND_FIRST.getStatus());
            userContact.setLastUpdateTime(curDate);
            userContactMapper.insertOrUpdate(userContact);
        }

    }

}