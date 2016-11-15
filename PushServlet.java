package cn.ltang.meeting.comet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.CometEvent;
import org.apache.catalina.CometProcessor;


/**
 * 推送消息服务
 * 
 */
public class PushServlet extends HttpServlet implements CometProcessor {

	private static final long serialVersionUID = -186432007155400490L;

	/* 超时时间必须大于js ajax请求的超时时间 */
	private static final Integer TIMEOUT = 10 * 61 * 1000;
	
	public void destroy() {

	}

	public void init() throws ServletException {
	}

	/**
	 * 事件处理方法
	 */
	public void event(CometEvent event) throws IOException, ServletException {
		HttpServletResponse response = event.getHttpServletResponse();
		HttpServletRequest request = event.getHttpServletRequest();
		String loginId = "sysadmin";
		HttpSession session = request.getSession();
		String sessionId = session.getId()+"_"+response.toString()+"_"+loginId;
		
		if(loginId!=null){
			if (event.getEventType() == CometEvent.EventType.BEGIN) {
				// Http连接空闲超时
				event.setTimeout(TIMEOUT);
				MessageManager.messageSender.addConnection(response, sessionId,loginId);

			} else if (event.getEventType() == CometEvent.EventType.ERROR) {
				MessageManager.messageSender.removeConnection(response, sessionId,loginId);
				event.close();
			} else if (event.getEventType() == CometEvent.EventType.END) {
				MessageManager.messageSender.removeConnection(response, sessionId,loginId);
				event.close();
			} else if (event.getEventType() == CometEvent.EventType.READ) {

			}
		}
	}
}