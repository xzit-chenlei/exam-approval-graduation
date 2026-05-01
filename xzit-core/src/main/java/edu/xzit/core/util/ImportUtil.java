package edu.xzit.core.util;

public class ImportUtil {

    public static String generateResult(int successNum,int failNum,String successMsg,String failMsg){
        String msgHeader = "导入结束，总共" + (successNum + failNum) + "条数据，导入成功" + successNum + "条，导入失败" + (failNum) + "条";
        return msgHeader + "<br/>" + successMsg + "<br/>" + failMsg;
    }
}
