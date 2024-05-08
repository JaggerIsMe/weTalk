package com.weTalk.service;

import java.io.IOException;
import java.util.List;

import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.enums.MessageTypeEnum;
import com.weTalk.entity.query.GroupInfoQuery;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 群组信息 业务接口
 */
public interface GroupInfoService {

    /**
     * 根据条件查询列表
     */
    List<GroupInfo> findListByParam(GroupInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(GroupInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param);

    /**
     * 新增
     */
    Integer add(GroupInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<GroupInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<GroupInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(GroupInfo bean, GroupInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(GroupInfoQuery param);

    /**
     * 根据GroupId查询对象
     */
    GroupInfo getGroupInfoByGroupId(String groupId);


    /**
     * 根据GroupId修改
     */
    Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId);


    /**
     * 根据GroupId删除
     */
    Integer deleteGroupInfoByGroupId(String groupId);

    /**
     * 新增、修改、保存群组信息
     *
     * @param groupInfo
     * @param avatarFile
     * @param avatarCover
     */
    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    /**
     * 解散群组
     *
     * @param groupOwnerId
     * @param groupId
     */
    void dissolutionGroup(String groupOwnerId, String groupId);

    /**
     * 管理员添加、移除群成员
     *
     * @param ownerInfoDto
     * @param groupId
     * @param selectUserIds
     * @param operationType
     */
    void addOrRemoveGroupMember(TokenUserInfoDto ownerInfoDto, String groupId, String selectUserIds, Integer operationType);

    /**
     * 离开群聊
     * @param userId
     * @param groupId
     * @param messageTypeEnum
     */
    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);

}