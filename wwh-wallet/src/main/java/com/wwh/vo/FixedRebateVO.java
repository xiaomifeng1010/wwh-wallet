package com.wwh.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 定返功能Vo
 * @ClassName: FixedRebateVo 
 * @Description: TODO
 * @author: ranle
 * @date: 2017年3月7日 下午4:15:35
 */
public class FixedRebateVO extends BaseVO {

	/**
	 * @fieldName: serialVersionUID
	 * @fieldType: long
	 * @Description: TODO
	 */
	private static final long serialVersionUID = 1817749150231313922L;
	/**
	 * 身份标识（统计）
	 */
	private String idCard;
	/**
	 * 充值金额（统计）
	 */
	private BigDecimal payAmount=new BigDecimal(0);
	/**
	 * 定返总金额（统计）
	 */
	private BigDecimal totalAmount=new BigDecimal(0);
	/**
	 * 已返总金额（统计）
	 */
	private BigDecimal returnedAmount=new BigDecimal(0);
	/**
	 * 定返状态（统计，WAIT:等待,RUNNING:运行,FINISHED:完成）
	 */
	private String rebateStatus;
	/**
	 * 收益金额（定返详情）
	 */
	private BigDecimal profitAmount=new BigDecimal(0);
	/**
	 * 收益类型（定返详情 DF:定返,PW:盘位,QD:抢点,FH:招商分红,TJ:推荐点,DL:推荐代理商）
	 */
	private String  profitType;
	/**
	 * 系统类型（统计&定返详情 OTHER:未入系统,TIYAN:体验,HUIMIN:惠民,FUMIN:富民,XINGMIN:兴民）
	 */
	private String  sysType;
	/**
	 * 定返比例（统计&配置）
	 */
	private BigDecimal fixedRate;
	/**
	 * 定返比例状态（配置）
	 */
	private String exeStatus;
	
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public BigDecimal getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(BigDecimal payAmount) {
		this.payAmount = payAmount;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	public BigDecimal getReturnedAmount() {
		return returnedAmount;
	}
	public void setReturnedAmount(BigDecimal returnedAmount) {
		this.returnedAmount = returnedAmount;
	}
	public String getRebateStauts() {
		return rebateStatus;
	}
	public void setRebateStauts(String rebateStauts) {
		this.rebateStatus = rebateStauts;
	}
	public BigDecimal getProfitAmount() {
		return profitAmount;
	}
	public void setProfitAmount(BigDecimal profitAmount) {
		this.profitAmount = profitAmount;
	}
	public String getProfitType() {
		return profitType;
	}
	public void setProfitType(String profitType) {
		this.profitType = profitType;
	}
	public String getSysType() {
		return sysType;
	}
	public void setSysType(String sysType) {
		this.sysType = sysType;
	}
	public BigDecimal getFixedRate() {
		return fixedRate;
	}
	public void setFixedRate(BigDecimal fixedRate) {
		this.fixedRate = fixedRate;
	}
	public String getExeStatus() {
		return exeStatus;
	}
	public void setExeStatus(String exeStatus) {
		this.exeStatus = exeStatus;
	}
	
}
