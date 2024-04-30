package com.weTalk.entity.query;

import java.util.Date;


/**
 * 联系人参数
 */
public class UserContactQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 联系人ID或群组ID
	 */
	private String contactId;

	private String contactIdFuzzy;

	/**
	 * 联系人类型 0:好友 1:群组
	 */
	private Integer contactType;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑
	 */
	private Integer status;

	/**
	 * 最后更新时间
	 */
	private String lastUpdateTime;

	private String lastUpdateTimeStart;

	private String lastUpdateTimeEnd;

	/**
	 * 是否要进行user_contact表和user_info表的链接查询
	 * 查询群员的信息时就能获取好友昵称
	 * 该属性不在数据库表设计里
	 *
	 * 注意！注意！！注意！！！
	 * 在命名属性时最好不要以is开头
	 */
	private Boolean queryUserInfo;

	/**
	 * 是否要进行user_contact表和group_info表的链接查询
	 * 查询群组的信息时就能获取群组的名称
	 * 该属性不在数据库表设计里
	 */
	private Boolean queryGroupInfo;

	/**
	 * 是否要进行user_contact表和user_info表的链接查询
	 * 查询好友的信息，获取好友列表时时就能获取好友昵称
	 * 该属性不在数据库表设计里
	 */
	private Boolean queryContactUserInfo;

	/**
	 * 获取群组列表时是否要过滤自己创建的群组
	 */
	private Boolean queryExcludeMyGroup;

	/**
	 * 获取好友列表时要根据好友状态筛选掉一些不应该出现的好友
	 */
	private Integer[] queryStatusArray;


	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
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

	public void setContactType(Integer contactType){
		this.contactType = contactType;
	}

	public Integer getContactType(){
		return this.contactType;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setLastUpdateTime(String lastUpdateTime){
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getLastUpdateTime(){
		return this.lastUpdateTime;
	}

	public void setLastUpdateTimeStart(String lastUpdateTimeStart){
		this.lastUpdateTimeStart = lastUpdateTimeStart;
	}

	public String getLastUpdateTimeStart(){
		return this.lastUpdateTimeStart;
	}
	public void setLastUpdateTimeEnd(String lastUpdateTimeEnd){
		this.lastUpdateTimeEnd = lastUpdateTimeEnd;
	}

	public String getLastUpdateTimeEnd(){
		return this.lastUpdateTimeEnd;
	}

	public Boolean getQueryUserInfo() {
		return queryUserInfo;
	}

	public void setQueryUserInfo(Boolean queryUserInfo) {
		this.queryUserInfo = queryUserInfo;
	}

	public Boolean getQueryGroupInfo() {
		return queryGroupInfo;
	}

	public void setQueryGroupInfo(Boolean queryGroupInfo) {
		this.queryGroupInfo = queryGroupInfo;
	}

	public Boolean getQueryContactUserInfo() {
		return queryContactUserInfo;
	}

	public void setQueryContactUserInfo(Boolean queryContactUserInfo) {
		this.queryContactUserInfo = queryContactUserInfo;
	}

	public Boolean getQueryExcludeMyGroup() {
		return queryExcludeMyGroup;
	}

	public void setQueryExcludeMyGroup(Boolean queryExcludeMyGroup) {
		this.queryExcludeMyGroup = queryExcludeMyGroup;
	}

	public Integer[] getQueryStatusArray() {
		return queryStatusArray;
	}

	public void setQueryStatusArray(Integer[] queryStatusArray) {
		this.queryStatusArray = queryStatusArray;
	}
}
