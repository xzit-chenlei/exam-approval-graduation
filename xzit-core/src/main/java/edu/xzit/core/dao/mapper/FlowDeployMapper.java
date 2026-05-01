package edu.xzit.core.dao.mapper;


import edu.xzit.core.dao.domain.FlowProcDefDto;

import java.util.List;

/**
 * 流程定义查询
 *
 * @author Tony
 * @email
 * @date 2022/1/29 5:44 下午
 **/
public interface FlowDeployMapper {

    /**
     * 流程定义列表
     * @param name 模糊查询
     * @return
     */
    List<FlowProcDefDto> selectDeployList(String name);

    /**
     * 流程定义列表
     * @param key 精确查询
     * @return
     */
    List<FlowProcDefDto> selectDeployListNonFuzzy(String key);

    String selectDeployIdByDeployName(String name);

    String selectDeployIdByDeployKey(String key);
}
