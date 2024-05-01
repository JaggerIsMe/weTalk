package com.weTalk.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.weTalk.entity.enums.BeautyAccountStatusEnum;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.query.UserInfoQuery;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.UserInfoMapper;
import com.weTalk.service.UserInfoService;
import org.springframework.stereotype.Service;

import com.weTalk.entity.enums.PageSize;
import com.weTalk.entity.query.UserInfoBeautyQuery;
import com.weTalk.entity.po.UserInfoBeauty;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.mappers.UserInfoBeautyMapper;
import com.weTalk.service.UserInfoBeautyService;
import com.weTalk.utils.StringTools;


/**
 * 靓号信息表 业务接口实现
 */
@Service("userInfoBeautyService")
public class UserInfoBeautyServiceImpl implements UserInfoBeautyService {

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery param) {
        return this.userInfoBeautyMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoBeautyQuery param) {
        return this.userInfoBeautyMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfoBeauty> list = this.findListByParam(param);
        PaginationResultVO<UserInfoBeauty> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfoBeauty bean) {
        return this.userInfoBeautyMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfoBeauty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoBeautyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfoBeauty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoBeautyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserInfoBeauty bean, UserInfoBeautyQuery param) {
        StringTools.checkParam(param);
        return this.userInfoBeautyMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserInfoBeautyQuery param) {
        StringTools.checkParam(param);
        return this.userInfoBeautyMapper.deleteByParam(param);
    }

    /**
     * 根据Id获取对象
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyById(Integer id) {
        return this.userInfoBeautyMapper.selectById(id);
    }

    /**
     * 根据Id修改
     */
    @Override
    public Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id) {
        return this.userInfoBeautyMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除
     */
    @Override
    public Integer deleteUserInfoBeautyById(Integer id) {
        return this.userInfoBeautyMapper.deleteById(id);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyByUserId(String userId) {
        return this.userInfoBeautyMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) {
        return this.userInfoBeautyMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoBeautyByUserId(String userId) {
        return this.userInfoBeautyMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfoBeauty getUserInfoBeautyByEmail(String email) {
        return this.userInfoBeautyMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) {
        return this.userInfoBeautyMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoBeautyByEmail(String email) {
        return this.userInfoBeautyMapper.deleteByEmail(email);
    }

    /**
     * 新增、修改、保存靓号
     *
     * @param beautyAcc
     */
    @Override
    public void saveAccount(UserInfoBeauty beautyAcc) {
        if (null != beautyAcc.getId()) {
            UserInfoBeauty dbInfo = this.userInfoBeautyMapper.selectById(beautyAcc.getId());
            //判断靓号是否已经使用
            if (BeautyAccountStatusEnum.USED.getStatus().equals(dbInfo.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        UserInfoBeauty dbInfo = this.userInfoBeautyMapper.selectByEmail(beautyAcc.getEmail());
        //新增时判断邮箱是否存在
        if (null == beautyAcc.getId() && dbInfo != null) {
            throw new BusinessException("该靓号邮箱已经存在");
        }

        //修改时判断邮箱是否存在
        if (null != beautyAcc.getId() && dbInfo != null && null != dbInfo.getId() && !beautyAcc.getId().equals(dbInfo.getId())) {
            throw new BusinessException("该靓号邮箱已经存在");
        }

        //判断靓号是否存在
        dbInfo = this.userInfoBeautyMapper.selectByUserId(beautyAcc.getUserId());
        if (null == beautyAcc.getId() && null != dbInfo) {
            throw new BusinessException("该靓号已经存在");
        }
        if (null != beautyAcc.getId() && null != dbInfo && null != dbInfo.getId() && !beautyAcc.getId().equals(dbInfo.getId())) {
            throw new BusinessException("该靓号已经存在");
        }

        //判断邮箱是否已经注册
        UserInfo userInfo = this.userInfoMapper.selectByEmail(beautyAcc.getEmail());
        if (null != userInfo) {
            throw new BusinessException("靓号邮箱已经被注册");
        }
        userInfo = this.userInfoMapper.selectByUserId(beautyAcc.getUserId());
        if (null != userInfo) {
            throw new BusinessException("靓号已经被注册");
        }
        if (null != beautyAcc.getId()) {
            this.userInfoBeautyMapper.updateById(beautyAcc, beautyAcc.getId());
        } else {
            beautyAcc.setStatus(BeautyAccountStatusEnum.UNUSED.getStatus());
            this.userInfoBeautyMapper.insert(beautyAcc);
        }
    }
}