package edu.xzit.core.core.common.core.domain;

import lombok.Data;

import java.util.Date;

@Data
public class BaseBizEntity {

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private Date updateTime;


    private Integer isDeleted;
}
