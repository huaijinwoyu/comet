
package cn.ltang.meeting.comet;

/**
 * 推送消息控制类
 * @author cc
 */
public class MessageManager {
	public static MessageSender messageSender;
	
	static {
		messageSender = new MessageSender();
		new Thread(messageSender).start();
	}
}
