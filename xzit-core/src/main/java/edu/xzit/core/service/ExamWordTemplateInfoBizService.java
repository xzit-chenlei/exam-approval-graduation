package edu.xzit.core.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.xzit.core.core.common.core.domain.AjaxResult;
import edu.xzit.core.core.flowable.constant.StoreSceneNameConstants;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;
import edu.xzit.core.core.flowable.domain.dto.ExamWordTemplateInfoDto;
import edu.xzit.core.core.flowable.service.IExamWordTemplateInfoRepoService;
import edu.xzit.core.dao.domain.SysStoreInfo;
import edu.xzit.core.dao.service.ISysStoreInfoRepoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExamWordTemplateInfoBizService {

    @Autowired
    IExamWordTemplateInfoRepoService examWordTemplateInfoRepoService;

    @Autowired
    ISysStoreInfoRepoService sysStoreInfoRepoService;

    public AjaxResult addExamWordTemplateInfo(ExamWordTemplateInfoDto examWordTemplateInfoDto) {

        if (examWordTemplateInfoDto == null) {
            return AjaxResult.error("参数不能为空");
        }

        try {
            // 检查必要的字段是否为空
            if (examWordTemplateInfoDto.getOssKey() == null || examWordTemplateInfoDto.getFileUrl() == null) {
                return AjaxResult.error("OSS Key 和 文件 URL 不能为空");
            }

            ExamWordTemplateInfo examWordTemplateInfo = BeanUtil.copyProperties(examWordTemplateInfoDto, ExamWordTemplateInfo.class);
            examWordTemplateInfoRepoService.insertExamWordTemplateInfo(examWordTemplateInfo);

            SysStoreInfo sysStoreInfo = new SysStoreInfo();
            sysStoreInfo.setObjId(examWordTemplateInfo.getId());
            sysStoreInfo.setSceneName(StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE);
            sysStoreInfo.setOssKey(examWordTemplateInfoDto.getOssKey());
            sysStoreInfo.setUrl(examWordTemplateInfoDto.getFileUrl());
            sysStoreInfoRepoService.save(sysStoreInfo);

            return AjaxResult.success("保存成功");
        } catch (Exception e) {
            // 记录异常日志
            log.error("保存考试模板信息失败", e);
            return AjaxResult.error("保存失败：" + e.getMessage());
        }

    }

    public AjaxResult updateExamWordTemplateInfo(ExamWordTemplateInfoDto examWordTemplateInfoDto) {

        if (examWordTemplateInfoDto == null) {
            return AjaxResult.error("参数不能为空");
        }

        try {
            // 检查必要的字段是否为空
            if (examWordTemplateInfoDto.getId() == null) {
                return AjaxResult.error("ID不能为空");
            }

            ExamWordTemplateInfo examWordTemplateInfo = BeanUtil.copyProperties(examWordTemplateInfoDto, ExamWordTemplateInfo.class);
            examWordTemplateInfoRepoService.updateExamWordTemplateInfo(examWordTemplateInfo);

            LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysStoreInfo::getObjId, examWordTemplateInfoDto.getId())
                    .eq(SysStoreInfo::getSceneName, StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE);
            List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.list(queryWrapper);
            SysStoreInfo sysStoreInfo = sysStoreInfos.stream().findFirst().orElse(null);

            if ( StrUtil.isNotBlank(examWordTemplateInfoDto.getOssKey()) && StrUtil.isNotBlank(examWordTemplateInfoDto.getFileUrl())) {

                SysStoreInfo storeInfo = sysStoreInfo != null ? sysStoreInfo : new SysStoreInfo();
                storeInfo.setObjId(examWordTemplateInfo.getId());
                storeInfo.setSceneName(StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE);
                storeInfo.setOssKey(examWordTemplateInfoDto.getOssKey());
                storeInfo.setUrl(examWordTemplateInfoDto.getFileUrl());

                if (sysStoreInfo != null) {
                    sysStoreInfoRepoService.updateById(storeInfo);
                } else {
                    sysStoreInfoRepoService.save(storeInfo);
                }
            }


            return AjaxResult.success("保存成功");
        } catch (Exception e) {
            // 记录异常日志
            log.error("保存考试模板信息失败", e);
            return AjaxResult.error("保存失败：" + e.getMessage());
        }
    }

    public AjaxResult getExamWordTemplateInfoById(Long id) {

        if (id == null) {
            return AjaxResult.error("参数不能为空");
        }

        try {
            ExamWordTemplateInfo examWordTemplateInfo = examWordTemplateInfoRepoService.getById(id);
            if (Objects.isNull(examWordTemplateInfo)) {
                return AjaxResult.error("考试模板信息不存在");
            }
            ExamWordTemplateInfoDto examWordTemplateInfoDto = BeanUtil.copyProperties(examWordTemplateInfo, ExamWordTemplateInfoDto.class);
            LambdaQueryWrapper<SysStoreInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysStoreInfo::getObjId, id)
                    .eq(SysStoreInfo::getSceneName, StoreSceneNameConstants.FLOWABLE_WORD_TEMPLATE);
            List<SysStoreInfo> sysStoreInfos = sysStoreInfoRepoService.list(queryWrapper);
            SysStoreInfo sysStoreInfo = sysStoreInfos.stream().findFirst().orElse(null);
            if(!Objects.isNull(sysStoreInfo)){
                examWordTemplateInfoDto.setFileUrl(sysStoreInfo.getUrl());
            }
            return AjaxResult.success(examWordTemplateInfoDto);
        }catch (Exception e){
            log.error("获取考试模板信息失败", e);
            return AjaxResult.error("获取考试模板信息失败");
        }

    }
}
