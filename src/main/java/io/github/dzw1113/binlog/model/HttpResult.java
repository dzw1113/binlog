package io.github.dzw1113.binlog.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 公共接口返回值
 *
 * @author eshengtai
 * @date 2018/08/06
 */
@ApiModel(description = "公共返回")
public class HttpResult<T> {
    
    /**
     * 失败
     */
    public static final String FAILURE = "0";
    /**
     * 成功
     */
    public static final String SUCCESS = "1";
    /**
     * 警告
     */
    public static final String WARNING = "2";
    /**
     * 无效的token
     */
    public static final String INVALIDATE_TOKEN = "3";
    /**
     * 重复
     */
    public static final String REPETITION = "4";
    /**
     * 返回状态
     */
    @ApiModelProperty(value = "返回状态")
    private String status = SUCCESS;
    /**
     * 返回结果
     */
    @ApiModelProperty(value = "返回结果")
    private T rs;
    /**
     * 错误代码(移动端用)
     */
    @ApiModelProperty(value = "错误代码")
    private String errcode;
    /**
     * 错误消息
     */
    @ApiModelProperty(value = "错误消息")
    private String errmsg;
    
    private HttpResult() {
    
    }
    
    public HttpResult(T t) {
        this.setRs(t);
    }
    
    public HttpResult(String status, String errcode, String errmsg) {
        this.status = status;
        this.errcode = errcode;
        this.errmsg = errmsg;
    }
    
    public HttpResult(String status, String errmsg, T t) {
        this.status = status;
        this.errmsg = errmsg;
        this.rs = t;
    }
    
    public HttpResult(String status, String errmsg) {
        this.status = status;
        this.errmsg = errmsg;
    }
    
    public static <T> HttpResult<T> ok(T t) {
        return new HttpResult<T>(HttpResult.SUCCESS, "操作成功", t);
    }
    
    public static <T> HttpResult<T> ok() {
        return new HttpResult<T>(HttpResult.SUCCESS, "操作成功");
    }
    
    public static <T> HttpResult<T> warn(String errmsg) {
        return new HttpResult<T>(HttpResult.WARNING, errmsg);
    }
    
    public static <T> HttpResult<T> warn(String errcode, String errmsg) {
        return new HttpResult<T>(HttpResult.WARNING, errcode, errmsg);
    }
    
    public static <T> HttpResult<T> warn(String errmsg, T t) {
        return new HttpResult<T>(HttpResult.WARNING, errmsg, t);
    }
    
    public static <T> HttpResult<T> error(T t) {
        return new HttpResult<T>(HttpResult.FAILURE, "内部异常", t);
    }
    
    public static <T> HttpResult<T> error(String errmsg) {
        return new HttpResult<T>(HttpResult.FAILURE, null, errmsg);
    }
    
    public static <T> HttpResult<T> error(String errcode, String errmsg) {
        return new HttpResult<T>(HttpResult.FAILURE, errcode, errmsg);
    }
    
    public static <T> HttpResult<T> error() {
        return new HttpResult<T>(HttpResult.FAILURE, "内部异常");
    }
    
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * @return the errmsg
     */
    public String getErrmsg() {
        return errmsg;
    }
    
    /**
     * @param errmsg the errmsg to set
     */
    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    
    /**
     * @return the rs
     */
    public Object getRs() {
        return rs;
    }
    
    public void setRs(T rs) {
        this.rs = rs;
    }
    
    /**
     * @return the errcode
     */
    public String getErrcode() {
        return errcode;
    }
    
    /**
     * @param errcode the errcode to set
     */
    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }
    
}
