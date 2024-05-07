package com.weTalk.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.weTalk.config.AppConfig;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.po.UserInfoBeauty;
import com.weTalk.entity.query.UserContactQuery;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.mappers.UserInfoBeautyMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.service.ChatSessionUserService;
import com.weTalk.service.UserContactService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.weTalk.entity.query.UserInfoQuery;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.mappers.UserInfoMapper;
import com.weTalk.service.UserInfoService;
import com.weTalk.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息表 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoQuery> userInfoBeautyMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 注册
     *
     * @param email
     * @param password
     * @param nickName
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, String nickName) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已存在");
        }
        String userId;
        UserInfoBeauty beautyAcc = this.userInfoBeautyMapper.selectByEmail(email);

        //是否使用靓号作为ID
        Boolean isUseBeautyAcc = ((null != beautyAcc) && (BeautyAccountStatusEnum.UNUSED.getStatus().equals(beautyAcc.getStatus())));
        if (isUseBeautyAcc) {
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAcc.getUserId();
            //若使用靓号 把靓号标记为已使用
            UserInfoBeauty updateBeauty = new UserInfoBeauty();
            updateBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            this.userInfoBeautyMapper.updateById(updateBeauty, beautyAcc.getId());
        } else {
            userId = StringTools.createUserId();
        }

        Date curDate = new Date();
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        userInfo.setCreateTime(curDate);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setLastOffTime(curDate.getTime());
        userInfo.setJoinType(JoinTypeEnum.NEED_APPLY.getType());
        this.userInfoMapper.insert(userInfo);

        //创建机器人好友
        userContactService.addContact4Robot(userId);
    }

    /**
     * 登录
     *
     * @param email
     * @param password
     * @return
     */
    @Override
    public TokenUserInfoDto login(String email, String password) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }
        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (null != lastHeartBeat) {
            throw new BusinessException("此账号已在别处登录，请退出后重试");
        }

        //查询联系人
        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(userInfo.getUserId());
        contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> contactList = userContactMapper.selectList(contactQuery);
        List<String> contactIdList = contactList.stream().map(item->item.getContactId()).collect(Collectors.toList());

        //登录成功后，将用户联系人列表添加进Redis里 缓存起来
        redisComponent.cleanUserContact(userInfo.getUserId());
        if (!contactIdList.isEmpty()){
            redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);

        //保存登录信息到Redis中
        String token = StringTools.encodeByMd5(tokenUserInfoDto.getUserId() + StringTools.createRandomStr(Constants.LENGTH_20));
        tokenUserInfoDto.setToken(token);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        return tokenUserInfoDto;

    }

    /**
     * 由UserInfo获取Token对象
     *
     * @param userInfo
     * @return
     */
    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());

        String adminEmailStr = appConfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmailStr) && ArrayUtils.contains(adminEmailStr.split(","), userInfo.getEmail())) {
            tokenUserInfoDto.setAdmin(true);
        } else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }

    /**
     * 修改用户信息 并且同步更新会话的名称
     * @param userInfo
     * @param avatarFile
     * @param avatarCover
     * @throws IOException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (null != avatarFile) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userInfo.getUserId() + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        //查询获取更新前的用户信息，保存下来
        UserInfo dbInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());
        //更新用户信息
        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        /**
         * 上面的小细节
         * 先查询后更新的小细节：
         * 先查询后更新，事务开启的时间就比较短
         * 查询时不会开启事务，更新时才会开启事务
         * 如果先更新，开启事务，然后再查询，如果查询耗时较长，可能导致事务超时
         */
        String contactNameUpdate = null;
        //如果更新前的名字和更新后的名字不相等
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        if (null == contactNameUpdate){
            return;
        }

        //更新缓存里token中的昵称
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(contactNameUpdate);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        //更新会话中的昵称信息 更新冗余信息
        chatSessionUserService.updateRedundantInfo(contactNameUpdate, userInfo.getUserId());
    }

    /**
     * 更新用户状态
     *
     * @param status
     * @param userId
     */
    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum userStatusEnum = UserStatusEnum.getByStatus(status);
        if (null == userStatusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(userStatusEnum.getStatus());
        this.userInfoMapper.updateByUserId(userInfo, userId);
    }

    /**
     * 强制用户下线
     * @param userId
     */
    @Override
    public void forceOffLine(String userId) {
        //TODO 强制用户下线
    }
}