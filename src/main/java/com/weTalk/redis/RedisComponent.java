package com.weTalk.redis;

import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
     * 获取系统设置
     * @return
     */
    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = null == sysSettingDto ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    /**
     * 保存系统设置
     * @param sysSettingDto
     */
    public void saveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

}
