package io.github.dzw1113.binlog.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: jdbc链接
 * @author: dzw
 * @date: 2021/09/03 15:59
 **/
@ApiModel(description = "jdbc链接")
@Data
public class JdbcVO {
    
    @ApiModelProperty(name = "jdbcName", value = "test1")
    String jdbcName;
    
    @ApiModelProperty(name = "host", value = "127.0.0.1")
    String host;
    @ApiModelProperty(name = "port", value = "3306")
    Integer port;
    @ApiModelProperty(name = "driverClassName", value = "com.mysql.jdbc.Driver")
    private String driverClassName;
    @ApiModelProperty(name = "url", value = "jdbc:mysql://127.0.0.1:3306/")
    private String url;
    @ApiModelProperty(name = "username", value = "root")
    private String username;
    
    @ApiModelProperty(name = "password", value = "123456")
    private String password;
    
    @ApiModelProperty(name = "serverId", value = "2")
    private Long serverId;
    
}
