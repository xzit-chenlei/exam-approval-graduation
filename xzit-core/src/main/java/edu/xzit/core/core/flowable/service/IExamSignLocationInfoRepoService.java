package edu.xzit.core.core.flowable.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.xzit.core.core.flowable.domain.ExamSignLocationInfo;


/**
 * 签名位置信息Service接口
 *
 * @author chenlei
 * @date 2025-03-05
 */
public interface IExamSignLocationInfoRepoService extends IService<ExamSignLocationInfo> {
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
     * 批量删除签名位置信息
     *
     * @param ids 需要删除的签名位置信息主键集合
     * @return 结果
     */
    int deleteExamSignLocationInfoByIds(Long[] ids);

    /**
     * 删除签名位置信息信息
     *
     * @param id 签名位置信息主键
     * @return 结果
     */
    int deleteExamSignLocationInfoById(Long id);
}
