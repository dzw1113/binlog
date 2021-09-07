package io.github.dzw1113.binlog.event;

import com.github.shyiko.mysql.binlog.event.QueryEventData;

/**
 * @description: ddl语句
 * @author: dzw
 * @date: 2021/09/06 15:10
 **/
public class SQLQueryEventData extends QueryEventData {
    
    String customSql;
    
    public String getCustomSql() {
        if(super.getSql().indexOf("CREATE DATABASE") != -1){
            return super.getSql();
        }
        if(super.getSql().equals("BEGIN")){
            return "";
        }
        return "use " + getDatabase() + ";\n" + super.getSql() + ";";
    }
    
}
