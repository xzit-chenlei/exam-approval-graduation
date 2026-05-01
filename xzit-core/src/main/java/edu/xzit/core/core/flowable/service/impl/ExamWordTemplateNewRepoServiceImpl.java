package edu.xzit.core.core.flowable.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateNew;
import edu.xzit.core.core.flowable.mapper.ExamWordTemplateNewMapper;
import edu.xzit.core.core.flowable.service.IExamWordTemplateNewRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 新word模板Service业务层处理
 */
@Service
public class ExamWordTemplateNewRepoServiceImpl extends ServiceImpl<ExamWordTemplateNewMapper, ExamWordTemplateNew> implements IExamWordTemplateNewRepoService {

    @Autowired
    private ExamWordTemplateNewMapper examWordTemplateNewMapper;

    @Override
    public ExamWordTemplateNew selectExamWordTemplateNewById(Long id) {
        return examWordTemplateNewMapper.selectExamWordTemplateNewById(id);
    }

    @Override
    public List<ExamWordTemplateNew> selectExamWordTemplateNewList(ExamWordTemplateNew examWordTemplateNew) {
        return examWordTemplateNewMapper.selectExamWordTemplateNewList(examWordTemplateNew);
    }

    @Override
    public List<ExamWordTemplateNew> getByFlowableKeyAndColumn(String flowableKey, Integer column, Integer row) {

        LambdaQueryWrapper<ExamWordTemplateNew> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExamWordTemplateNew::getFlowableKey, flowableKey)
                .eq(ExamWordTemplateNew::getColumnCount, column)
                .eq(ExamWordTemplateNew::getRowCount, row);
        return list(queryWrapper);
    }

    @Override
    public int insertExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew) {
        return save(examWordTemplateNew) ? 1 : 0;
    }

    @Override
    public int updateExamWordTemplateNew(ExamWordTemplateNew examWordTemplateNew) {
        return updateById(examWordTemplateNew) ? 1 : 0;
    }

    @Override
    public int deleteExamWordTemplateNewByIds(Long[] ids) {
        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }
        LambdaQueryWrapper<ExamWordTemplateNew> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamWordTemplateNew::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;
    }

    @Override
    public int deleteExamWordTemplateNewById(Long id) {
        return examWordTemplateNewMapper.deleteExamWordTemplateNewById(id);
    }
}

