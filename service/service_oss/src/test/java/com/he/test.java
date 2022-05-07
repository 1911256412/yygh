package com.he;

import org.joda.time.DateTime;

import java.util.Date;

public class test {

    public static void main(String[] args) {
        DateTime dateTime=new  DateTime();

        System.out.println(dateTime.toString("yyyy/MM/dd"));
    }
}
