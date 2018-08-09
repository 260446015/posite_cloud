package com.zkjl.posite_cloud.exception;

/**
 * 自定义异常基类
 * 
 * @author Jason.zhang
 * @mobile 18282600855
 * @since 2017/10/10
 *
 */
public abstract class BaseException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5463943846949917117L;
	
	public abstract int getCode();
	
}
