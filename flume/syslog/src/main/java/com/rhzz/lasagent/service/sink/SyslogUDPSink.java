package com.rhzz.lasagent.service.sink;


import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import java.sql.*;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SyslogUDPSink extends AbstractSink implements Configurable {

    private Logger log = LoggerFactory.getLogger(SyslogUDPSink.class);

    private String jdbcUrl;
    private String user;
    private String password;

    private PreparedStatement preparedStatement;
    private Connection conn;


    @Override
    public void configure(Context context) {
        jdbcUrl = context.getString("jdbcUrl");
        user = context.getString("user");
        password = context.getString("password");
        log.info("--------- jdbcUrl: " + jdbcUrl);
        log.info("--------- user: " + user);
        log.info("--------- password: " + password);
    }

    @Override
    public void start() {
        super.start();
        try {
            conn = DriverManager.getConnection(jdbcUrl, user, password);
//            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement("insert into t_log_msg(log_msg, create_time) values(?,?)");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Channel channel = getChannel();
        Transaction txn = channel.getTransaction();
        try {
            txn.begin();  // 开启事务
            Event event = channel.take();  // 从channel中获取数据
            if (event != null) {
//                String body = event.getHeaders().get("message");
                String data = new String(event.getBody());
                saveToMysql(data, preparedStatement);
            }
            txn.commit();  // 提交事务
            return Status.READY;
        } catch (Exception e) {
            try {
                txn.rollback();
            } catch (Exception ex) {
                log.error("Exception in rollback. Rollback might not have been.successful." + ex);
            }
            log.error("Failed to commit transaction.Transaction rolled back." + e);
            return Status.BACKOFF;
        } finally {
            txn.close();
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
