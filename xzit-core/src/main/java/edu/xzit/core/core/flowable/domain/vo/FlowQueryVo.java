package edu.xzit.core.core.flowable.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>流程任务<p>
 *
 * @author Tony
 * @date 2021-04-03
 */
@Data
@ApiModel("工作流任务相关--请求参数")
public class FlowQueryVo {

    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("当前页码")
    private Integer pageNum;

    @ApiModelProperty("每页条数")
    private Integer pageSize;

    @ApiModelProperty("课题组")
    private Long deptId;

    @ApiModelProperty("教研室ID")
    private Long officeId;

    @ApiModelProperty("流程定义名称")
    private String procDefName;

    @ApiModelProperty("是否完结（true:已完成, false:进行中, null:全部）")
    private Boolean finished;

    @ApiModelProperty("是否配备模板（1:未配备模板, 2:月内未更新模板, 3:月内已更新模板, 4:未配备||月内未更新, null:全部）")
    private Integer templateStatus;

    @ApiModelProperty("流程实例ID（可选）")
    private String procInsId;

    @ApiModelProperty("表单模式（custom:自定义表单, vform:VForm表单，用于数据隔离筛选）")
    private String formMode;
}
