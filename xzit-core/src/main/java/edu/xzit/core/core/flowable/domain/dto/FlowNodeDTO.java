package edu.xzit.core.core.flowable.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class FlowNodeDTO {

    private String id;
    private String name;

    private List<NodeInfoDto> nodeInfoDtoList;

}
