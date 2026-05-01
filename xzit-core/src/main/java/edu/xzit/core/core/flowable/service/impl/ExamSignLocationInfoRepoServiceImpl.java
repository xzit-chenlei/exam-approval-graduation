package edu.xzit.core.core.flowable.service.impl;

import java.util.Arrays;
import java.util.List;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.xzit.core.core.flowable.domain.ExamSignLocationInfo;
import edu.xzit.core.core.flowable.service.IExamSignLocationInfoRepoService;
import edu.xzit.core.core.flowable.mapper.ExamSignLocationInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 签名位置信息Service业务层处理
 *
 * @author chenlei
 * @date 2025-03-05
 */
@Service
public class ExamSignLocationInfoRepoServiceImpl extends ServiceImpl<ExamSignLocationInfoMapper, ExamSignLocationInfo> implements IExamSignLocationInfoRepoService {
    @Autowired
    private ExamSignLocationInfoMapper examSignLocationInfoMapper;

    /**
     * 查询签名位置信息
     *
     * @param id 签名位置信息主键
     * @return 签名位置信息
     */
    @Override
    public ExamSignLocationInfo selectExamSignLocationInfoById(Long id) {
        return getById(id);
    }

    /**
     * 查询签名位置信息列表
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 签名位置信息
     */
    @Override
    public List<ExamSignLocationInfo> selectExamSignLocationInfoList(ExamSignLocationInfo examSignLocationInfo) {
        return list(new LambdaQueryWrapper<>(examSignLocationInfo));
    }

    /**
     * 新增签名位置信息
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 结果
     */
    @Override
    public int insertExamSignLocationInfo(ExamSignLocationInfo examSignLocationInfo) {
        return saveOrUpdate(examSignLocationInfo) ? 1 : 0;
    }

    /**
     * 修改签名位置信息
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 结果
     */
    @Override
    public int updateExamSignLocationInfo(ExamSignLocationInfo examSignLocationInfo) {
        return updateById(examSignLocationInfo) ? 1 : 0;
    }

    /**
     * 批量删除签名位置信息
     *
     * @param ids 需要删除的签名位置信息主键
     * @return 结果
     */
    @Override
    public int deleteExamSignLocationInfoByIds(Long[] ids) {

        if (ArrayUtil.isEmpty(ids)) {
            return 0;
        }

        LambdaQueryWrapper<ExamSignLocationInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExamSignLocationInfo::getId, Arrays.asList(ids));
        return remove(queryWrapper) ? 1 : 0;

    }

    /**
     * 删除签名位置信息信息
     *
     * @param id 签名位置信息主键
     * @return 结果
     */
    @Override
    public int deleteExamSignLocationInfoById(Long id) {
        return examSignLocationInfoMapper.deleteExamSignLocationInfoById(id);
    }
}
