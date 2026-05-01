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
 * 考试表单对象 exam_form
 *
 * @author
 * @date 2025-11-09
 */
@Data
@TableName("exam_form")
public class ExamForm extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 表单名
     */
    @Excel(name = "表单名")
    private String name;

    /**
     * 关联模块ID集合
     */
    @Excel(name = "关联模块ID集合")
    @TableField("module_ids")
    private String moduleIds;

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

