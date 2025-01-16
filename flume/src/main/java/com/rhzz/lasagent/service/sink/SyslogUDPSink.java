package com.rhzz.lasagent.service.sink;


import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import java.sql.*;
import java.time.LocalDateTime;


public class SyslogUDPSink extends AbstractSink implements Configurable {

    private String jdbcUrl;
    private String user;
    private String password;

    @Override
    public void configure(Context context) {
        jdbcUrl = context.getString("jdbcUrl");
        user = context.getString("user");
        password = context.getString("password");
    }

    @Override
    public Status process() throws EventDeliveryException {
        System.out.println("--------- jdbcUrl: " + jdbcUrl);
        System.out.println("--------- user: " + user);
        System.out.println("--------- password: " + password);
        Channel channel = getChannel();
        Connection connection = null;
        PreparedStatement statement = null;
        Transaction txn = channel.getTransaction();
        try {
            txn.begin();  // 开启事务
            Event event = channel.take();  // 从channel中获取数据
            if (event != null) {
//                String body = event.getHeaders().get("message");
                String data = new String(event.getBody());
                connection = DriverManager.getConnection(jdbcUrl, user, password);
                statement = connection.prepareStatement("insert into test(content, create_time) values(?,?)");
                saveToMysql(data, statement);
            }
            txn.commit();  // 提交事务
            return Status.READY;
        } catch (Throwable tx) {
            try {
                txn.rollback();
            } catch (Exception ex) {
                System.out.println("exception in rollback." + ex);
            }
            System.out.println("transaction rolled back." + tx);
            return Status.BACKOFF;
        } finally {
            txn.close();

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveToMysql(String data, PreparedStatement statement) throws SQLException {

        statement.setString(1, data);
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);
        statement.setTimestamp(2, timestamp);
        statement.executeUpdate();
    }

}
