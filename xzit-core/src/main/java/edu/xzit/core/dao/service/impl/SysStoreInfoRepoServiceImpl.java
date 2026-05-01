package edu.xzit.core.dao.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.exception.ServiceException;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.mapper.SysStoreInfoMapper;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件存储Service业务层处理
 *
 * @author chenlei
 * @date 2024-10-03
 */
@Service
public class SysStoreInfoRepoServiceImpl extends ServiceImpl<SysStoreInfoMapper, SysStoreInfo> implements ISysStoreInfoRepoService {
    @Autowired
    private SysStoreInfoMapper sysStoreInfoMapper;

    /**
     * 查询文件存储
     *
     * @param id 文件存储主键
     * @return 文件存储
     */
    @Override
    public SysStoreInfo selectSysStoreInfoById(Long id) {
        return sysStoreInfoMapper.selectSysStoreInfoById(id);
    }

    /**
     * 查询文件存储列表
     *
     * @param sysStoreInfo 文件存储
     * @return 文件存储
     */
    @Override
    public List<SysStoreInfo> selectSysStoreInfoList(SysStoreInfo sysStoreInfo) {
        return sysStoreInfoMapper.selectSysStoreInfoList(sysStoreInfo);
    }

    @Override
    public List<SysStoreInfo> getBySceneNameAndObjId(String sceneName, Long objId) {
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getSceneName, sceneName)
                .eq(SysStoreInfo::getObjId, objId);
                return list(queryWrapper);
    }

    /**
     * 新增文件存储
     *
     * @param sysStoreInfo 文件存储
     * @return 结果
     */
    @Override
    public int insertSysStoreInfo(SysStoreInfo sysStoreInfo) {
        return save(sysStoreInfo) ? 1 : 0;
    }

    /**
     * 修改文件存储
     *
     * @param sysStoreInfo 文件存储
     * @return 结果
     */
    @Override
    public int updateSysStoreInfo(SysStoreInfo sysStoreInfo) {
        return updateById(sysStoreInfo) ? 1 : 0;
    }

    /**
     * 批量删除文件存储
     *
     * @param ids 需要删除的文件存储主键
     * @return 结果
     */
    @Override
    public int deleteSysStoreInfoByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysStoreInfo::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除文件存储信息
     *
     * @param id 文件存储主键
     * @return 结果
     */
    @Override
    public int deleteSysStoreInfoById(Long id) {
        return sysStoreInfoMapper.deleteSysStoreInfoById(id);
    }

    @Override
    public String listBySceneNameAndObjId(String sceneName, Long objId) {
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getSceneName, sceneName);
        queryWrapper.eq(SysStoreInfo::getObjId, objId);
        return list(queryWrapper).stream().map(SysStoreInfo::getUrl).collect(Collectors.joining(","));
    }

    @Override
    public void flushUrl(String sceneName, Long objId, String url) {

        if (StrUtil.isBlank(url) || url.split(",").length <=0){
            return;
        }

        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getSceneName, sceneName);
        queryWrapper.eq(SysStoreInfo::getObjId, objId);
        List<SysStoreInfo> sysStoreInfos = list(queryWrapper);

        Set<String> requestUrls = new HashSet<>(Arrays.asList(url.split(",")));
        Set<String> existUrls = sysStoreInfos.stream().map(SysStoreInfo::getUrl).collect(Collectors.toSet());

        // 请求的url和已经存在的url取差集，作为新增的url
        List<String> addUrls = requestUrls.stream().filter(item -> !existUrls.contains(item)).collect(Collectors.toList());
        List<SysStoreInfo> addSysStoreInfos = addUrls.stream().map(item -> {
            SysStoreInfo sysStoreInfo = new SysStoreInfo();
            sysStoreInfo.setUrl(item);
            sysStoreInfo.setSceneName(sceneName);
            sysStoreInfo.setObjId(objId);
            return sysStoreInfo;
        }).collect(Collectors.toList());
        saveOrUpdateBatch(addSysStoreInfos);

        // 已存在的url和请求的url取差集，作为删除的url
        List<SysStoreInfo> deleteUrls = sysStoreInfos.stream().filter(item -> !requestUrls.contains(item.getUrl())).collect(Collectors.toList());
        removeByIds(deleteUrls.stream().map(SysStoreInfo::getId).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void bindObjId(Long objId, String url) {
        LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysStoreInfo::getUrl, url);
        List<SysStoreInfo> sysStoreInfos = list(queryWrapper).stream().filter(item-> Objects.isNull(item.getObjId())).collect(Collectors.toList());

        if (CollUtil.isEmpty(sysStoreInfos)){
            throw new ServiceException("文件已经绑定");
        }

        SysStoreInfo sysStoreInfo = sysStoreInfos.get(0);
        sysStoreInfo.setObjId(objId);
        this.saveOrUpdate(sysStoreInfo);

    }
}
