package com.zkjl.posite_cloud.exception;

public class CustomerException extends BaseException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7030730939804827069L;

	private int code;
	private String message;
	public CustomerException(int code){
		this.code=code;
	}
	
	@Override
	public int getCode() {
		return code;
	}

	public CustomerException(String message) {
		this.message = message;
	}

}
