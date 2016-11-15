package cn.ltang.meeting.comet;

import java.util.Map;

/**
 * 推送消息存放类
 * @author cc
 */
public class Message {
	
	/*消息类型*/
	private String type;
	/*基本信息*/
	private String message;
	/*额外参数*/
	private Map<String, Object> param;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Object> getParam() {
		return param;
	}

	public void setParam(Map<String, Object> param) {
		this.param = param;
	}

}
