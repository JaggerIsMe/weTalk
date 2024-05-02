package com.weTalk.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.weTalk.config.AppConfig;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.AppUpdateFileTypeEnum;
import com.weTalk.entity.enums.AppUpdateStatusEnum;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weTalk.entity.enums.PageSize;
import com.weTalk.entity.query.AppUpdateQuery;
import com.weTalk.entity.po.AppUpdate;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.mappers.AppUpdateMapper;
import com.weTalk.service.AppUpdateService;
import com.weTalk.utils.StringTools;
import org.springframework.web.multipart.MultipartFile;


/**
 * APP发布 业务接口实现
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

    @Resource
    private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<AppUpdate> findListByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<AppUpdate> list = this.findListByParam(param);
        PaginationResultVO<AppUpdate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(AppUpdate bean) {
        return this.appUpdateMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(AppUpdate bean, AppUpdateQuery param) {
        StringTools.checkParam(param);
        return this.appUpdateMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(AppUpdateQuery param) {
        StringTools.checkParam(param);
        return this.appUpdateMapper.deleteByParam(param);
    }

    /**
     * 根据Id获取对象
     */
    @Override
    public AppUpdate getAppUpdateById(Integer id) {
        return this.appUpdateMapper.selectById(id);
    }

    /**
     * 根据Id修改
     */
    @Override
    public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
        return this.appUpdateMapper.updateById(bean, id);
    }

    /**
     * 根据Id删除版本
     */
    @Override
    public Integer deleteAppUpdateById(Integer id) {
        AppUpdate dbInfo = this.getAppUpdateById(id);
        //已经发布的版本不能删除
        if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return this.appUpdateMapper.deleteById(id);
    }

    /**
     * 根据Version获取对象
     */
    @Override
    public AppUpdate getAppUpdateByVersion(String version) {
        return this.appUpdateMapper.selectByVersion(version);
    }

    /**
     * 根据Version修改
     */
    @Override
    public Integer updateAppUpdateByVersion(AppUpdate bean, String version) {
        return this.appUpdateMapper.updateByVersion(bean, version);
    }

    /**
     * 根据Version删除
     */
    @Override
    public Integer deleteAppUpdateByVersion(String version) {
        return this.appUpdateMapper.deleteByVersion(version);
    }

    /**
     * 保存新版本
     *
     * @param appUpdate
     * @param file
     */
    @Override
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (null == fileTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //已经发布的版本无法修改，只能取消发布后再修改
        if (appUpdate.getId() != null) {
            AppUpdate dbInfo = this.getAppUpdateById(appUpdate.getId());
            if (!AppUpdateStatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        AppUpdateQuery updateQuery = new AppUpdateQuery();
        updateQuery.setOrderBy("id desc");
        //获取最新的历史版本
        updateQuery.setSimplePage(new SimplePage(0, 1));
        List<AppUpdate> appUpdateList = appUpdateMapper.selectList(updateQuery);
        if (!appUpdateList.isEmpty()) {
            //最新的历史版本
            AppUpdate latest = appUpdateList.get(0);
            //将版本号String转换成Integer，以进行大小对比
            Long historyVer = Long.parseLong(latest.getVersion().replace(".", ""));
            Long currentVer = Long.parseLong(appUpdate.getVersion().replace(".", ""));

            //新增版本时必须大于历史版本
            if (null == appUpdate.getId() && currentVer <= historyVer) {
                throw new BusinessException("当前版本必须大于历史版本");
            }

            /**
             * 修改已有版本时 版本号只能修改为小于最新的历史版本，不能修改为大于最新的历史版本
             * eg. 当修改的版本不是最新的版本时，不能将其修改为大于最新的历史版本
             *     当系统目前最新版本为1.0.8，若我们要修改已有的1.0.5的版本时，无法将其修改为1.0.9，只能修改为小于1.0.8
             *     但我们可以将1.0.8修改为1.0.9
             *
             * 这就保证了我们通过id降序排列("id desc")后获得的版本号绝对就是当前最新的历史版本
             * 避免了后续的一些不规范修改导致我们无法通过id降序排列获得最新的历史版本
             */
            if (null != appUpdate.getId() && currentVer >= historyVer && !appUpdate.getId().equals(latest.getId())) {
                throw new BusinessException("修改当前版本不能大于最新历史版本");
            }

            AppUpdate repeatVer = appUpdateMapper.selectByVersion(appUpdate.getVersion());
            //新增或修改版本时不能出现版本重复
            if (null != appUpdate.getId() && null != repeatVer && !repeatVer.getId().equals(appUpdate.getId())) {
                throw new BusinessException("当前版本已存在");
            }
        }

        if (null == appUpdate.getId()) {
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        } else {
            appUpdateMapper.updateById(appUpdate, appUpdate.getId());
        }

        //上传新版本文件
        if (null != file) {
            File folder = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file.transferTo(new File(folder.getAbsolutePath() + "/" + appUpdate.getId() + Constants.APP_EXE_SUFFIX));
        }
    }

    /**
     * 发布新版本
     *
     * @param id
     * @param status
     * @param grayscaleUid
     */
    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(status);
        if (null == statusEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //灰度发布 但没有灰度Uid  参数错误
        if (AppUpdateStatusEnum.GRAYSCALE == statusEnum && StringTools.isEmpty(grayscaleUid)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //不是灰度发布 但有灰度Uid参数 把灰度Uid参数置空，防止恶意渗透
        if (AppUpdateStatusEnum.GRAYSCALE != statusEnum) {
            grayscaleUid = "";
        }
        AppUpdate update = new AppUpdate();
        update.setStatus(status);
        update.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.updateById(update, id);
    }

    /**
     * 获取最新版本
     * @param appVersion
     * @param uid
     * @return
     */
    @Override
    public AppUpdate getLatestUpdate(String appVersion, String uid) {
        return appUpdateMapper.selectLatestUpdate(appVersion, uid);
    }
}