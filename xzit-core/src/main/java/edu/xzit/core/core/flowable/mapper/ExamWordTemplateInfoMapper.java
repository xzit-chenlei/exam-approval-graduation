package edu.xzit.core.core.flowable.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.core.flowable.domain.ExamWordTemplateInfo;

import java.util.List;


/**
 * word模板信息Mapper接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface ExamWordTemplateInfoMapper extends BaseMapper<ExamWordTemplateInfo> {
    /**
     * 查询word模板信息
     *
     * @param id word模板信息主键
     * @return word模板信息
     */
        ExamWordTemplateInfo selectExamWordTemplateInfoById(Long id);

    /**
     * 查询word模板信息列表
     *
     * @param examWordTemplateInfo word模板信息
     * @return word模板信息集合
     */
    List<ExamWordTemplateInfo> selectExamWordTemplateInfoList(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 新增word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    int insertExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 修改word模板信息
     *
     * @param examWordTemplateInfo word模板信息
     * @return 结果
     */
    int updateExamWordTemplateInfo(ExamWordTemplateInfo examWordTemplateInfo);

    /**
     * 删除word模板信息
     *
     * @param id word模板信息主键
     * @return 结果
     */
    int deleteExamWordTemplateInfoById(Long id);

    /**
     * 批量删除word模板信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteExamWordTemplateInfoByIds(Long[] ids);
}
