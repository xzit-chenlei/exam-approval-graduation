package edu.xzit.core.core.flowable.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import edu.xzit.core.core.common.annotation.Excel;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 签名位置信息对象 exam_sign_location_info
 *
 * @author chenlei
 * @date 2025-03-05
 */
@Data
@TableName("exam_sign_location_info")
public class ExamSignLocationInfo extends BaseEntity{
private static final long serialVersionUID = 1L;

        /** 主键 */
            @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        /** 表单id */
                @Excel(name = "表单id")
        private Long formId;

        /** 表单名称 */
                @Excel(name = "表单名称")
        private String formName;

        /** 流程id */
        @Excel(name = "流程id")
        private String flowId;

        /** 流程名称 */
        @Excel(name = "流程名称")
        private String flowName;


        /** 节点id */
        @Excel(name = "节点id")
        private String nodeId;

        /** 节点名称 */
        @Excel(name = "签名人名称")
        private String nodeName;


        /** 签名位置 */
                @Excel(name = "签名位置")
        private String signatureLocation;

        /**
         * 逻辑删除
         */
        @TableLogic
        private Long isValid;

        /** 签名位置 */
        @Excel(name = "签名类型")
        private String examElectronicType;




}
