
package cn.ltang.meeting.comet;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletResponse;

/**
 * 存放请求
 * 
 * @author cc
 */
public class ResponseInfo {

	private String loginId;
	/* http返回 */
	private ServletResponse response;
	/* 请求返回时间 */
	private Date leaveTime;
	/* 是否可用 */
	private boolean isActive;
	/*需要发送的信息*/
	private List<Message> messages;

	public ServletResponse getResponse() {
		return response;
	}

	public void setResponse(ServletResponse response) {
		this.response = response;
	}

	public Date getLeaveTime() {
		return leaveTime;
	}

	public void setLeaveTime(Date leaveTime) {
		this.leaveTime = leaveTime;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	
}
