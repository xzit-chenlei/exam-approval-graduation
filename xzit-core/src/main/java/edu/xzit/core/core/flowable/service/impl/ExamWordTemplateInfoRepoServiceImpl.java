package edu.xzit.core.core.flowable.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.common.utils.DateUtils;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;
import edu.xzit.core.core.flowable.mapper.ExamWordTemplateInfoMapper;
import edu.xzit.core.core.flowable.service.IExamWordTemplateInfoRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * word模板信息Service业务层处理
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Service
public class ExamWordTemplateInfoRepoServiceImpl extends ServiceImpl<ExamWordTemplateInfoMapper, ExamWordTemplateInfo> implements IExamWordTemplateInfoRepoService {
    @Autowired
    private ExamWordTemplateInfoMapper examWordTemplateInfoMapper;

    /**
     * 查询word模板信息
     *
     * @param id word模板信息主键
     * @return word模板信息
     */
    @Override
    public ExamWordTemplateInfo selectExamWordTemplateInfoById(Long id) {
        return examWordTemplateInfoMapper.selectExamWordTemplateInfoById(id);
    }

    /**
     * 查询word模板信息列表
     *
     * @param examWordTemplateInfo word模板信息
     * @return word模板信息
     */
    @Override
    public List<ExamWordTemplateInfo> selectExamWordTemplateInfoList(ExamWordTemplateInfo examWordTemplateInfo) {
        LambdaQueryWrapper<ExamWordTemplateInfo> queryWrapper = new LambdaQueryWrapper<>(examWordTemplateInfo);
        return list(queryWrapper);
    }

    /**
     * 新增word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    @Override
    public int insertExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo) {
        return save(examWordTemplateInfo) ? 1 : 0;
    }

    /**
     * 修改word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    @Override
    public int updateExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo) {
        return updateById(examWordTemplateInfo) ? 1 : 0;
    }

    /**
     * 批量删除word模板信息
     *
     * @param ids 需要删除的word模板信息主键
     * @return 结果
     */
    @Override
    public int deleteExamWordTemplateInfoByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<ExamWordTemplateInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamWordTemplateInfo::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除word模板信息信息
     *
     * @param id word模板信息主键
     * @return 结果
     */
    @Override
    public int deleteExamWordTemplateInfoById(Long id) {
        return examWordTemplateInfoMapper.deleteExamWordTemplateInfoById(id);
    }
}
