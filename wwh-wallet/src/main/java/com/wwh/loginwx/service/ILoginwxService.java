package com.wwh.loginwx.service;

import com.wwh.common.ResultMsg;
import com.wwh.loginwx.vo.WXUserInfoVO;
import com.wwh.loginwx.vo.WeChatQRCodeVO;
import com.wwh.vo.UserVO;

import net.sf.json.JSONObject;

public interface ILoginwxService {
	/**
	 * 新增微信用户信息
	 * 
	 * @param wxUserInfoVO
	 * @return
	 */
	public Long insertWXUserInfo(WXUserInfoVO wxUserInfoVO);

	/**
	 * 通过userId查询微信用户信息
	 * 
	 * @param wxUserInfoVO
	 * @return
	 */
	public WXUserInfoVO getWXUserInfoByUserId(Long userId);

	/**
	 * 通过微信唯一标示查询微信用户信息
	 * 
	 * @param wxUserInfoVO
	 * @return
	 */
	public WXUserInfoVO getWXUserInfoByUnionId(String unionId);

	/**
	 * 注册
	 */
	public void registerMember(WXUserInfoVO wxUserInfoVO);

	/**
	 * 绑定微信用户手机号
	 */
	public ResultMsg<Object> bindWXUser(WXUserInfoVO wxUserInfoVO,String isTrue);

	/**
	 * 得到用户信息
	 */
	public UserVO getUserByUserId(Long userId);
	/**
	 * 更新微信表中的用户信息
	 * @param userId
	 * @param unionId
	 */
	public void updateWXUserInfo(WXUserInfoVO wxUserInfoVO); 
	/**
	 * 得到有效的公共接口使用token凭证（两个小时有效期）
	 */
	public String getValidTokenByUnionId(String unionId);
	/**
	 * 得到用户有效的媒体id(媒体id有效3天)
	 */
	public String getValidMediaIdByUnionId(String unionId);
	/**
	 * 获取生成的二维码链接
	 * @param ticket
	 * @return
	 */
	public String chageQr(String ticket);
	/**
	 * 生成二维码
	 * @param accessToken
	 * @param expireSeconds
	 * @param sceneId
	 * @return
	 */
	public WeChatQRCodeVO createTemporaryORCode(String accessToken,String expireSeconds ,Long sceneId);
	/**
	 * 上传图片素材
	 * @param accessToken
	 * @param imgurl
	 * @param headUrl
	 * @param openId
	 * @return
	 */
	public void uploadImg(String accessToken, String imgurl,WXUserInfoVO wxUserInfo);
	/**
	 * 验证手机号是否存在
	 * 
	 * @param phone
	 * @return
	 */
	public ResultMsg<Object> validPhone(String phone);
}
