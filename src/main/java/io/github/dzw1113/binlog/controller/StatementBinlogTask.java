package io.github.dzw1113.binlog.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import io.github.dzw1113.binlog.event.SQLQueryEventData;
import io.github.dzw1113.binlog.jdbc.JdbcUtil;
import io.github.dzw1113.binlog.model.HttpResult;
import io.github.dzw1113.binlog.model.JdbcVO;

/**
 * @description:
 * @author: dzw
 * @date: 2021/09/06 10:10
 **/
public class StatementBinlogTask implements Runnable {
    
    
    private static final HashMap<Long, List<String>> tableColumnMap = new HashMap<>();
    private static final HashMap<Long, String> tableIdNameMap = new HashMap<>();
    
    private final BinaryLogClient client;
    private final JdbcVO jdbcVO;
    private final SimpMessagingTemplate messageTemplate;
    
    public StatementBinlogTask(BinaryLogClient client, JdbcVO jdbcVO, SimpMessagingTemplate messageTemplate) {
        this.client = client;
        this.jdbcVO = jdbcVO;
        this.messageTemplate = messageTemplate;
    }
    
    @Override
    public void run() {
        try {
            client.registerEventListener(event -> {
                EventData data = event.getData();
                if (data == null) {
                    return;
                }
                System.out.println("header:" + event.getHeader());
                if (data instanceof TableMapEventData) {
                    System.out.println("Table:");
                }else if (data instanceof SQLQueryEventData) {
                    System.out.println("Query:");
                    SQLQueryEventData queryEventData = (SQLQueryEventData) data;
                    System.out.println(queryEventData.getCustomSql());
                    messageTemplate.convertAndSend("/topic/message", HttpResult.ok(queryEventData.getCustomSql()));
                } else {
                    System.out.println(data.getClass());
                    System.out.println(data.toString());
                }
            });
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            if(e instanceof InterruptedException){
                Thread.currentThread().isInterrupted();
                System.out.println("外部中断异常！");
            }
        }
    }
    
}
