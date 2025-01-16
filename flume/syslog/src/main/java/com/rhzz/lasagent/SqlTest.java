package com.rhzz.lasagent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SqlTest {


    public static void main(String [] args) throws Exception {
//         Class.forName( "com.mysql.jdbc.Driver" ); // do this in init
        // // edit the jdbc url
//        Connection conn = DriverManager.getConnection(
//                "jdbc:mysql://192.168.3.57:3310/dsp?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true", "root", "RHZZdsp_2020");
        // Statement st = conn.createStatement();
        // ResultSet rs = st.executeQuery( "select * from table" );


        String url = String.format("jdbc:mysql://192.168.3.57:3310/demo001?useUnicode=true&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&useSSL=false");
        Connection conn = DriverManager.getConnection(url, "root", "123456");
        System.out.println("-------- 41 ------------");
        Connection connection = null;
        PreparedStatement statement = null;
        System.out.println("-------- 43 ------------");
        statement = conn.prepareStatement("insert into test(content) values(?)");
        System.out.println("-------- 45 ------------");
        statement.setString(1, "hehe");
        System.out.println("-------- 81 ------------");
        statement.executeUpdate();


        System.out.println("Connected?");
    }
}
