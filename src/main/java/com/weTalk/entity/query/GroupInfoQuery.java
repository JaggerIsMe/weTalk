package com.weTalk.entity.query;

import java.util.Date;


/**
 * 群组信息参数
 */
public class GroupInfoQuery extends BaseParam {


	/**
	 * 群ID
	 */
	private String groupId;

	private String groupIdFuzzy;

	/**
	 * 群名
	 */
	private String groupName;

	private String groupNameFuzzy;

	/**
	 * 群主ID
	 */
	private String groupOwnerId;

	private String groupOwnerIdFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 群公告
	 */
	private String groupNotice;

	private String groupNoticeFuzzy;

	/**
	 * 0:直接加入 1:管理员同意后加入(默认1)
	 */
	private Integer joinType;

	/**
	 * 0:解散 1:正常(默认1)
	 */
	private Integer status;

	/**
	 * 是否要进行group_info表和user_info表的子查询
	 * 查询群组的信息时就能获取群主昵称
	 * 该属性不在数据库表设计里
	 */
	private Boolean queryGroupOwnerName;

	/**
	 * 是否要进行group_info表和contact_info表的子查询
	 * 查询群组的信息时就能获取群员的数量
	 * 该属性不在数据库表设计里
	 */
	private Boolean queryMemberCount;


	public void setGroupId(String groupId){
		this.groupId = groupId;
	}

	public String getGroupId(){
		return this.groupId;
	}

	public void setGroupIdFuzzy(String groupIdFuzzy){
		this.groupIdFuzzy = groupIdFuzzy;
	}

	public String getGroupIdFuzzy(){
		return this.groupIdFuzzy;
	}

	public void setGroupName(String groupName){
		this.groupName = groupName;
	}

	public String getGroupName(){
		return this.groupName;
	}

	public void setGroupNameFuzzy(String groupNameFuzzy){
		this.groupNameFuzzy = groupNameFuzzy;
	}

	public String getGroupNameFuzzy(){
		return this.groupNameFuzzy;
	}

	public void setGroupOwnerId(String groupOwnerId){
		this.groupOwnerId = groupOwnerId;
	}

	public String getGroupOwnerId(){
		return this.groupOwnerId;
	}

	public void setGroupOwnerIdFuzzy(String groupOwnerIdFuzzy){
		this.groupOwnerIdFuzzy = groupOwnerIdFuzzy;
	}

	public String getGroupOwnerIdFuzzy(){
		return this.groupOwnerIdFuzzy;
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

	public void setGroupNotice(String groupNotice){
		this.groupNotice = groupNotice;
	}

	public String getGroupNotice(){
		return this.groupNotice;
	}

	public void setGroupNoticeFuzzy(String groupNoticeFuzzy){
		this.groupNoticeFuzzy = groupNoticeFuzzy;
	}

	public String getGroupNoticeFuzzy(){
		return this.groupNoticeFuzzy;
	}

	public void setJoinType(Integer joinType){
		this.joinType = joinType;
	}

	public Integer getJoinType(){
		return this.joinType;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public Boolean getQueryGroupOwnerName() {
		return queryGroupOwnerName;
	}

	public void setQueryGroupOwnerName(Boolean queryGroupOwnerName) {
		this.queryGroupOwnerName = queryGroupOwnerName;
	}

	public Boolean getQueryMemberCount() {
		return queryMemberCount;
	}

	public void setQueryMemberCount(Boolean queryMemberCount) {
		this.queryMemberCount = queryMemberCount;
	}
}
