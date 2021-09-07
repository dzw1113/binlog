# Getting Started

###解决通过java订阅binlog，解析数据库的dml/dcl/ddl转换成sql文件进行存储。

maven引用或者直接下代码进行编译

```
<dependency>
    <groupId>io.github.dzw1113</groupId>
    <artifactId>binlog</artifactId>
    <version>1.0.1</version>
</dependency>
主要依赖：
<dependency>
    <groupId>com.github.shyiko</groupId>
    <artifactId>mysql-binlog-connector-java</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 1、配置mysql.ini信息(可以不用这么多)

```
server-id =1
user=mysql
log-bin=master-mysql-bin
character-set-server = utf8mb4
default-storage-engine  = InnoDB
log-bin=mysql-bin
binlog_format=mixed
log_bin_trust_function_creators=1
log_slave_updates=1
gtid_mode=ON
enforce_gtid_consistency=ON
auto_increment_increment = 2
query-cache-size  = 0
external-locking = FALSE
max_allowed_packet = 32M
sort_buffer_size = 2M
join_buffer_size = 2M
thread_cache_size = 51
query_cache_size = 32M
tmp_table_size = 96M
query_cache_type=1
log-error=D:\mysql\mysql-5.7.20-winx64-master2\mysqld.log
slow_query_log = 1
slow_query_log_file = D:\mysql\mysql-5.7.20-winx64-master2\slow.log
long_query_time = 0.1
expire-logs-days = 14
sync_binlog = 1
binlog_cache_size = 4M
max_binlog_cache_size = 8M
max_binlog_size = 1024M
log_slave_updates
binlog_format = MIXED
#这里使用的混合模式复制
relay_log_recovery = 1
#不需要同步的表
replicate-wild-ignore-table=mydb.sp_counter
#不需要同步的库
replicate-ignore-db = mysql,information_schema,performance_schema
key_buffer_size = 32M
read_buffer_size = 1M
read_rnd_buffer_size = 16M
bulk_insert_buffer_size = 64M
myisam_sort_buffer_size = 128M
myisam_max_sort_file_size = 10G
myisam_repair_threads = 1
transaction_isolation = REPEATABLE-READ
#innodb_additional_mem_pool_size = 16M
innodb_buffer_pool_size = 1024M
innodb_buffer_pool_load_at_startup = 1
innodb_buffer_pool_dump_at_shutdown = 1
#innodb_data_file_path = ibdata1:1024M:autoextend
innodb_flush_log_at_trx_commit = 2
innodb_log_buffer_size = 32M
innodb_log_file_size = 2G
innodb_log_files_in_group = 2
innodb_io_capacity = 4000
innodb_io_capacity_max = 8000
innodb_max_dirty_pages_pct = 50
# innodb_flush_method = O_DIRECT
innodb_file_format = Barracuda
innodb_file_format_max = Barracuda
innodb_lock_wait_timeout = 10
innodb_rollback_on_timeout = 1
innodb_print_all_deadlocks = 1
innodb_file_per_table = 1
innodb_locks_unsafe_for_binlog = 0
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0
```

## 2、启动项目访问Swagger（http://127.0.0.1:19911/doc.html）

执行【启动MySQL监听】 入参为（依据自身调整）：

```
{
"driverClassName": "com.mysql.jdbc.Driver",
"host": "127.0.0.1",
"jdbcName": "test1",
"password": "xtt@123456",
"port": 13308,
"serverId": 2,
"url": "jdbc:mysql://127.0.0.1:13308/",
"username": "root"
}
```

## 3、打开日志输出（http://127.0.0.1:19911/）

![Image](https://github.com/dzw1113/binlog/blob/master/log.png?raw=true)

## 4、在mysql客户端工具里执行脚本

```
CREATE DATABASE `test111` CHARACTER SET 'utf8' COLLATE 'utf8_bin';

use test111;

create table test(
id int
);

insert test select 2;

alter table test add column first_name varchar(20);

insert test VALUES(1,'2'),(1,3);

update test set first_name = '再说2' where id =2;

DELETE FROM TEST WHERE ID = 1;
```

再切换网页查看日志。

## 常用指令

#### 重置binlog

RESET MASTER;

#### 刷新日志

FLUSH LOGS;

#### 查看master的状态

show master status;

#### 查看binlog文件

show binary logs;

#### 从指定的文件看binlog

show binlog events in 'mysql-bin.000001';

#### 从指定的文件看binlog带分页

show binlog events in 'mysql-bin.000001' FROM 28375689 LIMIT 10;

#### row模式是否开启queryEvent注释信息

set binlog_rows_query_log_events=1;

#### binlog格式

show variables like '%binlog_format%';

#### innodb引擎状态

show engine innodb status;

#### 授权用户有模拟slave权限

CREATE USER canal IDENTIFIED BY 'dzw';  <br>
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'dzw'@'%';<br>
-- GRANT ALL PRIVILEGES ON *.* TO 'dzw'@'%' ;<br>
FLUSH PRIVILEGES;<br>


参考 <br/>
https://blog.csdn.net/qq_32352565/article/details/77506618 <br/>
https://www.cnblogs.com/codingLiu/p/12725789.html <br/>
http://blog.itpub.net/20892230/viewspace-2129567/  <br/>
https://www.cnblogs.com/mysql-dba/tag/mysql/ <br/>
https://www.cnblogs.com/kevingrace/p/5569753.html <br/>