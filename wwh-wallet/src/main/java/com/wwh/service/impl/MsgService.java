package com.wwh.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wwh.dao.IMsgDao;
import com.wwh.service.IMsgService;

@Service
public class MsgService implements IMsgService {

	@Autowired
	private IMsgDao msgDao;

	/**
	 * 查询短信
	 * 
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> queryMsgInfoByCondition(Map<String, Object> map) {
		return msgDao.queryMsgInfoByCondition(map);
	}

	/**
	 * 增加短信记录
	 * 
	 * @param map
	 * @return
	 */
	public int addMsg(Map<String, Object> map) {
		return msgDao.addMsg(map);
	}

	/**
	 * 修改短信记录
	 * 
	 * @param map
	 * @return
	 */
	public int modifyMsg(Map<String, Object> map) {
		return msgDao.modifyMsg(map);
	}

	/**
	 * 查询短信详情
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getMsgInfo(Map<String, Object> map) {
		return msgDao.getMsgInfo(map);
	}
}
