package edu.xzit.core.core.flowable.domain.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import edu.xzit.core.core.common.annotation.Excel;
import lombok.Builder;
import lombok.Data;

@Data
public class ExamWordTemplateInfoDto {

    /** id */
    private Long id;

    /** 表单id */
    private Long formId;

    /** 表单名称 */
    private String formName;

    /** 部门id */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 存储表id */
    private Long storeInfoId;

    /** word模板名称 */
    private String wordTemplateName;

    /** word模板类型 */
    private String wordTemplateType;

    /** 对应文件标签 */
    private String correspondingFileLabel;

    /** 对应文件名称 */
    private String correspondingFileName;

    private String remark;

    /** 文件地址 */
    private String fileUrl;

    /**
     * 对象存储的key
     */
    private String ossKey;
}
