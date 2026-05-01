package edu.xzit.core.core.flowable.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * word模板主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_word_template_new")
public class ExamWordTemplateNew extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    @Excel(name = "模板名称")
    private String templateName;

    /** 模板唯一标识 */
    @Excel(name = "模板唯一标识")
    private String templateKey;

    /** 流程定义key */
    @Excel(name = "流程定义key")
    private String flowableKey;

    /** 模板描述 */
    @Excel(name = "模板描述")
    private String description;

    /** 模板行数 */
    @Excel(name = "模板行数")
    private Integer rowCount;

    /** 模板列数 */
    @Excel(name = "模板列数")
    private Integer columnCount;

    /** 模板版本 */
    @Excel(name = "模板版本")
    private String version;

    /** 逻辑删除：0正常 1删除 */
    @TableLogic
    private Integer isValid;
}

