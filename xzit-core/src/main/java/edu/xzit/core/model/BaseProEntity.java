package edu.xzit.core.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import edu.xzit.core.core.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class BaseProEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long isValid;
}
