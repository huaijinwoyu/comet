
package cn.ltang.meeting.comet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.ltang.meeting.util.DateUtil;


/**
 * 推送消失主要实现类
 * 
 * @author cc
 */
public class MessageSender implements Runnable {

	private Logger log = Logger.getLogger(MessageSender.class);

	private int TIME_OUT = 20 * 1000;

	protected boolean running = true;
	/**
	 * <sessionId ResponseInfo>
	 */
	protected Map<String, ResponseInfo> connectionMap = new HashMap<String, ResponseInfo>();
	/**
	 * <loginId sessionIdList>
	 */
	protected Map<String, List<String>> usersMap = new HashMap<String, List<String>>();

	private boolean team = false;

	public synchronized void addConnection(ServletResponse connection,
			String sessionId, String loginId) {
		if (log.isDebugEnabled()) {
			log.debug("User [ " + loginId + " ] ready to regist comet message. Session ID is [ " + sessionId + " ].");
		}
		
		if (null != loginId && null != connection && null != sessionId) {
			synchronized (connectionMap) {
				log.debug("Ready to add/update to connectionMap<sessionId, ResponseInfo>");
				
				ResponseInfo responseInfo = connectionMap.get(sessionId);
				if (null == responseInfo) {
					log.debug("Response Info doesn't exist. Add it.");
					
					ResponseInfo infoTemp = new ResponseInfo();
					infoTemp.setResponse(connection);
					infoTemp.setActive(true);
					infoTemp.setLoginId(loginId);
					connectionMap.put(sessionId, infoTemp);
				} else {
					log.debug("Response Info exist. Update it. \nLeave Time: " 
							+ responseInfo.getLeaveTime());
					
					responseInfo.setResponse(connection);
					responseInfo.setActive(true);
				}
			}
			synchronized (usersMap) {
				log.debug("Ready to add/update to usersMap<loginId, sessionIdList>");
				
				List<String> sessionIdList = usersMap.get(loginId);
				if (null == sessionIdList) {
					log.debug("sessionIdList is empty. new it and add current sessionId to the list.");
					
					List<String> sessionIdListTemp = new ArrayList<String>();
					sessionIdListTemp.add(sessionId);
					usersMap.put(loginId, sessionIdListTemp);
				} else {
					log.debug("sessionIdList is not empty.");
					boolean flag = true;
					for (String id : sessionIdList) {
						if (id.equals(sessionId)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						log.debug("sessionId is not in the sessionIdList. add it to the list");
						sessionIdList.add(sessionId);
					} else {
						log.debug("sessionId already in the sessionIdList. do nothing.");
					}
				}
				usersMap.notify();
			}
		}
	}

	public synchronized void removeConnection(ServletResponse connection,
			String sessionId, String loginId) {
		if (log.isDebugEnabled()) {
			log.debug("User [ " + loginId + " ] ready to unregist. Session ID is [ " + sessionId + " ].");
		}
		
		if (null != connection && null != loginId && null != sessionId) {
			synchronized (connectionMap) {
				ResponseInfo infoTemp = connectionMap.get(sessionId);
				if (null != infoTemp) {
					if (connection.equals(infoTemp.getResponse())) {
						log.debug("clear response and set the leave time.");
						infoTemp.setResponse(null);
						infoTemp.setLeaveTime(DateUtil.getDate());
						infoTemp.setActive(false);
					}
				}
			}
		}
	}

	public void send(Message message, List<String> loginIds) {
		
		if (null != loginIds && !loginIds.isEmpty()) {
			List<String> sessionList = new ArrayList<String>();
			synchronized (usersMap) {
				log.debug("find sessionIds by loginIds");
				for (String loginId : loginIds) {
					List<String> sessionIds = usersMap.get(loginId);
					if (null != sessionIds && !sessionIds.isEmpty()) {
						for (String id : sessionIds) {
							sessionList.add(id);
						}
					}
				}
			}
			
			synchronized (connectionMap) {
				log.debug("put message to every sessionId");
				for (String id : sessionList) {
					ResponseInfo responseInfo = connectionMap.get(id);
					if (null != responseInfo) {
						if (log.isDebugEnabled()) {
							log.debug("put message to loginId[" + responseInfo.getLoginId() +"]. sessionId[" + id +"]");
						}
						
						List<Message> messages = responseInfo.getMessages();
						if (null == messages) {
							List<Message> messagesTemp = new ArrayList<Message>();
							messagesTemp.add(message);
							responseInfo.setMessages(messagesTemp);
						} else {
							messages.add(message);
						}
					} else {
						log.debug("ResponseInfo is null. ignore it");
					}
				}
			}
			synchronized (usersMap) {
				usersMap.notify();
			}
		}
	}
	
	/*若：无发送者，则发送给所有连接着*/
	public void send(Message message) {
	
			List<String> sessionList = new ArrayList<String>();
			synchronized (usersMap) {
				log.debug("find sessionIds by loginIds");
				for (String loginId : usersMap.keySet()) {
					List<String> sessionIds = usersMap.get(loginId);
					if (null != sessionIds && !sessionIds.isEmpty()) {
						for (String id : sessionIds) {
							sessionList.add(id);
						}
					}
				}
			}
			
			synchronized (connectionMap) {
				log.debug("put message to every sessionId");
				for (String id : sessionList) {
					ResponseInfo responseInfo = connectionMap.get(id);
					if (null != responseInfo) {
						if (log.isDebugEnabled()) {
							log.debug("put message to loginId[" + responseInfo.getLoginId() +"]. sessionId[" + id +"]");
						}
						
						List<Message> messages = responseInfo.getMessages();
						if (null == messages) {
							List<Message> messagesTemp = new ArrayList<Message>();
							messagesTemp.add(message);
							responseInfo.setMessages(messagesTemp);
						} else {
							messages.add(message);
						}
					} else {
						log.debug("ResponseInfo is null. ignore it");
					}
				}
			}
			synchronized (usersMap) {
				usersMap.notify();
			}
	}
	

	public void stop() {
		log.error("message sender close");
		this.running = false;
	}

	public void run() {
		while (running) {

			if (team) {
				try {
					synchronized (usersMap) {
						usersMap.wait();
					}
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
			team = true;

			Map<String, List<String>> deleteUser = new HashMap<String, List<String>>();
			synchronized (connectionMap) {

				List<String> deleteList = new ArrayList<String>();
				Set<String> connIdSet = connectionMap.keySet();
				for (String id : connIdSet) {
					ResponseInfo conn = connectionMap.get(id);
					if (null == conn) {
						continue;
					}
					if (null == conn.getResponse()) {
						long leaveTime = DateUtil.add(conn.getLeaveTime(),
								Calendar.MILLISECOND, TIME_OUT).getTime();
						long time = DateUtil.getDate().getTime();
						if (leaveTime < time) {
							if (log.isDebugEnabled()) {
								log.debug("Session ID " + id + " is timeout. ready to clear");
							}
							
							deleteList.add(id);
							//超时的sessionId准备给用户表，清除多余sessionId
							List<String> sessionIds = deleteUser.get(conn
									.getLoginId());
							if (null == sessionIds) {
								List<String> temp = new ArrayList<String>();
								temp.add(id);
								deleteUser.put(conn.getLoginId(), temp);
							} else {
								sessionIds.add(id);
							}

						}
						continue;
					}
					if (null != conn.getMessages()
							&& !conn.getMessages().isEmpty() && conn.isActive()) {
						try {
							PrintWriter writer;
							writer = conn.getResponse().getWriter();
							Map<String, Object> result = new HashMap<String, Object>();
							result.put("list", conn.getMessages());
							JSONObject jsonObject = JSONObject
									.fromObject(result);
							writer.write(jsonObject.toString());
							
							if (log.isDebugEnabled()) {
								log.debug("Ready to send message to browser. data is: " + jsonObject.toString());
							}
							
							writer.flush();
							writer.close();
							conn.getMessages().clear();
							conn.setActive(false);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				}
				for (String id : deleteList) {
					connectionMap.remove(id);
				}
			}

			//删除过期的用户表中的值
			synchronized (usersMap) {
				Set<String> deleteSet = deleteUser.keySet();
				for (String loginId : deleteSet) {
					List<String> sessionIds = usersMap.get(loginId);
					if (null != sessionIds) {
						for (String deleteId : deleteUser.get(loginId)) {
							boolean flag = false;
							for (String id : sessionIds) {
								if (deleteId.equals(id)) {
									flag = true;
									break;
								}
							}
							if(flag){
								sessionIds.remove(deleteId);
								
								log.debug("User: " + deleteId + " is timeout. remove it from usersMap.");
							}
						}
						if (sessionIds.isEmpty()) {
							usersMap.remove(loginId);
						}
					}
				}
			}
		}
	}
}