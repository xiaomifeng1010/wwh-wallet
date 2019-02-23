package com.wwh.loginwx.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.wwh.common.ResultMsg;
import com.wwh.loginwx.config.WeixinConfig;
import com.wwh.loginwx.req.LoginwxRequest;
import com.wwh.loginwx.service.ILoginwxService;
import com.wwh.loginwx.util.LoginwxUtil;
import com.wwh.loginwx.util.WeiXinUtil;
import com.wwh.loginwx.vo.WXUserInfoVO;
import com.wwh.loginwx.vo.WXUserTokenVO;
import com.wwh.service.IMemberService;
import com.wwh.service.IUserService;
import com.wwh.util.CommonConstant;
import com.wwh.util.MD5Utils;
import com.wwh.util.ReturnConstant;
import com.wwh.util.StringUtils;
import com.wwh.vo.UserVO;

@Controller
@RequestMapping("/pages")
public class LoginwxControlller {
	private static Logger logger = LogManager.getLogger(LoginwxControlller.class);
	private static final String roleName = "MEMBER";

	@Autowired
	ILoginwxService loginwxService;

	@Autowired
	IUserService userService;

	@Autowired
	IMemberService memberService;

//	// 服务器token验证
//	@RequestMapping(value = "", method = RequestMethod.GET)
//	public void checkToken(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("服务器token验证：reqInit start");
//		String signature = request.getParameter("signature");
//		String timestamp = request.getParameter("timestamp");
//		String nonce = request.getParameter("nonce");
//		String echostr = request.getParameter("echostr");
//		logger.info("signature={},timestamp={},nonce={},echostr={}",signature,timestamp,nonce,echostr);
//		try {
//			PrintWriter pw = response.getWriter();
//			pw.append(echostr);
//			pw.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	// 请求授权微信授权回调后页面发送微信登陆
	@RequestMapping(value = "/con", method = RequestMethod.GET)
	public String reqInit(HttpServletRequest request, HttpServletResponse response) {
		logger.info("进入微信登陆授权接口，开始请求授权：reqInit start");
		try {
			String referenceId=request.getParameter("referenceId");
			if(null==referenceId||"".equals(referenceId)){
				referenceId="SYSTEMUSER";
			}
			// 获取授权链接
			String url = LoginwxRequest.getWXAuthorizeUrl(LoginwxUtil.redirectUrl, LoginwxUtil.scopeUserinfo,referenceId);
			// 重定向，让用户授权
			response.sendRedirect(url);
			logger.info("reqInit end");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	// 请求授权微信授权回调后页面发送微信登陆
	@RequestMapping(value = "/loginwx", method = RequestMethod.GET)
	public String loginwx(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("用户已成功授权，开始获取用户信息:loginwx start");
		// 获取传递的参数
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		Long referenceId=CommonConstant.SYSTEM_USER_ID;
		if(!"SYSTEMUSER".equals(state)){
			referenceId=Long.parseLong(state);
		}
		logger.info("成功得到了有效的授权码code= {},state= {}", code,referenceId);
		WXUserTokenVO wxUserTokenVO = null;
		WXUserInfoVO wxUserInfoVO = null;
		if (null != code && !"".equals(code)) {
			// 得到有效的授权码，获取授权凭证
			wxUserTokenVO = LoginwxRequest.getWXUserToken(code);
			if (null != wxUserTokenVO) {
				logger.info("成功得到了获取用户信息凭证接口AccessToken={},openId={}", wxUserTokenVO.getAccessToken(),
						wxUserTokenVO.getOpenId());
				// 得到微信用户信息
				wxUserInfoVO = LoginwxRequest.getWXUserInfo(wxUserTokenVO.getAccessToken(), wxUserTokenVO.getOpenId());
				logger.info("微信用户信息：wxUserInfoVO= " + wxUserInfoVO);
			}
		}
		if (null != wxUserInfoVO) {
			WXUserInfoVO userInfoVO = loginwxService.getWXUserInfoByUnionId(wxUserInfoVO.getUnionId());
			if (null != userInfoVO) {
				// 若已经注册过则直接登陆
				UserVO userVO = loginwxService.getUserByUserId(userInfoVO.getUserId());
				if(null!=userVO){
					//只有绑定手机号后才会创建新用户，此时有用户则是真实用户直接登录
					try {
						String url = LoginwxUtil.loginURL + "?userName=" + userVO.getNickName() + "&passWord="
								+ userVO.getPassword() + "&state=" + LoginwxUtil.state + "&unionId="
								+ wxUserInfoVO.getUnionId() + "&userId=" + userVO.getUserId()+"&referenceId="+userVO.getReferenceId();
						logger.info("username={},password={}", userVO.getNickName(), userVO.getPassword());
						try {
							logger.info("url={}", url);
							request.setAttribute("url", url);
							request.setAttribute("userId", userVO.getUserId());
							request.setAttribute("username", userVO.getNickName());
							request.setAttribute("password", userVO.getPassword());
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							logger.info("跳转路径 action={}", LoginwxUtil.shrioLoginUrl);
							String action = LoginwxUtil.shrioLoginUrl;
							request.getRequestDispatcher(action).forward(request, response);
						} catch (ServletException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					//若只创建微信用户没有绑定手机号则不会创建真实用户，此时登录为假登陆
					try {
						String url = LoginwxUtil.loginURL+"?userName=" + URLEncoder.encode(userInfoVO.getNickName(),"UTF-8") + "&passWord="
								+ userInfoVO.getPassWord() + "&state=" + LoginwxUtil.state + "&unionId="
								+ userInfoVO.getUnionId() + "&userId=" + userInfoVO.getUserId()+"&referenceId="+userInfoVO.getReferenceId();
						logger.info("username={},password={}", userInfoVO.getNickName(), userInfoVO.getPassWord());
						try {
							logger.info("url={}", url);
							request.setAttribute("url", url);
							request.setAttribute("userId", userInfoVO.getUserId());
							request.setAttribute("username", userInfoVO.getNickName());
							request.setAttribute("password", userInfoVO.getPassWord());
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							logger.info("跳转路径 action={}", LoginwxUtil.shrioLoginsUrl);
							String action = LoginwxUtil.shrioLoginsUrl;
							request.getRequestDispatcher(action).forward(request, response);
						} catch (ServletException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				//若没有微信信息，则创建微信用户
				// 得到了微信用户信息,存入微信用户表.
				String passWord = MD5Utils.encryptMD5("123456");
				wxUserInfoVO.setUserId(System.currentTimeMillis());
				wxUserInfoVO.setCreatedBy(wxUserInfoVO.getUserId());
				wxUserInfoVO.setLastUpdatedBy(wxUserInfoVO.getUserId());
				wxUserInfoVO.setPassWord(passWord);
				wxUserInfoVO.setReferenceId(referenceId);
				Long count = loginwxService.insertWXUserInfo(wxUserInfoVO);
				if (count > 0) {
					logger.info("插入微信用户信息成功userId={}", wxUserInfoVO.getUserId());
					// 假登陆
					try {
						String url = LoginwxUtil.loginURL + "?userName=" + URLEncoder.encode(wxUserInfoVO.getNickName(),"UTF-8") + "&passWord="
								+ wxUserInfoVO.getPassWord() + "&state=" + LoginwxUtil.state + "&unionId=" + wxUserInfoVO.getUnionId()
								+ "&userId=" + wxUserInfoVO.getUserId()+"&referenceId="+wxUserInfoVO.getReferenceId();
						logger.info("username={},password={}", wxUserInfoVO.getNickName(), wxUserInfoVO.getPassWord());
						try {
							request.setAttribute("url", url);
							request.setAttribute("userId", wxUserInfoVO.getUserId());
							request.setAttribute("username", wxUserInfoVO.getUserId());
							request.setAttribute("password", wxUserInfoVO.getPassWord());
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							String action = LoginwxUtil.shrioLoginsUrl;
							request.getRequestDispatcher(action).forward(request, response);
						} catch (ServletException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		logger.info("loginwx end");
		return null;
	}

	/**
	 * 绑定手机号
	 * 
	 * @param vo
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "/bindPhone/{isTrue}", method = RequestMethod.POST)
	@ResponseBody
	public ResultMsg<Object> bindPhone(@RequestBody WXUserInfoVO wxUserInfoVO,@PathVariable String isTrue) {
		ResultMsg<Object> msg = new ResultMsg<Object>();
		msg = loginwxService.bindWXUser(wxUserInfoVO,isTrue);
		return msg;
	}

	/**
	 * 校验手机号是否存在
	 * 
	 * @param phone
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/validphone/{phone}", method = RequestMethod.GET)
	public ResultMsg<Object> validPhone(@PathVariable String phone) {
		return loginwxService.validPhone(phone);
	}

	// 请求授权微信授权回调后页面发送微信登陆
	@RequestMapping(value = "/son", method = RequestMethod.GET)
	public String reqInite(HttpServletRequest request, HttpServletResponse response) {
		logger.info("进入微信登陆授权接口，开始请求授权");
		try {
			// 获取授权链接
			String url = LoginwxRequest.getWXAuthorizeUrl(LoginwxUtil.codeRedirectUrl, LoginwxUtil.scopeUserinfo,LoginwxUtil.state);
			// 重定向，让用户授权
			response.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/getTwoBarCode", method =  RequestMethod.GET )
	public void getTwoBarCode(HttpServletRequest request, HttpServletResponse response) {
		logger.info("进入获取二维码事件");
		logger.info("用户已授权，开始获取用户信息");
		// 获取传递的参数
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		WXUserTokenVO wxUserTokenVO = null;
		WXUserInfoVO wxUserInfoVO = null;
		if (null != code && !"".equals(code) && LoginwxUtil.state.equals(state)) {
			// 得到有效的授权码，获取授权凭证
			wxUserTokenVO = LoginwxRequest.getWXUserToken(code);
			if (null != wxUserTokenVO) {
				// 得到微信用户信息
				wxUserInfoVO = LoginwxRequest.getWXUserInfo(wxUserTokenVO.getAccessToken(), wxUserTokenVO.getOpenId());
			}
		}
		if (null != wxUserInfoVO) {
			// 通过unionid获取用户注册信息
			WXUserInfoVO wxUserInfo = loginwxService.getWXUserInfoByUnionId(wxUserInfoVO.getUnionId());
			// 如果返回不为空 则标识用户注册过
			if (null != wxUserInfo) {
				//先得到保存过的还在有效期的token
				String accessToken= loginwxService.getValidTokenByUnionId(wxUserInfoVO.getUnionId());
				if(null==accessToken||"".equals(accessToken)){
					//如果保存的token过了有效期则重新获得，得到二维码授权凭证接口
					accessToken=WeiXinUtil.getAccessToken(WeixinConfig.appId, WeixinConfig.secret).getToken();
					//更新所有用户保存有效的token
					wxUserInfoVO=new WXUserInfoVO();
					wxUserInfoVO.setAccessToken(accessToken);
					loginwxService.updateWXUserInfo(wxUserInfoVO);
				}
				logger.info("得到二维码凭证接口accessToken ={}", accessToken);
				//WeChatQRCodeVO wecarRecde = loginwxService.createTemporaryORCode(accessToken,WeixinConfig.expireSeconds
				//		,wxUserInfo.getUserId());
				//logger.info("返回的ticket ={}", wecarRecde.getTicket());
				// 获取二维码
				//String imgUrl = loginwxService.chageQr(wecarRecde.getTicket());
				//上传二维码素材并发送消息
				loginwxService.uploadImg(accessToken,"",wxUserInfo);
				try {
					//发送完消息关闭微信窗口
					PrintWriter	out=response.getWriter();
					out.write("<script type=\"text/javascript\">setTimeout(function(){WeixinJSBridge.call('closeWindow');},500);</script>");
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
//	@RequestMapping(value = "/acceptQRCode", method = { RequestMethod.POST, RequestMethod.GET })
//	public String acceptQRCode(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("进入扫描二维码事件");
//		 try {
//			// 处理接收消息  
//			ServletInputStream in = request.getInputStream();  
//			// 将POST流转换为XStream对象  
//			XStream xs = WeiXinUtil.createXstream();  
//			xs.processAnnotations(WXMessageVO.class);  
//			// 将指定节点下的xml节点数据映射为对象  
//			xs.alias("xml", WXMessageVO.class);  
//			// 将流转换为字符串  
//			StringBuilder xmlMsg = new StringBuilder();  
//			byte[] b = new byte[4096];  
//			for (int n; (n = in.read(b)) != -1;) {  
//			    xmlMsg.append(new String(b, 0, n, "UTF-8"));  
//			}  
//			// 将xml内容转换为WXMessageVO对象  
//			WXMessageVO wxMessageVO = (WXMessageVO) xs.fromXML(xmlMsg.toString());  
//			logger.info("得到扫码事件消息");
//			String referenceId="";
//			//取得事件类型
//			String eventType=wxMessageVO.getEvent();
//			// 取得消息类型  
//			String msgType = wxMessageVO.getMsgType();  
//			
//			// 根据消息类型获取对应的消息内容，如果是事件  
//			if (msgType.equals("event")) { 
//				//如果是未关注扫码事件
//				if(eventType.equals("subscribe")){
//					//未关注，返回场景值带“qrscene_”前缀，需截取字符串
//					referenceId = wxMessageVO.getEventKey().substring(wxMessageVO.getEventKey().indexOf("_")+1);// 推荐人
//					request.setAttribute("referenceId", referenceId);
//					//关注完授权登陆
//					request.getRequestDispatcher("/pages/con").forward(request, response);
//				}else if(eventType.equals("SCAN")){
//					//已关注，直接取
//					referenceId = wxMessageVO.getEventKey();
//					request.setAttribute("referenceId", referenceId);
//					//已关注则授权登陆
//					request.getRequestDispatcher("/pages/con").forward(request, response);
//				}	
//			}
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ServletException e) {
//			e.printStackTrace();
//		}
//		//不做任何响应就直接放回空串，不返回空串，微信服务器会默认等待5秒响应，重复三次
//        return "";
//	}
	/**
	 * 登陆
	 * 
	 * @param vo
	 * @param request
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/wxlogin", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResultMsg<Object> wxUserLogin(HttpServletRequest request, HttpServletResponse rep,
			RedirectAttributes redirectAttributes) throws Exception {
		logger.info("微信登陆 wxUserLogin={}");
		ResultMsg<Object> rs = new ResultMsg<Object>();
		try {
			String username = request.getAttribute("username").toString();
			String password = request.getAttribute("password").toString();
			logger.info("用户名和密码 ：username={},password={}", username, password);
			String userName = username;
			Map<String, Object> queryLoginParm = new HashMap<String, Object>();
			queryLoginParm.put("userName", userName);
			List<Map<String, Object>> un = userService.queryUserIdByLogin(queryLoginParm);
			username = un.size() > 0 ? String.valueOf(un.get(0).get("userId")) : "";
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			Subject currentUser = SecurityUtils.getSubject();
			token.setRememberMe(true);
			currentUser.login(token);
			long uId = 0;
			uId = StringUtils.isEmpty(username) ? uId : Long.parseLong(username);
			UserVO user = userService.getUserRole(uId);
			Set<String> rolesName = user.getRolesName();
			boolean isMember = rolesName.contains(roleName);
			if (isMember) {
				List<Map<String, Object>> list = memberService.getMemberById(uId);
				if (list.size() > 0) {
					Map<String, Object> data = list.get(0);
					data.remove("password");
					rs.setData(data);
				}
			}
			logger.info("action action={}", request.getAttribute("url").toString());
			String action = request.getAttribute("url").toString();
			rep.sendRedirect(action);
			logger.info("重定向成功了");
			rs.setReturnCode(ReturnConstant.RETURN_CODE_200);
			rs.setReturnMsg(ReturnConstant.RETURN_MSG_900);
		} 
		catch (LockedAccountException lae) {
			rs.setReturnCode(ReturnConstant.RETURN_CODE_901);
			rs.setReturnMsg(ReturnConstant.RETURN_MSG_901);
		} catch (ExcessiveAttemptsException eae) {
			rs.setReturnCode(ReturnConstant.RETURN_CODE_902);
			rs.setReturnMsg(ReturnConstant.RETURN_MSG_902);
		} catch (AuthenticationException ae) {
			rs.setReturnCode(ReturnConstant.RETURN_CODE_903);
			rs.setReturnMsg(ReturnConstant.RETURN_MSG_903);
		}
		return rs;
	}
	
	/**
	 * 假登陆
	 * 
	 * @param vo
	 * @param request
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/wxlogins", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public void wxUserLogins(HttpServletRequest request, HttpServletResponse rep,
			RedirectAttributes redirectAttributes) throws Exception {
			//得到参数
			String username = request.getAttribute("userId").toString();
			String password = request.getAttribute("password").toString();
			logger.info("用户id和密码 ：userId={},password={}", username, password);
			//记住用户token
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			Subject currentUser = SecurityUtils.getSubject();
			token.setRememberMe(true);
			//currentUser.login(token);
			currentUser.getSession().setAttribute("CURRENT_USER", Long.parseLong(username));
			//转发登陆页面
			logger.info("action action={}", request.getAttribute("url").toString());
			String action = request.getAttribute("url").toString();
			rep.sendRedirect(action);
			logger.info("重定向成功了");
	}

}
