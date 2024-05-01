package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.entity.vo.UserInfoVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.redis.RedisComponent;
import com.weTalk.redis.RedisUtils;
import com.weTalk.service.UserInfoService;
import com.weTalk.utils.CopyTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 生成验证码
     *
     * @return
     */
    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        /**
         * checkCodeKey的作用是标记区分每一个用户的验证码
         * 和以往的项目设计不同，本系统生成的验证码是存储在Redis里，若所有验证码存储的KEY值都相同即都是REDIS_KEY_CHECK_CODE时，
         * 当其他用户在客户端也发起/checkCode请求的话，前一个用户的验证码将会被覆盖重置，
         * 所以在每个验证码存储的KEY值后面都加上一个UUID(通用唯一识别码)来区分所有验证码
         * --------------------------------------------------------------------------------------------------
         * 其它项目一般都是存储在Session里，每个客户端都有自己的Session所以不会发生以上问题
         */
        String checkCodeKey = UUID.randomUUID().toString();

        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN * 10);

        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);

        return getSuccessResponseVO(result);
    }

    /**
     * 注册
     * @param checkCodeKey
     * @param email
     * @param password
     * @param nickName
     * @param checkCode
     * @return
     */
    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            userInfoService.register(email, password, nickName);
            return getSuccessResponseVO(null);

        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    /**
     * 登录
     * @param checkCodeKey
     * @param email
     * @param password
     * @param checkCode
     * @return
     */
    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
                throw new BusinessException("图片验证码错误");
            }
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email, password);

            UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
            UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
            userInfoVO.setToken(tokenUserInfoDto.getToken());
            userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());

            return getSuccessResponseVO(userInfoVO);

        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @RequestMapping("/getSysSetting")
    @GlobalInterceptor
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }

}
