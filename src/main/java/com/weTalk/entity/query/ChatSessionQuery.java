package com.weTalk.entity.query;



/**
 * 会话信息表参数
 */
public class ChatSessionQuery extends BaseParam {


	/**
	 * 会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 最后接收的消息
	 */
	private String lastMessage;

	private String lastMessageFuzzy;

	/**
	 * 最后接收消息时间毫秒
	 */
	private Long lastReceiveTime;


	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy){
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy(){
		return this.sessionIdFuzzy;
	}

	public void setlastMessage(String lastMessage){
		this.lastMessage = lastMessage;
	}

	public String getlastMessage(){
		return this.lastMessage;
	}

	public void setlastMessageFuzzy(String lastMessageFuzzy){
		this.lastMessageFuzzy = lastMessageFuzzy;
	}

	public String getlastMessageFuzzy(){
		return this.lastMessageFuzzy;
	}

	public void setLastReceiveTime(Long lastReceiveTime){
		this.lastReceiveTime = lastReceiveTime;
	}

	public Long getLastReceiveTime(){
		return this.lastReceiveTime;
	}

}
