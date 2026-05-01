package edu.xzit.core.core.flowable.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.core.flowable.domain.ExamSignLocationInfo;

import java.util.List;


/**
 * 签名位置信息Mapper接口
 *
 * @author chenlei
 * @date 2025-03-05
 */
public interface ExamSignLocationInfoMapper extends BaseMapper<ExamSignLocationInfo> {
    /**
     * 查询签名位置信息
     *
     * @param id 签名位置信息主键
     * @return 签名位置信息
     */
        ExamSignLocationInfo selectExamSignLocationInfoById(Long id);

    /**
     * 查询签名位置信息列表
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 签名位置信息集合
     */
    List<ExamSignLocationInfo> selectExamSignLocationInfoList(ExamSignLocationInfo examSignLocationInfo);

    /**
     * 新增签名位置信息
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 结果
     */
    int insertExamSignLocationInfo(ExamSignLocationInfo examSignLocationInfo);

    /**
     * 修改签名位置信息
     *
     * @param examSignLocationInfo 签名位置信息
     * @return 结果
     */
    int updateExamSignLocationInfo(ExamSignLocationInfo examSignLocationInfo);

    /**
     * 删除签名位置信息
     *
     * @param id 签名位置信息主键
     * @return 结果
     */
    int deleteExamSignLocationInfoById(Long id);

    /**
     * 批量删除签名位置信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteExamSignLocationInfoByIds(Long[] ids);
}
