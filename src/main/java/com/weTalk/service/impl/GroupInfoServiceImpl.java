package com.weTalk.service.impl;

import com.weTalk.config.AppConfig;
import com.weTalk.dto.SysSettingDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.PageSize;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.entity.enums.UserContactStatusEnum;
import com.weTalk.entity.enums.UserContcatTypeEnum;
import com.weTalk.entity.po.GroupInfo;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.query.GroupInfoQuery;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.entity.query.UserContactQuery;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.GroupInfoMapper;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.service.GroupInfoService;
import com.weTalk.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * 群组信息 业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(param);
        PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(GroupInfoQuery param) {
        StringTools.checkParam(param);
        return this.groupInfoMapper.deleteByParam(param);
    }

    /**
     * 根据GroupId获取对象
     */
    @Override
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId修改
     */
    @Override
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    @Override
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        Date curDate = new Date();
        //新增 groupId为空，则是新建立的群组
        if (StringTools.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            //查询当前用户已经创建了多少个群组
            Integer count = this.groupInfoMapper.selectCount(groupInfoQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupCount()) {
                throw new BusinessException("每个用户最多只能创建" + sysSettingDto.getMaxGroupCount() + "个群");
            }

            if (null == avatarFile) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            groupInfo.setCreateTime(curDate);
            groupInfo.setGroupId(StringTools.createGroupId());
            this.groupInfoMapper.insert(groupInfo);

            //将群组添加进联系人列表
            UserContact userContact = new UserContact();
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContcatTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            this.userContactMapper.insert(userContact);

            //TODO 创建会话
            //TODO 发送消息

        } else {
            GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            //如果发起请求的用户不是该群组是所有者（不是群主），则无法修改，拦截请求
            if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            this.groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());

            //TODO 更新相关表的冗余信息
            //TODO 修改群昵称发送WebSocket消息

        }

        if (null == avatarFile) {
            return;
        }
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + Constants.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));

    }
}