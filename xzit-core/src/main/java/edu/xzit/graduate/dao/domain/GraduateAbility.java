package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 毕业达成度能力表 graduate_ability
 *
 * 支持父子层级（parentId）
 */
@Data
@TableName("graduate_ability")
public class GraduateAbility extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属专业ID */
    @Excel(name = "所属专业ID")
    private Long majorId;

    /** 父能力ID，顶层为NULL */
    @Excel(name = "父能力ID")
    private Long parentId;

    /** 能力名称 */
    @Excel(name = "能力名称")
    private String name;

    /** 显示排序 */
    @Excel(name = "排序")
    private Integer orderNo;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "能力描述")
    private String remark;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}

