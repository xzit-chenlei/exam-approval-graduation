package edu.xzit.core.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 考试模块对象 exam_module
 *
 * @author
 * @date 2025-11-09
 */
@Data
@TableName("exam_module")
public class ExamModule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模块名
     */
    @Excel(name = "模块名")
    private String name;

    /**
     * 模块类型：1-数据类型，2-文件类型，3-日期选择
     */
    @Excel(name = "模块类型")
    private Integer type;

    /**
     * 模块字段标识
     */
    @Excel(name = "模块字段标识")
    @TableField("field_id")
    private String fieldId;

    /**
     * 行头
     */
    @Excel(name = "行头")
    @TableField("row_headers")
    private String rowHeaders;

    /**
     * 列头
     */
    @Excel(name = "列头")
    @TableField("col_headers")
    private String colHeaders;

    /**
     * 数据
     */
    @Excel(name = "数据")
    private String data;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remark;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long isValid;
}

