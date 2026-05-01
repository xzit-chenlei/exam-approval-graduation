package edu.xzit.core.core.flowable.enums;

public enum WordTemplateType {


    /**
     * 说明
     */
    MAIN_FORM("1", "主表单"),
    ATTACHMENT("2", "附件");


    /**
     * 类型
     */
    private final String type;

    /**
     * 说明
     */
    private final String remark;

    WordTemplateType(String type, String remark) {
        this.type = type;
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }
}
