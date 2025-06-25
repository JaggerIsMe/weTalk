package com.weTalk.redis;

import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.utils.StringTools;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     * 用户登录时就会获取心跳
     *
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEARTBEAT + userId);
    }

    /**
     * 保存用户心跳
     *
     * @param userId
     */
    public void saveUserHeartBeat(String userId) {
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEARTBEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

    /**
     * 用户离线 删除用户心跳
     *
     * @param userId
     */
    public void removeUserHeartBeat(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEARTBEAT + userId);
    }

    /**
     * 一个是由token存储对应的tokenUserInfoDto信息
     * <p>
     * 一个是由userId存储对应的token值。将来可以通过userId获取token，再由token获取对应的tokenUserInfoDto信息
     * 因为将来有一些场景可能无法直接获取token，只能获取userId
     *
     * @param tokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }

    /**
     * 根据token获取对应的tokenUserInfoDto信息
     *
     * @param token
     * @return
     */
    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

    /**
     * 根据userId获取对应的tokenUserInfoDto信息
     *
     * @param userId
     * @return
     */
    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        return getTokenUserInfoDto(token);
    }

    /**
     * 根据userId删除token
     * 用户强制下线后执行
     *
     * @param userId
     */
    public void cleanUserTokenByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        if (StringTools.isEmpty(token)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN + token);
    }

    /**
     * 获取系统设置
     *
     * @return
     */
    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = null == sysSettingDto ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     *
     * @param sysSettingDto
     */
    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 清空用户联系人列表缓存
     *
     * @param userId
     */
    public void cleanUserContact(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    /**
     * 批量添加用户联系人列表缓存
     *
     * @param userId
     * @param contactIdList
     */
    public void addUserContactBatch(String userId, List<String> contactIdList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_USER_CONTACT + userId, contactIdList, Constants.REDIS_KEY_TOKEN_EXPIRES);
    }

    /**
     * 添加用户联系人列表缓存
     *
     * @param userId
     * @param contactId
     */
    public void addUserContact(String userId, String contactId) {
        List<String> contactIdList = getUserContactList(userId);
        if (contactIdList.contains(contactId)) {
            return;
        }
        redisUtils.lpush(Constants.REDIS_KEY_USER_CONTACT + userId, contactId, Constants.REDIS_KEY_TOKEN_EXPIRES);
    }

    /**
     * 获取用户联系人列表
     *
     * @param userId
     * @return
     */
    public List<String> getUserContactList(String userId) {
        return (List<String>) redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    /**
     * 移除用户联系人缓存
     *
     * @param userId
     * @param contactId
     */
    public void removeUserContact(String userId, String contactId) {
        redisUtils.remove(Constants.REDIS_KEY_USER_CONTACT + userId, contactId);
    }

}
