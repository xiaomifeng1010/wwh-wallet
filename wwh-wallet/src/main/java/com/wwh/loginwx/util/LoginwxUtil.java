package com.wwh.loginwx.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 微信登陆常量
 * 
 * @author wwh
 *
 */
public class LoginwxUtil {
	/**
	 * 公众号应用id
	 */
	public static final String appId = "wx6203d4f1069772fb";
	/**
	 * 公众号应用密匙
	 */
	public static final String secret = "9914372e71978e78fc8a03e9d91ce794";
	/**
	 * 登陆识别状态
	 */
	public static final String state = "loginwx";
	/**
	 * 授权类型(用户点击确认授权)
	 */
	public static final String scopeUserinfo = "snsapi_userinfo";
	/**
	 * 授权类型(用户无确认默认授权)
	 */
	public static final String scopeBase = "snsapi_base";
	/**
	 * shrio记住当前用户的登陆信息
	 */
	public static final String shrioLoginUrl = "/pages/wxlogin";
	/**
	 * shrio记住当前用户的假登陆信息
	 */
	public static final String shrioLoginsUrl = "/pages/wxlogins";
	/**
	 * 登陆首页
	 */
	public static final String loginURL = "http://mobile.vwhsc.com/src/html/login/loginwx.html";
	/**
	 * 回调请求路径
	 */
	public static final String redirectUrl = "http://mobile.vwhsc.com/pages/loginwx";
	/**
	 * 
	 */
	public static final String codeRedirectUrl = "http://mobile.vwhsc.com/pages/getTwoBarCode";

	/**
	 * 授权登陆请求url
	 */
	public static final String conBaseUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECTURL&response_type=code&scope=SCOPE&state=STATE#wechat_redirect";
	/**
	 * 授权凭证接口请求url
	 */
	public static final String tokenBaseUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	/**
	 * 授权凭证接口请求url
	 */
	public static final String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

	/**
	 * URL编码（utf-8）
	 * 
	 * @param source
	 * @return
	 */
	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
