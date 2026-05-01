package edu.xzit.core.core.flowable.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * word模板信息对象 exam_word_template_info
 *
 * @author chenlei
 * @date 2025-03-09
 */
@Data
@TableName("exam_word_template_info")
public class ExamWordTemplateInfo extends BaseEntity{
private static final long serialVersionUID = 1L;

        /** id */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 表单id */
                @Excel(name = "表单id")
        private Long formId;

        /** 表单名称 */
                @Excel(name = "表单名称")
        private String formName;

        /** 存储表id */
                @Excel(name = "存储表id")
        private Long storeInfoId;

        /** word模板名称 */
                @Excel(name = "word模板名称")
        private String wordTemplateName;

        /** word模板类型 */
                @Excel(name = "word模板类型")
        private String wordTemplateType;

        /** 对应文件标签 */
                @Excel(name = "对应文件标签")
        private String correspondingFileLabel;

        /** 对应文件名称 */
                @Excel(name = "对应文件名称")
        private String correspondingFileName;

                @Excel(name = "备注")
        private String remark;

        /** 逻辑字符 */
            @TableLogic
        private Long isValid;

        /** 存储表id */
        @Excel(name = "部门id")
        private Long deptId;

        /** word模板名称 */
        @Excel(name = "部门名称")
        private String deptName;


}
