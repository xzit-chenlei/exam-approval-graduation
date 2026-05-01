package edu.xzit.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.xzit.core.dao.domain.SysStoreInfo;

import java.util.List;


/**
 * 文件存储Mapper接口
 *
 * @author chenlei
 * @date 2024-10-03
 */
public interface SysStoreInfoMapper extends BaseMapper<SysStoreInfo> {
    /**
     * 查询文件存储
     *
     * @param id 文件存储主键
     * @return 文件存储
     */
    SysStoreInfo selectSysStoreInfoById(Long id);

    /**
     * 查询文件存储列表
     *
     * @param sysStoreInfo 文件存储
     * @return 文件存储集合
     */
    List<SysStoreInfo> selectSysStoreInfoList(SysStoreInfo sysStoreInfo);

    /**
     * 新增文件存储
     *
     * @param sysStoreInfo 文件存储
     * @return 结果
     */
    int insertSysStoreInfo(SysStoreInfo sysStoreInfo);

    /**
     * 修改文件存储
     *
     * @param sysStoreInfo 文件存储
     * @return 结果
     */
    int updateSysStoreInfo(SysStoreInfo sysStoreInfo);

    /**
     * 删除文件存储
     *
     * @param id 文件存储主键
     * @return 结果
     */
    int deleteSysStoreInfoById(Long id);

    /**
     * 批量删除文件存储
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSysStoreInfoByIds(Long[] ids);
}
