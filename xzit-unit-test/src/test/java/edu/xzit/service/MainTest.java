package edu.xzit.service;

import cn.hutool.core.date.DateUtil;

import java.util.Date;

public class MainTest {

    public static void main(String[] args) {
        System.out.println(DateUtil.offsetDay(new Date(), 1));
    }
}
