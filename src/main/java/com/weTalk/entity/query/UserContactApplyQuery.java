package com.weTalk.entity.query;



/**
 * 联系人申请参数
 */
public class UserContactApplyQuery extends BaseParam {


	/**
	 * 自增ID
	 */
	private Integer applyId;

	/**
	 * 申请人ID
	 */
	private String applyUserId;

	private String applyUserIdFuzzy;

	/**
	 * 接收人ID
	 */
	private String receiveUserId;

	private String receiveUserIdFuzzy;

	/**
	 * 联系人类型 0:好友 1:群组(加好友申请还是加群申请)
	 */
	private Integer contactType;

	/**
	 * 联系人群组ID
	 */
	private String contactId;

	private String contactIdFuzzy;

	/**
	 * 最后申请时间
	 */
	private Long lastApplyTime;

	/**
	 * 状态 0:待处理 1:已同意 2:已拒绝 3:已被拉
	 */
	private Integer status;

	/**
	 * 申请信息
	 */
	private String applyInfo;

	private String applyInfoFuzzy;

	/**
	 * 是否要进行user_contact_apply表和user_info表(或group_info表)的链接查询
	 * 以获取申请人的nick_name(或群组的group_name)
	 * 该属性不在数据库表设计里
	 *
	 * 注意！注意！！注意！！！
	 * 在命名属性时最好不要以is开头
	 * 例如本属性若命名为isQueryContactInfo则myBatis会发生NoSuchPropertyException异常
	 *
	 */
	private Boolean queryContactInfo;

	private Long lastApplyTimestamp;

	public void setApplyId(Integer applyId){
		this.applyId = applyId;
	}

	public Integer getApplyId(){
		return this.applyId;
	}

	public void setApplyUserId(String applyUserId){
		this.applyUserId = applyUserId;
	}

	public String getApplyUserId(){
		return this.applyUserId;
	}

	public void setApplyUserIdFuzzy(String applyUserIdFuzzy){
		this.applyUserIdFuzzy = applyUserIdFuzzy;
	}

	public String getApplyUserIdFuzzy(){
		return this.applyUserIdFuzzy;
	}

	public void setReceiveUserId(String receiveUserId){
		this.receiveUserId = receiveUserId;
	}

	public String getReceiveUserId(){
		return this.receiveUserId;
	}

	public void setReceiveUserIdFuzzy(String receiveUserIdFuzzy){
		this.receiveUserIdFuzzy = receiveUserIdFuzzy;
	}

	public String getReceiveUserIdFuzzy(){
		return this.receiveUserIdFuzzy;
	}

	public void setContactType(Integer contactType){
		this.contactType = contactType;
	}

	public Integer getContactType(){
		return this.contactType;
	}

	public void setContactId(String contactId){
		this.contactId = contactId;
	}

	public String getContactId(){
		return this.contactId;
	}

	public void setContactIdFuzzy(String contactIdFuzzy){
		this.contactIdFuzzy = contactIdFuzzy;
	}

	public String getContactIdFuzzy(){
		return this.contactIdFuzzy;
	}

	public void setLastApplyTime(Long lastApplyTime){
		this.lastApplyTime = lastApplyTime;
	}

	public Long getLastApplyTime(){
		return this.lastApplyTime;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setApplyInfo(String applyInfo){
		this.applyInfo = applyInfo;
	}

	public String getApplyInfo(){
		return this.applyInfo;
	}

	public void setApplyInfoFuzzy(String applyInfoFuzzy){
		this.applyInfoFuzzy = applyInfoFuzzy;
	}

	public String getApplyInfoFuzzy(){
		return this.applyInfoFuzzy;
	}

	public Boolean getQueryContactInfo() {
		return queryContactInfo;
	}

	public void setQueryContactInfo(Boolean queryContactInfo) {
		this.queryContactInfo = queryContactInfo;
	}

	public Long getLastApplyTimestamp() {
		return lastApplyTimestamp;
	}

	public void setLastApplyTimestamp(Long lastApplyTimestamp) {
		this.lastApplyTimestamp = lastApplyTimestamp;
	}
}
