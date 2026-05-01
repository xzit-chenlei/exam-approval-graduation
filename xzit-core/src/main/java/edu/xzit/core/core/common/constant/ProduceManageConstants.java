package edu.xzit.core.core.common.constant;

/**
 * 使用枚举代替
 */
@Deprecated
public class ProduceManageConstants {

    private ProduceManageConstants() {

    }

    public static final int STATUS_NOTHING = -1000; // 无状态


    public static final int STATUS_DEMAND_CREATED = -3; // 销售需求单创建
    public static final int STATUS_PLAN_CREATED = -1; // 计划员采购计划创建
    public static final int STATUS_REJECT = -2; // 采购计划创建


    public static final int STATUS_ACCEPT = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_NEARBY = 2;
    public static final int STATUS_EXPIRED = 3;
    public static final int STATUS_NEARBY_REMIND_1 = 4;
    public static final int STATUS_NEARBY_CONFIRM_1 = 5;
    public static final int STATUS_NEARBY_REMIND_2 = 6;
    public static final int STATUS_NEARBY_CONFIRM_2 = 7;
}
