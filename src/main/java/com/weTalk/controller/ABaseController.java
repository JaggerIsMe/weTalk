package com.weTalk.controller;

import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


public class ABaseController {

    @Resource
    private RedisUtils redisUtils;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    /**
     * 在HttpServletRequest里获取请求头里的token
     * 再从Redis里取出用户信息
     * @param request
     * @return
     */
    protected TokenUserInfoDto getTokenUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

}
