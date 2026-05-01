package edu.xzit.core.examEnums;

/**
 * 表单模块类型枚举
 * 对应前端下拉菜单的组件类型
 *
 * @author system
 */
public enum FormModuleType {

    /**
     * 数据类型（二维数据列表）
     */
    DATA_TABLE("1", "data_table", "数据类型（二维数据列表）"),

    /**
     * 文件类型（文件上传）
     */
    FILE_UPLOAD("2", "file_upload", "文件类型（文件上传）"),

    /**
     * 日期选择
     */
    DATE_PICKER("3", "date_picker", "日期选择"),

    /**
     * 文本输入（标签+输入框）
     */
    TEXT_INPUT("4", "text_input", "文本输入（标签+输入框）"),

    /**
     * 单选
     */
    RADIO("5", "radio", "单选"),

    /**
     * 勾选矩阵（二维勾选表）
     */
    CHECK_MATRIX("6", "check_matrix", "勾选矩阵（二维勾选表）"),

    /**
     * 图片上传
     */
    IMAGE_UPLOAD("7", "image_upload", "图片上传");

    /**
     * 类型值（对应前端command）
     */
    private final String type;

    /**
     * 映射值（英文标识）
     */
    private final String reflect;

    /**
     * 说明
     */
    private final String remark;

    FormModuleType(String type, String reflect, String remark) {
        this.type = type;
        this.reflect = reflect;
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public String getReflect() {
        return reflect;
    }

    public String getRemark() {
        return remark;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param type 类型值
     * @return 对应的枚举，如果不存在返回null
     */
    public static FormModuleType getByType(String type) {
        for (FormModuleType value : FormModuleType.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据映射值获取枚举
     *
     * @param reflect 映射值
     * @return 对应的枚举，如果不存在返回null
     */
    public static FormModuleType getByReflect(String reflect) {
        for (FormModuleType value : FormModuleType.values()) {
            if (value.getReflect().equals(reflect)) {
                return value;
            }
        }
        return null;
    }
}
