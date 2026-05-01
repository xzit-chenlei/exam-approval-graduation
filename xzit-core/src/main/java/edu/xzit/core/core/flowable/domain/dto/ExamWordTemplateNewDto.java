package edu.xzit.core.core.flowable.domain.dto;

import lombok.Data;

/**
 * word模板主表 DTO
 */
@Data
public class ExamWordTemplateNewDto {

    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板唯一标识 */
    private String templateKey;

    /** 流程定义key */
    private String flowableKey;

    /** 模板描述 */
    private String description;

    /** 模板行数 */
    private Integer rowCount;

    /** 模板列数 */
    private Integer columnCount;

    /** 模板版本 */
    private String version;

    /** 对象存储key */
    private String ossKey;

    /** 文件URL */
    private String fileUrl;
}










