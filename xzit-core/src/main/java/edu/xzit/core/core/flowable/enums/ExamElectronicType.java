package edu.xzit.core.core.flowable.enums;

public enum ExamElectronicType {


    /**
     * 电子签名
     */
    ELECTRONIC_SIGNATURE("0","electronic_signature","名字电子签章"),

    ELECTRONIC_AGREE("1","electronic_agree","同意电子签章");

    /**
     * 类型
     */
    private final String type;

    /**
     * 映射值
     */
    private final String reflect;


    /**
     * 说明
     */
    private final String remark;

    ExamElectronicType(String type, String reflect,String remark) {
        this.type = type;
        this.reflect = reflect;
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public String getReflect() {
        return reflect;
    }

    public static ExamElectronicType getByType(String type) {
        for (ExamElectronicType value : ExamElectronicType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
