package edu.xzit.core.dao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.ExamListenClassPlan;

import java.util.List;


/**
 * 听课计划Mapper接口
 *
 * @author chenlei
 * @date 2025-03-09
 */
public interface ExamListenClassPlanMapper extends BaseMapper<ExamListenClassPlan> {

    /**
     * 删除听课计划
     *
     * @param id 听课计划主键
     * @return 结果
     */
    int deleteExamListenClassPlanById(Long id);

}
