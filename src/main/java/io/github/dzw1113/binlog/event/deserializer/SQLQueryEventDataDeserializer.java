package io.github.dzw1113.binlog.event.deserializer;

import java.io.IOException;

import com.github.shyiko.mysql.binlog.event.QueryEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDataDeserializer;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;

import io.github.dzw1113.binlog.event.SQLQueryEventData;

/**
 * @description: ddl
 * @author: dzw
 * @date: 2021/09/06 15:16
 **/
public class SQLQueryEventDataDeserializer implements EventDataDeserializer<SQLQueryEventData> {
    
    @Override
    public SQLQueryEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        SQLQueryEventData eventData = new SQLQueryEventData();
        eventData.setThreadId(inputStream.readLong(4));
        eventData.setExecutionTime(inputStream.readLong(4));
        inputStream.skip(1); // length of the name of the database
        eventData.setErrorCode(inputStream.readInteger(2));
        inputStream.skip(inputStream.readInteger(2)); // status variables block
        String database = inputStream.readZeroTerminatedString();
        eventData.setDatabase(database);
        String sql = inputStream.readString(inputStream.available());
        eventData.setSql(sql);
        return eventData;
    }
}
