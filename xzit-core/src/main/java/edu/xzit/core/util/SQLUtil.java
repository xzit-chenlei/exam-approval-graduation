package edu.xzit.core.util;

public class SQLUtil {

    public static String lastSQL(int batchNum,int batchSize){
        return "limit " + batchNum * batchSize + "," + batchSize;
    }
}
