package io.github.dzw1113.binlog.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.RotateEventData;
import com.github.shyiko.mysql.binlog.event.RowsQueryEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.XidEventData;

import io.github.dzw1113.binlog.event.SQLQueryEventData;
import io.github.dzw1113.binlog.jdbc.JdbcUtil;
import io.github.dzw1113.binlog.model.HttpResult;
import io.github.dzw1113.binlog.model.JdbcVO;

/**
 * @description:
 * @author: dzw
 * @date: 2021/09/06 10:10
 **/
public class RowBinlogTask implements Runnable {
    
    
    private static final HashMap<Long, List<String>> tableColumnMap = new HashMap<>();
    private static final HashMap<Long, String> tableIdNameMap = new HashMap<>();
    private static final HashMap<Long, String> tableIdDataBaseMap = new HashMap<>();
    
    private final BinaryLogClient client;
    private final JdbcVO jdbcVO;
    private final SimpMessagingTemplate messageTemplate;
    
    public RowBinlogTask(BinaryLogClient client, JdbcVO jdbcVO, SimpMessagingTemplate messageTemplate) {
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
                    TableMapEventData tableMapEventData = (TableMapEventData) data;
                    if (tableColumnMap.get(tableMapEventData.getTableId()) == null) {
                        try {
                            List<String> colList = JdbcUtil.listColumnByTableName(jdbcVO, tableMapEventData.getTable(), tableMapEventData.getDatabase());
                            tableColumnMap.put(tableMapEventData.getTableId(), colList);
                            tableIdNameMap.put(tableMapEventData.getTableId(), tableMapEventData.getTable());
                            tableIdDataBaseMap.put(tableMapEventData.getTableId(), tableMapEventData.getDatabase());
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
                else if (data instanceof UpdateRowsEventData) {
                    UpdateRowsEventData updateRowsEventData = (UpdateRowsEventData) data;
                    List colList = tableColumnMap.get(updateRowsEventData.getTableId());
                    String tableName = tableIdNameMap.get(updateRowsEventData.getTableId());
                    String database = tableIdDataBaseMap.get(updateRowsEventData.getTableId());
                    if (colList != null) {
                        for (Map.Entry<Serializable[], Serializable[]> row : updateRowsEventData.getRows()) {
                            List<Serializable> afterVal = Arrays.asList(row.getValue());
                            List<Serializable> beforeVal = Arrays.asList(row.getKey());
                            StringBuilder sb = new StringBuilder("update `" + database + "`.`" + tableName + "` set ");
                            for (int i = 0; i < afterVal.size(); i++) {
                                sb.append("`" + colList.get(i) + "` = '" + afterVal.get(i).toString() + "' ");
                            }
                            for (int i = 0; i < beforeVal.size(); i++) {
                                if (i == 0) {
                                    sb.append(" where `" + colList.get(i) + "` = " + beforeVal.get(i).toString() + " ");
                                } else {
                                    sb.append(" and `" + colList.get(i) + "` = " + beforeVal.get(i).toString() + " ");
                                }
                            }
                            messageTemplate.convertAndSend("/topic/message", HttpResult.ok(sb.toString()));
                        }
                    }
                }
                else if (data instanceof WriteRowsEventData) {
                    WriteRowsEventData eventData = (WriteRowsEventData) data;
                    List colList = tableColumnMap.get(eventData.getTableId());
                    String tableName = tableIdNameMap.get(eventData.getTableId());
                    String database = tableIdDataBaseMap.get(eventData.getTableId());
                    if (colList != null) {
                        StringBuilder sb = new StringBuilder("insert `" + database + "`.`" + tableName + "` ( ");
                        int[] fieldArray = bitSet2IntArr(eventData.getIncludedColumns());
                        for (int i = 0; i < fieldArray.length; i++) {
                            sb.append(colList.get(fieldArray[i]) + ",");
                        }
                        sb.append(" ) values ");
                        for (int i = 0; i < eventData.getRows().size(); i++) {
                            sb.append("(");
                            for (int x = 0; x < eventData.getRows().get(i).length; x++) {
                                if (eventData.getRows().get(i).length - 1 == x) {
                                    sb.append("`" + eventData.getRows().get(i)[x].toString() + "`");
                                } else {
                                    sb.append("`" + eventData.getRows().get(i)[x].toString() + "`,");
                                }
                            }
                            if (eventData.getRows().size() - 1 == i) {
                                sb.append(");");
                            } else {
                                sb.append("),");
                            }
                        }
                        messageTemplate.convertAndSend("/topic/message", HttpResult.ok(sb.toString()));
                    }
                }
                else if (data instanceof DeleteRowsEventData) {
                    DeleteRowsEventData eventData = (DeleteRowsEventData) data;
                    List colList = tableColumnMap.get(eventData.getTableId());
                    String tableName = tableIdNameMap.get(eventData.getTableId());
                    String database = tableIdDataBaseMap.get(eventData.getTableId());
                    if (colList != null) {
                        StringBuilder sb = new StringBuilder();
                        int[] fieldArray = bitSet2IntArr(eventData.getIncludedColumns());
                        for (int i = 0; i < eventData.getRows().size(); i++) {
                            if (i == 0) {
                                sb.append("DELETE `" + database + "`.`" + tableName + "` where ");
                            }
                            for (int x = 0; x < eventData.getRows().get(i).length; x++) {
                                sb.append(colList.get(fieldArray[x]) + " = `" + eventData.getRows().get(i)[x].toString() + "`");
                                if (eventData.getRows().get(i).length - 1 == x) {
                                    sb.append(" ;\n");
                                } else {
                                    sb.append(" and ");
                                }
                            }
                            if (eventData.getRows().size() - 1 == i) {
                                sb.append("\n");
                            } else {
                                sb.append("DELETE `" + database + "`.`" + tableName + "` where ");
                            }
                        }
                        messageTemplate.convertAndSend("/topic/message", HttpResult.ok(sb.toString()));
                    }
                }
                else if (data instanceof SQLQueryEventData) {
                    SQLQueryEventData queryEventData = (SQLQueryEventData) data;
                    if(StringUtils.isNotBlank(queryEventData.getCustomSql())){
                        messageTemplate.convertAndSend("/topic/message", HttpResult.ok(queryEventData.getCustomSql()));
                    }
                }
                else if (data instanceof RowsQueryEventData) {
                    //如果开启了row下sql注释set binlog_rows_query_log_events=1;
                    RowsQueryEventData eventData = (RowsQueryEventData) data;
                    System.out.println("注释sql：" + eventData.getQuery());
                }
                else if(data instanceof XidEventData){
                    //事务提交xid
                }
                else if(data instanceof RotateEventData){
                    //FLUSH LOGS
                    System.out.println("变更nextPosition以及fileName");
                    RotateEventData eventData = (RotateEventData) data;
                    client.setBinlogFilename(eventData.getBinlogFilename());
                    client.setBinlogPosition(eventData.getBinlogPosition());
                }
                else{
                    System.out.println(data.getClass());
                    System.out.println(data.toString());
                }
            });
            try {
                client.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().isInterrupted();
                System.out.println("外部中断异常！");
            }
        }
    }
    
    public int[] bitSet2IntArr(BitSet bitSet) {
        int[] arr = new int[bitSet.length()];
        for (int i = bitSet.nextSetBit(0), y = 0; i >= 0; i = bitSet.nextSetBit(i + 1), y++) {
            arr[y] = i;
        }
        return arr;
    }
    
    
}
