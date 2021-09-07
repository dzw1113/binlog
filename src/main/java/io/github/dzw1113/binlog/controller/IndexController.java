package io.github.dzw1113.binlog.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;
import com.github.shyiko.mysql.binlog.event.deserialization.QueryEventDataDeserializer;

import io.github.dzw1113.binlog.event.deserializer.SQLQueryEventDataDeserializer;
import io.github.dzw1113.binlog.jdbc.JdbcUtil;
import io.github.dzw1113.binlog.model.HttpResult;
import io.github.dzw1113.binlog.model.JdbcVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: index
 * @author: dzw
 * @date: 2021/09/03 15:55
 **/
@Controller
@Api(tags = "index")
@Slf4j
public class IndexController {
    
    private static final ConcurrentHashMap<String, JdbcVO> concurrentHashMap = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, BinaryLogClient> concurrentHashMap1 = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, Future> concurrentHashMap2 = new ConcurrentHashMap();
    private static BlockingQueue linkedBlockingQueue = new LinkedBlockingQueue<Runnable>();
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10,
            60L, TimeUnit.SECONDS,
            linkedBlockingQueue,new ThreadPoolExecutor.CallerRunsPolicy());
    @Autowired
    SimpMessagingTemplate messageTemplate;
    
    public static void main(String[] args) throws InterruptedException {
        List<Future> list = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            int finalI = i;
            FutureTask future = (FutureTask) threadPoolExecutor.submit(()->{
                System.out.println(new Date() + "任务开始" + finalI);
                try {
                    Thread.sleep((15 + 5) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    
                }
                System.out.println(new Date() + "任务结束" + finalI);
            });
            list.add(future);
        }
        Thread.sleep(5 * 1000);
        for (Future future : list) {
            System.out.println("强行中断任务" );
            future.cancel(true);
            System.out.println(future.isDone());
            System.out.println(threadPoolExecutor.getTaskCount());
            System.out.println(threadPoolExecutor.getActiveCount());
        }
        threadPoolExecutor.awaitTermination(100, TimeUnit.SECONDS);
//        int[] arr = {5, 4, 1, 2, 7, 5, 9, 6};
//        int[] retArr = maxSlidingWindow(arr, 2);
//        System.out.println(retArr);
    }
    
    public static int[] maxSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length == 0)
            return new int[0];
        int[] result = new int[nums.length - k + 1];
        LinkedList<Integer> deque = new LinkedList<Integer>();
        for (int i = 0; i < nums.length; i++) {
            if (!deque.isEmpty() && deque.peekFirst() == i - k)
                deque.poll();
            while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
                deque.removeLast();
            }
            deque.offer(i);
            if (i + 1 >= k)
                result[i + 1 - k] = nums[deque.peek()];
        }
        return result;
    }
    
    /**
     * @param
     * @return java.lang.String
     * @description 首页
     * @author dzw
     * @date 2020/7/3 11:21
     **/
    @RequestMapping(path = {"/", "/index"})
    public String index() {
        return "index";
    }
    
    /**
     * @param jdbcVO
     * @return com.xtt.selfhelp.model.HttpResult
     * @description 启动Com监听端口
     * @author dzw
     * @date 2021/5/11 13:11
     **/
    @ResponseBody
    @RequestMapping("启动MySQL监听")
    @ApiOperation(httpMethod = "POST", value = "启动MySQL监听", produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult startListnerMySQL(@ApiParam(value = "jdbcVO", name = "jdbcVO", required = true)
                                        @RequestBody JdbcVO jdbcVO) {
        if (concurrentHashMap.get(jdbcVO.getJdbcName()) == null) {
            BinaryLogClient client = new BinaryLogClient(jdbcVO.getHost(), jdbcVO.getPort(), jdbcVO.getUsername(), jdbcVO.getPassword());
            EventDeserializer eventDeserializer = new EventDeserializer();
            eventDeserializer.setEventDataDeserializer(EventType.QUERY,
                    new SQLQueryEventDataDeserializer());
            client.setEventDeserializer(eventDeserializer);
            client.setServerId(jdbcVO.getServerId());
            Future future = null;
            if("ROW".equals(JdbcUtil.showBinlogFormat(jdbcVO))){
                future = threadPoolExecutor.submit(new RowBinlogTask(client, jdbcVO,messageTemplate));
            }else if("STATEMENT".equals(JdbcUtil.showBinlogFormat(jdbcVO))){
                future = threadPoolExecutor.submit(new StatementBinlogTask(client, jdbcVO,messageTemplate));
            }else if("MIXED".equals(JdbcUtil.showBinlogFormat(jdbcVO))){
                future = threadPoolExecutor.submit(new StatementBinlogTask(client, jdbcVO,messageTemplate));
            }
            concurrentHashMap2.put(jdbcVO.getJdbcName(), future);
            concurrentHashMap.put(jdbcVO.getJdbcName(), jdbcVO);
            concurrentHashMap1.put(jdbcVO.getJdbcName(), client);
        }
        return HttpResult.ok();
    }
    
    @ResponseBody
    @RequestMapping("停止MySQL监听")
    @ApiOperation(httpMethod = "POST", value = "停止MySQL监听", produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult stopListnerMySQL(@ApiParam(value = "jdbcVO", name = "jdbcVO", required = true)
                                       @RequestBody JdbcVO jdbcVO) {
        if (concurrentHashMap1.get(jdbcVO.getJdbcName()) != null) {
            try {
                concurrentHashMap1.get(jdbcVO.getJdbcName()).disconnect();
                concurrentHashMap2.get(jdbcVO.getJdbcName()).cancel(true);
                concurrentHashMap.remove(jdbcVO.getJdbcName());
                concurrentHashMap1.remove(jdbcVO.getJdbcName());
                concurrentHashMap2.remove(jdbcVO.getJdbcName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return HttpResult.ok();
    }
    
    @ResponseBody
    @RequestMapping("获取数据库的binlog信息")
    @ApiOperation(httpMethod = "POST", value = "获取数据库的binlog信息", produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpResult listBinlog(@ApiParam(value = "jdbcVO", name = "jdbcVO", required = true)
                                 @RequestBody JdbcVO jdbcVO) {
        return HttpResult.ok(JdbcUtil.showBinlogFormat(jdbcVO));
    }
    
}
