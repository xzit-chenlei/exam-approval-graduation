package edu.xzit.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.flowable.constant.StoreSceneNameConstants;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;
import edu.xzit.core.core.flowable.domain.dto.ExamWordTemplateNewDto;
import edu.xzit.core.core.flowable.service.IExamWordTemplateNewRepoService;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExamWordTemplateNewBizService {

    @Autowired
    private IExamWordTemplateNewRepoService examWordTemplateNewRepoService;

    @Autowired
    private ISysStoreInfoRepoService sysStoreInfoRepoService;

    public AjaxResult addExamWordTemplateNew(ExamWordTemplateNewDto dto) {

        if (dto == null) {
            return AjaxResult.error("参数不能为空");
        }

        try {
            if (StrUtil.isBlank(dto.getOssKey()) || StrUtil.isBlank(dto.getFileUrl())) {
                return AjaxResult.error("OSS Key 和 文件 URL 不能为空");
            }
            if (this.existsSameLayout(dto)) {
                return AjaxResult.error("同一流程、相同行列的模板已存在");
            }

            ExamWordTemplateNew entity = BeanUtil.copyProperties(dto, ExamWordTemplateNew.class);
            examWordTemplateNewRepoService.insertExamWordTemplateNew(entity);

            SysStoreInfo sysStoreInfo = new SysStoreInfo();
            sysStoreInfo.setObjId(entity.getId());
            sysStoreInfo.setSceneName(StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW);
            sysStoreInfo.setOssKey(dto.getOssKey());
            sysStoreInfo.setUrl(dto.getFileUrl());
            sysStoreInfoRepoService.save(sysStoreInfo);

            return AjaxResult.success("保存成功");
        } catch (Exception e) {
            log.error("保存word模板主表失败", e);
            return AjaxResult.error("保存失败：" + e.getMessage());
        }
    }

    public AjaxResult updateExamWordTemplateNew(ExamWordTemplateNewDto dto) {
        if (dto == null) {
            return AjaxResult.error("参数不能为空");
        }

        if (dto.getId() == null) {
            return AjaxResult.error("ID不能为空");
        }

        try {
            ExamWordTemplateNew entity = BeanUtil.copyProperties(dto, ExamWordTemplateNew.class);
            if (this.existsSameLayout(dto)) {
                return AjaxResult.error("同一流程、相同行列的模板已存在");
            }
            examWordTemplateNewRepoService.updateExamWordTemplateNew(entity);

            LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysStoreInfo::getObjId, dto.getId())
                    .eq(SysStoreInfo::getSceneName, StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW);
            List<SysStoreInfo> list = sysStoreInfoRepoService.list(queryWrapper);
            SysStoreInfo sysStoreInfo = list.stream().findFirst().orElse(null);

            if (StrUtil.isNotBlank(dto.getOssKey()) && StrUtil.isNotBlank(dto.getFileUrl())) {
                SysStoreInfo storeInfo = sysStoreInfo != null ? sysStoreInfo : new SysStoreInfo();
                storeInfo.setObjId(entity.getId());
                storeInfo.setSceneName(StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW);
                storeInfo.setOssKey(dto.getOssKey());
                storeInfo.setUrl(dto.getFileUrl());

                if (sysStoreInfo != null) {
                    sysStoreInfoRepoService.updateById(storeInfo);
                } else {
                    sysStoreInfoRepoService.save(storeInfo);
                }
            }

            return AjaxResult.success("保存成功");
        } catch (Exception e) {
            log.error("更新word模板主表失败", e);
            return AjaxResult.error("保存失败：" + e.getMessage());
        }
    }

    public AjaxResult getExamWordTemplateNewById(Long id) {
        if (id == null) {
            return AjaxResult.error("参数不能为空");
        }

        try {
            ExamWordTemplateNew entity = examWordTemplateNewRepoService.getById(id);
            if (Objects.isNull(entity)) {
                return AjaxResult.error("word模板主表不存在");
            }

            ExamWordTemplateNewDto dto = BeanUtil.copyProperties(entity, ExamWordTemplateNewDto.class);

            LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysStoreInfo::getObjId, id)
                    .eq(SysStoreInfo::getSceneName, StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE_NEW);
            List<SysStoreInfo> list = sysStoreInfoRepoService.list(queryWrapper);
            SysStoreInfo sysStoreInfo = list.stream().findFirst().orElse(null);
            if (sysStoreInfo != null) {
                dto.setFileUrl(sysStoreInfo.getUrl());
            }
            return AjaxResult.success(dto);
        } catch (Exception e) {
            log.error("获取word模板主表失败", e);
            return AjaxResult.error("获取失败");
        }
    }

    /**
     * 校验同一流程下相同行列是否已存在模板
     */
    private boolean existsSameLayout(ExamWordTemplateNewDto dto) {
        if (StrUtil.isBlank(dto.getFlowableKey()) || dto.getRowCount() == null || dto.getColumnCount() == null) {
            return false;
        }
        LambdaQueryWrapper<ExamWordTemplateNew> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamWordTemplateNew::getFlowableKey, dto.getFlowableKey())
                .eq(ExamWordTemplateNew::getRowCount, dto.getRowCount())
                .eq(ExamWordTemplateNew::getColumnCount, dto.getColumnCount());
        if (dto.getId() != null) {
            wrapper.ne(ExamWordTemplateNew::getId, dto.getId());
        }
        return examWordTemplateNewRepoService.count(wrapper) > 0;
    }
}










