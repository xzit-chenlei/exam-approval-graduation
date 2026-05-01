package edu.xzit.graduate.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 毕业达成度课程表 graduate_course
 *
 * @author chenlei
 * @date 2025-08-25
 */
@Data
@TableName("graduate_course")
public class GraduateCourse extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 所属专业ID */
    @Excel(name = "所属专业ID")
    private Long majorId;

    /** 课程编号 */
    @Excel(name = "课程编号")
    private String code;

    /** 课程名称 */
    @Excel(name = "课程名称")
    private String name;

    /** 显示排序 */
    @Excel(name = "排序")
    private Integer orderNo;

    /** 备注（持久化字段，覆盖 BaseEntity 中的非持久化 remark） */
    @Excel(name = "备注")
    private String remark;

    /** 入学年级 */
    @Excel(name = "年级")
    private Integer grade;

    /** 逻辑删除 */
    @TableLogic
    private Long isValid;
}

