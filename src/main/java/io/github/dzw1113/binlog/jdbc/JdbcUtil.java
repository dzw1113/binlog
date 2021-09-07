package io.github.dzw1113.binlog.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.zaxxer.hikari.HikariDataSource;

import io.github.dzw1113.binlog.model.JdbcVO;

/**
 * @description: jdbc工具类
 * @author: dzw
 * @date: 2021/09/06 10:17
 **/
public class JdbcUtil {
    
    /**
     * 获取所有数据库
     *
     * @param jdbcVO
     * @return
     */
    public static List<String> showDataBase(JdbcVO jdbcVO) {
        QueryRunner queryRunner = new QueryRunner(getDataSource(jdbcVO));
        List<String> databaseList = null;
        try {
            databaseList = queryRunner.query("SHOW DATABASES", new ColumnListHandler<>());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return databaseList;
    }
    
    /**
     * 获取数据库的binlog_format参数
     *
     * @param jdbcVO
     * @return
     */
    public static String showBinlogFormat(JdbcVO jdbcVO) {
        String binLogFormat = (String) showVariables(jdbcVO).stream().map(map -> {
            if (Objects.equals(map.get("Variable_name"), "binlog_format")) {
                return map.get("Value");
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList()).stream().findFirst().get();
        return binLogFormat;
    }
    
    /**
     * 获取数据库的参数
     *
     * @param jdbcVO
     * @return
     */
    public static List<Map<String, Object>> showVariables(JdbcVO jdbcVO) {
        QueryRunner queryRunner = new QueryRunner(getDataSource(jdbcVO));
        List<Map<String, Object>> variablesList = null;
        try {
            variablesList = queryRunner.query("show variables", new MapListHandler());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return variablesList;
    }
    
    /**
     * 获取数据源
     *
     * @param jdbcVO
     * @return
     */
    public static HikariDataSource getDataSource(JdbcVO jdbcVO) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + jdbcVO.getHost() + ":" + jdbcVO.getPort() + "/");
        hikariDataSource.setDriverClassName(jdbcVO.getDriverClassName());
        hikariDataSource.setUsername(jdbcVO.getUsername());
        hikariDataSource.setPassword(jdbcVO.getPassword());
        return hikariDataSource;
    }
    
    /**
     * 根据表名数据库名获取列信息
     *
     * @param jdbcVO
     * @param tableName
     * @param databaseName
     * @return
     * @throws SQLException
     */
    public static List<String> listColumnByTableName(JdbcVO jdbcVO, String tableName, String databaseName) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(getDataSource(jdbcVO));
        String sql = "select COLUMN_NAME from information_schema.`COLUMNS` a where a.TABLE_SCHEMA = '" + databaseName + "' and table_name = '" + tableName + "' order by ORDINAL_POSITION;";
        List<String> columnList = queryRunner.query(sql, new ColumnListHandler<>());
        return columnList;
    }
    
}
