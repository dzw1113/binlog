package io.github.dzw1113.binlog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * @description: 开启WebSocket
 * @author: dzw
 * @date: 2019/08/19 11:50
 **/
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * 配置信息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 订阅Broker名称,接受消息用户必须以这个开头的路径才能收到消息
        config.enableSimpleBroker("/user", "/topic");
        // 点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * 注册stomp的端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 允许使用socketJs方式访问，访问点为xttWs，允许跨域
        registry.addEndpoint("/binlogWs").setAllowedOriginPatterns("*") // 允许跨域设置
                .withSockJS().setClientLibraryUrl("/sockjs.min.js");
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 消息字节数
        registry.setMessageSizeLimit(500 * 1024 * 1024);
        // 设置消息缓存大小
        registry.setSendBufferSizeLimit(1024 * 1024 * 1024);
        // 设置消息发送时间限制毫秒
        registry.setSendTimeLimit(10000);
    }
    
}
