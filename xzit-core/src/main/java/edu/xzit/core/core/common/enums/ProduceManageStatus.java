package edu.xzit.core.core.common.enums;

import java.util.*;

public enum ProduceManageStatus {

    STATUS_NOTHING(-1000, "无状态", "无状态"),
    STATUS_DEMAND_CREATED(-3, "销售员创建需求，但计划员未传递", "待传递"),
    STATUS_PLAN_CREATED(-1, "计划员创建计划，但采购员未接受", "待接收"),
    STATUS_REJECT(-2, "采购员驳回计划", "已驳回"),
    STATUS_REJECT_AND_UPDATE(-4, "采购员驳回计划，但同时更新", "已驳回"),
    STATUS_ACCEPT(0, "采购员接受计划，但未完成", "已接收"),
    STATUS_EXPIRED_AND_ACCEPT(10, "采购员接受计划，但同时也过期", "延期处理"),
    STATUS_FINISHED(1, "采购计划完成", "已完成"),
    STATUS_FINISHED_AND_EXPIRED(13, "确认收货，但同时也过期", "过期完成"),
    STATUS_NEARBY(2, "采购计划即将到期", "即将到期"),
    STATUS_EXPIRED(3, "采购计划已过期", "已过期"),
    STATUS_NEARBY_REMIND_1(4, "采购计划即将到期提醒1", "即将到期提醒1"),
    STATUS_NEARBY_CONFIRM_1(5, "采购计划即将到期确认1", "即将到期确认1"),
    STATUS_NEARBY_REMIND_2(6, "采购计划即将到期提醒2", "即将到期提醒2"),
    STATUS_NEARBY_CONFIRM_2(7, "采购计划即将到期确认2", "即将到期确认2"),

    STATUS_TERMINAL(-5, "采购计划终止", "已取消"),
    STATUS_CANCEL(-6, "采购计划取消", "已取消"),
    ;
    private final int code;
    private final String name;

    private final String shortName;

    public static final Set<ProduceManageStatus> ALL_STATUS_REJECT = new HashSet<>(Arrays.asList(
            STATUS_REJECT, STATUS_REJECT_AND_UPDATE
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_ACCEPT = new HashSet<>(Arrays.asList(
            STATUS_ACCEPT, STATUS_EXPIRED_AND_ACCEPT,STATUS_NEARBY_CONFIRM_1,STATUS_NEARBY_CONFIRM_2
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_FINISHED = new HashSet<>(Arrays.asList(
            STATUS_FINISHED, STATUS_FINISHED_AND_EXPIRED
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_EXPIRED = new HashSet<>(Arrays.asList(
            STATUS_EXPIRED, STATUS_FINISHED_AND_EXPIRED, STATUS_EXPIRED_AND_ACCEPT
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_NEARBY = new HashSet<>(Arrays.asList(
            STATUS_NEARBY, STATUS_NEARBY_REMIND_1, STATUS_NEARBY_CONFIRM_1, STATUS_NEARBY_REMIND_2, STATUS_NEARBY_CONFIRM_2
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_TERMINAL = new HashSet<>(Arrays.asList(
            STATUS_FINISHED, STATUS_FINISHED_AND_EXPIRED, STATUS_TERMINAL, STATUS_EXPIRED_AND_ACCEPT, STATUS_EXPIRED, STATUS_CANCEL
    ));

    public static final Set<ProduceManageStatus> ALL_STATUS_ABNORMAL_FINISH = new HashSet<>(Arrays.asList(
            STATUS_REJECT, STATUS_EXPIRED_AND_ACCEPT, STATUS_EXPIRED, STATUS_TERMINAL, STATUS_CANCEL
    ));

    public static List<Integer> toCodeList(Set<ProduceManageStatus> statusSet) {
        List<Integer> codeList = new ArrayList<>();
        for (ProduceManageStatus status : statusSet) {
            codeList.add(status.getCode());
        }
        return codeList;

    }

    ProduceManageStatus(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public static ProduceManageStatus getByCode(int code) {
        for (ProduceManageStatus status : ProduceManageStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return STATUS_NOTHING;
    }

    public static boolean canItCancel(int code) {
        return ALL_STATUS_REJECT.contains(getByCode(code)) || ALL_STATUS_TERMINAL.contains(getByCode(code));
    }

    public static boolean planerCanCancel(int code) {
        return STATUS_REJECT.code == code;
    }

    public static boolean canItReject(int code) {
        return !ALL_STATUS_REJECT.contains(getByCode(code)) && !ALL_STATUS_TERMINAL.contains(getByCode(code));
    }


}
