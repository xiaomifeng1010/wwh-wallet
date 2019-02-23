package com.wwh.pay.weixin.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.wwh.pay.weixin.req.UnifiedOrderRequest;
import com.wwh.pay.weixin.util.WeiXinPayConfigUtil;
import com.wwh.pay.weixin.util.WeiXinUtils;
import com.wwh.pay.weixin.util.XMLUtil;
import com.wwh.service.IPayService;
import com.wwh.vo.PayDetailVO;

@Controller
@RequestMapping(value = "/wx")
public class WeiXinController {

	private static Logger logger = LogManager.getLogger(WeiXinController.class);

	@Autowired
	private IPayService payService;

	/**
	 * 手机版微信支付
	 * 
	 * @param payAmountType
	 * @param paySeq
	 * @param response
	 * @throws Exception
	 */
	/*
	 * @RequestMapping(value = "/mweixinpay/{payAmountType}/{paySeq}", method =
	 * RequestMethod.GET) public void mWeixinPay(@PathVariable String
	 * payAmountType, @PathVariable String paySeq, HttpServletResponse response)
	 * throws Exception { // 调用微信预支付 PayDetailVO payDetailVO =
	 * payService.getPayDetailByPayAmountTypeAndPaySeq(payAmountType, paySeq);
	 * if (payDetailVO == null) { logger.info("无此订单"); return; } BigDecimal
	 * amount = payDetailVO.getAmount(); // 生成订单 String orderInfo =
	 * WeiXinUtils.createOrderInfo2(paySeq, payAmountType, amount); // 调统一下单API
	 * String code_url = WeiXinUtils.httpOrder(orderInfo); // 此处就不生成二维码了
	 * 直接将支付的链接返回到页面，点击支付按钮支付即可 }
	 */

	/**
	 * 创建二维码
	 */
	@RequestMapping(value = "/createqrcode/{payAmountType}/{paySeq}", method = RequestMethod.GET)
	public void createQRCode(@PathVariable String payAmountType, @PathVariable String paySeq,
			HttpServletResponse response) throws Exception {

		// 将返回预支付交易链接（code_url）生成二维码图片
		// 这里使用的是zxing <span
		// style="color:#ff0000;"><strong>说明1(见文末)</strong></span>

		// 调用微信预支付

		PayDetailVO payDetailVO = payService.getPayDetailByPayAmountTypeAndPaySeq(payAmountType, paySeq);

		if (payDetailVO == null) {
			logger.info("无此订单");
			return;
		}
		BigDecimal amount = payDetailVO.getAmount();
		// 生成订单
		String orderInfo = WeiXinUtils.createOrderInfo(paySeq, payAmountType, amount);
		// 调统一下单API
		String code_url = WeiXinUtils.httpOrder(orderInfo);
		// 将返回预支付交易链接（code_url）生成二维码图片
		// 这里使用的是zxing <span
		// style="color:#ff0000;"><strong>说明1(见文末)</strong></span>

		WeiXinUtils.urlToImages(code_url, 200, 200, "png", response);

	}

	/**
	 * 创建二维码
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/returnhuidiao")
	public void returnhuidiao(HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.info("returnhuidiao  回调 被调用");

		this.paySuccess(request, response);

	}

	public void paySuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {

		InputStream inStream = request.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		String result = new String(outSteam.toByteArray(), "utf-8");// 获取微信调用我们notify_url的返回信息

		Map<Object, Object> map = XMLUtil.doXMLParse(result);

		// for(Object keyValue : map.keySet()){
		// System.out.println(keyValue+"="+map.get(keyValue));
		// }
		if (map.get("result_code").toString().equalsIgnoreCase("SUCCESS")) {
			// 对数据库的操作
			// 微信文档的错误写法
			// response.getWriter().write(PayCommonUtil.setXML("SUCCESS", ""));
			// //告诉微信服务器，我收到信息了，不要在调用回调action了

			response.getWriter().write("success"); // 告诉微信服务器，我收到信息了，不要在调用回调action了

			String nonceStr = (String) map.get("nonce_str");
			// String totalFee=(String) map.get("total_fee");
			final String paySeq = (String) map.get("out_trade_no"); // 支付订单号
			final String returnCode = (String) map.get("transaction_id"); // 微信支付订单号
			final String payAmountType = nonceStr.substring(17);

			new Thread(new Runnable() {
				@Override
				public void run() {
					PayDetailVO payDetailVO = null;
					try {
						payDetailVO = payService.updatePrePaySuccess(payAmountType, paySeq, returnCode);
						if (null == payDetailVO) {
							logger.info(
									"微信支付成功后， 本地服务修改数据 为成功状态失败 ，payAmountType" + payAmountType + ",paySeq" + paySeq);
						} else {
							payService.updatePrePaySuccessAndDoNext(payAmountType, payDetailVO.getIdCard(),
									payDetailVO);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			// System.out.println("-------------"+PayCommonUtil.setXML("SUCCESS",
			// ""));
		}
	}

	/**
	 * 生成订单
	 * 
	 * @param paySeq
	 * @return
	 */
	public static String createOrderInfo(String paySeq, BigDecimal amount) {
		// 保存在我的数据库中 一条单记录

		// 生成订单对象
		UnifiedOrderRequest unifiedOrderRequest = new UnifiedOrderRequest();
		unifiedOrderRequest.setAppid(WeiXinPayConfigUtil.getAppid());// 公众账号ID
		unifiedOrderRequest.setMch_id(WeiXinPayConfigUtil.getMchid());// 商户号
		unifiedOrderRequest.setNonce_str(WeiXinUtils.makeUUID());// 随机字符串 <span
		// style="color:#ff0000;"><strong>说明2(见文末)</strong></span>
		unifiedOrderRequest.setBody("weiwowushangping");// 商品描述
		unifiedOrderRequest.setOut_trade_no(paySeq);// 商户订单号
		unifiedOrderRequest.setTotal_fee("" + amount); // 金额需要扩大100倍:1代表支付时是0.01
		unifiedOrderRequest.setSpbill_create_ip(WeiXinUtils.localIp());// 终端IP
		unifiedOrderRequest.setNotify_url(WeiXinPayConfigUtil.NOTIFY_URL);// 通知地址
		unifiedOrderRequest.setTrade_type("NATIVE");// JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付

		unifiedOrderRequest.setSign(WeiXinUtils.createSign(unifiedOrderRequest));// 签名<span
																					// style="color:#ff0000;"><strong>说明5(见文末，签名方法一并给出)</strong></span>

		// 将订单对象转为xml格式
		XStream xStream = new XStream(new XppDriver(new XmlFriendlyNameCoder("_-", "_"))); // <span
																							// style="color:#ff0000;"><strong>说明3(见文末)</strong></span>

		xStream.alias("xml", UnifiedOrderRequest.class);// 根元素名需要是xml

		return xStream.toXML(unifiedOrderRequest);
	}

}
