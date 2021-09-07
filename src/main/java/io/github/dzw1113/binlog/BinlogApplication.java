package io.github.dzw1113.binlog;

import java.io.File;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.github.shyiko.mysql.binlog.BinaryLogFileReader;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableKnife4j
public class BinlogApplication {
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(BinlogApplication.class, args);
    }
    
    public static void readFile(String binlogFilePath) throws IOException {
        File binlogFile = new File(binlogFilePath);
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(
                EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG,
                EventDeserializer.CompatibilityMode.CHAR_AND_BINARY_AS_BYTE_ARRAY
        );
        BinaryLogFileReader reader = new BinaryLogFileReader(binlogFile, eventDeserializer);
        try {
            for (Event event; (event = reader.readEvent()) != null; ) {
                System.out.println("header:" + event.getHeader());
                EventData data = event.getData();
                if (data == null) {
                    System.out.println("空的event");
                    return;
                }
                if (data instanceof TableMapEventData) {
                    System.out.println("Table:");
                    TableMapEventData tableMapEventData = (TableMapEventData) data;
                    System.out.println(tableMapEventData.getTableId() + ": [" + tableMapEventData.getDatabase() + "-" + tableMapEventData.getTable() + "]");
                }
                if (data instanceof UpdateRowsEventData) {
                    System.out.println("Update:");
                    System.out.println(data.toString());
                } else if (data instanceof WriteRowsEventData) {
                    System.out.println("Insert:");
                    System.out.println(data.toString());
                } else if (data instanceof DeleteRowsEventData) {
                    System.out.println("Delete:");
                    System.out.println(data.toString());
                } else if (data instanceof QueryEventData) {
                    System.out.println("Query:");
                    System.out.println(data.toString());
                } else {
                    System.out.println(data.getClass());
                    System.out.println(data.toString());
                }
            }
        } finally {
            reader.close();
        }
    }
    
    
    @Bean
    public Docket createRestApi() {
        // 配置OAS 3.0协议
        // 查找有@Tag注解的类，并生成一个对应的分组；类下面的所有http请求方法，都会生成对应的API接口
        // 通过这个配置，就可以将那些没有添加@Tag注解的控制器类排除掉
        return new Docket(DocumentationType.OAS_30).useDefaultResponseMessages(false).apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build().apiInfo(apiInfo()).enable(true);
    }
    
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("binlog")
                .description("649095437@qq.com。")
                .contact(new Contact("dzw。", "", ""))
                .version("1.0")
                .build();
    }
    
}
