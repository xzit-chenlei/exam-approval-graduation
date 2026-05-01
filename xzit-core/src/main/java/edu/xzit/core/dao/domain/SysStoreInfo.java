package edu.xzit.core.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 文件存储对象 sys_store_info
 *
 * @author chenlei
 * @date 2024-10-03
 */
@Data
@TableName("sys_store_info")
public class SysStoreInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long isValid;

    /**
     * 对象存储的key
     */
    @Excel(name = "对象存储的key")
    private String ossKey;

    /**
     * 文件url
     */
    @Excel(name = "文件url")
    private String url;

    /**
     * 场景名称。可以用类名或者函数名代替
     */
    @Excel(name = "场景名称。可以用类名或者函数名代替")
    private String sceneName;

    /**
     * 关联外键
     */
    @Excel(name = "关联外键")
    private Long objId;

    private String shortLink;


}
