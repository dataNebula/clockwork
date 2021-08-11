package com.creditease.adx.clockwork.common.exception;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:02 下午 2020/10/27
 * @ Description：任务分发异常
 * @ Modified By：
 */
public class TaskDistributeException  extends Exception{

    private static final long serialVersionUID = -3187516993124260048L;

    public TaskDistributeException(String message){
        super(message);
    }

    public TaskDistributeException(){
        super();
    }
}
