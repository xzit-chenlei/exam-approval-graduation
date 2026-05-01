package edu.xzit.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static String generateMD5Hash() {
        long timestamp = System.currentTimeMillis();
        try {
            // 获取 MD5 算法的实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将时间戳转换为字节数组
            byte[] inputBytes = String.valueOf(timestamp).getBytes();
            // 计算哈希值
            byte[] hashBytes = md.digest(inputBytes);

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            // 处理算法不存在的异常
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(HashUtil.generateMD5Hash());
    }
}


