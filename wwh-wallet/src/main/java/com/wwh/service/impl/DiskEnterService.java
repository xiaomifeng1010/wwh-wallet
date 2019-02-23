package com.wwh.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wwh.dao.ICertificationDao;
import com.wwh.dao.IDiskDao;
import com.wwh.dao.IDiskTypeProfitDao;
import com.wwh.dao.IEarningDao;
import com.wwh.dao.IEmptyPointDiskDao;
import com.wwh.dao.IFixedRebateDao;
import com.wwh.dao.IPayDetailDao;
import com.wwh.dao.IPlatformProfitDao;
import com.wwh.dao.IPointDao;
import com.wwh.dao.IPointGrapRelationDao;
import com.wwh.dao.IScoreDao;
import com.wwh.dao.ISingleProfitAmountConfigDao;
import com.wwh.dao.IUserDao;
import com.wwh.dao.IWalletAmountDao;
import com.wwh.dao.IWalletDiskRelationDao;
import com.wwh.dao.IWalletDiskWaittingDao;
import com.wwh.dao.IWalletProfitDetailDao;
import com.wwh.dao.IWithdrawReserveConfigDao;
import com.wwh.enums.ActiveFlagEnum;
import com.wwh.enums.AssetExpenditureEnum;
import com.wwh.enums.DeleteFlagEnum;
import com.wwh.enums.DiskEnum;
import com.wwh.enums.DiskStatusEnum;
import com.wwh.enums.DiskWaittingStatusEnum;
import com.wwh.enums.EliminateTypeEnum;
import com.wwh.enums.FixedRebateEnum;
import com.wwh.enums.FixedRebateTypeEnum;
import com.wwh.enums.IsCalcuatedEnum;
import com.wwh.enums.OrderByTypeEnum;
import com.wwh.enums.PayAmountTypeEnum;
import com.wwh.enums.PayStatusEnum;
import com.wwh.enums.ProfitScoreTypeEnum;
import com.wwh.enums.ProfitTargetTypeEnum;
import com.wwh.enums.RoleEnum;
import com.wwh.enums.WaittingStatusEnum;
import com.wwh.enums.WaittingTypeEnum;
import com.wwh.enums.WalletProfitTargetTypeEnum;
import com.wwh.service.IDiskEnterService;
import com.wwh.service.IDiskService;
import com.wwh.service.IFixedRebateService;
import com.wwh.service.ILogService;
import com.wwh.service.IPayService;
import com.wwh.service.IStapDiskEnterService;
import com.wwh.util.CommonConstant;
import com.wwh.util.DiskRelationRuleUtils;
import com.wwh.util.SeqUtils;
import com.wwh.util.StringUtils;
import com.wwh.util.ToolsUtil;
import com.wwh.vo.AgentProfitDetailVO;
import com.wwh.vo.AgentProfitVO;
import com.wwh.vo.AgentRegionVO;
import com.wwh.vo.AreaVO;
import com.wwh.vo.AssetExpenditureVO;
import com.wwh.vo.CityVO;
import com.wwh.vo.DefinedProfitConfigVO;
import com.wwh.vo.DirectorConfigVO;
import com.wwh.vo.DiskProfitVO;
import com.wwh.vo.DiskRelationVO;
import com.wwh.vo.DiskTypeProfitVO;
import com.wwh.vo.DiskTypeUpWaittingVO;
import com.wwh.vo.DiskUserRelationVO;
import com.wwh.vo.DiskVO;
import com.wwh.vo.EliminateVO;
import com.wwh.vo.FixedRebateVO;
import com.wwh.vo.IdcardRelationVO;
import com.wwh.vo.KeyPointRecommendVO;
import com.wwh.vo.NullPointVO;
import com.wwh.vo.PayDetailVO;
import com.wwh.vo.PlatformProfitScoreVO;
import com.wwh.vo.PlatformProfitVO;
import com.wwh.vo.PointGrapRelationVO;
import com.wwh.vo.ProfitDetailVO;
import com.wwh.vo.RecommendAPointStatisticsVO;
import com.wwh.vo.RegionUserVO;
import com.wwh.vo.SingleProfitAmountConfigVO;
import com.wwh.vo.SystemWalletVO;
import com.wwh.vo.WalletAmountVO;
import com.wwh.vo.WalletDiskRelationVO;
import com.wwh.vo.WalletDiskWaittingVO;
import com.wwh.vo.WalletProfitDetailVO;

@Service
public class DiskEnterService implements IDiskEnterService {

	private static Logger logger = LogManager.getLogger(DiskEnterService.class);

	private static Map<String, Object> map = new HashMap<>();

	@Autowired
	private IWalletAmountDao walletAmountDao;

	@Autowired
	private IScoreDao scoreDao;

	@Autowired
	private IWalletDiskRelationDao walletDiskRelationDao;

	@Autowired
	private IWalletDiskWaittingDao walletDiskWaittingDao;

	@Autowired
	private IDiskDao diskDao;

	@Autowired
	private IWalletProfitDetailDao walletProfitDetailDao;

	@Autowired
	private IWithdrawReserveConfigDao withdrawReserveConfigDao;

	@Autowired
	private ISingleProfitAmountConfigDao singleProfitAmountConfigDao;

	@Autowired
	private IPointGrapRelationDao pointGrapRelationDao;

	@Autowired
	private IDiskTypeProfitDao diskTypeProfitDao;

	@Autowired
	private IPlatformProfitDao platformProfitDao;

	@Autowired
	private IPointDao pointDao;

	@Autowired
	private IUserDao userDao;

	@Autowired
	private IDiskService diskService;

	@Autowired
	private IPayService payService;

	@Autowired
	private IPayDetailDao payDetailDao;

	@Autowired
	private ILogService logService;

	@Autowired
	private IEmptyPointDiskDao emptyPointDiskDao;
	@Autowired
	private IEarningDao earningDao;
	@Autowired
	private ICertificationDao certificationDao;

	@Autowired
	private IFixedRebateDao fixedRebateDao;
	
	@Autowired
	private IFixedRebateService fixedRebateService;
	
	@Autowired
	private IStapDiskEnterService stapDiskEnterService;

	@Override
	public void fissionDisk(String diskSeq, Long lastUpdateBy, String enterDiskIdCard) throws Exception {
		logger.info("盘满,分裂:fissionDisk start param[diskSeq]:" + diskSeq + "param[lastUpdateBy]:" + lastUpdateBy
				+ "param[enterDiskIdCard]:" + enterDiskIdCard);
		DiskVO oldDisk = diskDao.getByDiskSeq(diskSeq);
		String oldDiskType = oldDisk.getDiskType();
		String oldDiskSeq = oldDisk.getDiskSeq();
		// 生成三个新盘的 盘号
		String newDiskSeq1 = SeqUtils.generateDiskOrderSN();
		String newDiskSeq2 = SeqUtils.generateDiskOrderSN();
		String newDiskSeq3 = SeqUtils.generateDiskOrderSN();
		List<String> newDiskSeqs = new ArrayList<>();
		newDiskSeqs.add(newDiskSeq1);
		newDiskSeqs.add(newDiskSeq2);
		newDiskSeqs.add(newDiskSeq3);
		// 得到老盘的用户详情
		List<WalletDiskRelationVO> diskRelationVOs = new LinkedList<>(
				walletDiskRelationDao.selectDiskRelation(oldDisk.getDiskType(), diskSeq));
		// 得到老潘的用户详情
		List<WalletDiskRelationVO> diskRelationVOs1 = new ArrayList<>(
				walletDiskRelationDao.selectDiskRelation(oldDisk.getDiskType(), diskSeq));
		// 考核经理
		grapPoinExamManager(oldDisk, diskRelationVOs1, enterDiskIdCard, newDiskSeqs, lastUpdateBy);

		List<WalletDiskRelationVO> diskRelationMembersAndPlans = new ArrayList<>();
		// 循环老盘的数据得到规划师和所有的会员
		for (WalletDiskRelationVO diskRelationVO : diskRelationVOs) {
			if (diskRelationVO.getRoleId() != 2 && diskRelationVO.getRoleId() != 1) {
				diskRelationMembersAndPlans.add(diskRelationVO);
			}
		}
		// 得到总监
		WalletDiskRelationVO director = diskRelationVOs.get(0);

		int count1 = 0;
		int count2 = 0;
		int count3 = 0;

		// 拿到三个 即将晋升总监的经理的userId
		Long diretorUserId1 = diskRelationVOs.get(1).getUserId();
		Long diretorUserId2 = diskRelationVOs.get(2).getUserId();
		Long diretorUserId3 = diskRelationVOs.get(3).getUserId();

		// 拿到三个 即将晋升总监的经理的IDCARD
		String diretorIdCard1 = diskRelationVOs.get(1).getIdCard();
		String diretorIdCard2 = diskRelationVOs.get(2).getIdCard();
		String diretorIdCard3 = diskRelationVOs.get(3).getIdCard();

		List<WalletDiskRelationVO> newDiskRelation1 = new ArrayList<>();
		List<WalletDiskRelationVO> newDiskRelation2 = new ArrayList<>();
		List<WalletDiskRelationVO> newDiskRelation3 = new ArrayList<>();
		// 分别得到三个盘的用户数据
		synchronized (this) {
		for (int i = 0; i < diskRelationVOs.size(); i++) {
			if (DiskRelationRuleUtils.firstList.contains(diskRelationVOs.get(i).getLocaltion())) {
				++count1;
				newDiskRelation1.add(generatorFisionDiskRelation(diskRelationVOs, count1, newDiskSeq1,
						diretorUserId1, diretorIdCard1, i, lastUpdateBy, diskRelationVOs.get(i).getLocaltion()));
			} else if (DiskRelationRuleUtils.secondList.contains(diskRelationVOs.get(i).getLocaltion())) {
				++count2;
				newDiskRelation2.add(generatorFisionDiskRelation(diskRelationVOs, count2, newDiskSeq2,
						diretorUserId2, diretorIdCard2, i, lastUpdateBy, diskRelationVOs.get(i).getLocaltion()));
			} else if (DiskRelationRuleUtils.thirdList.contains(diskRelationVOs.get(i).getLocaltion())) {
				++count3;
				newDiskRelation3.add(generatorFisionDiskRelation(diskRelationVOs, count3, newDiskSeq3,
						diretorUserId3, diretorIdCard3, i, lastUpdateBy, diskRelationVOs.get(i).getLocaltion()));
			}
		}
	}
		// 构建三个新盘的信息
		List<DiskVO> newDisk = new ArrayList<>();
		DiskVO initDisk = generatorNewDiskVO(oldDiskSeq, oldDiskType, newDiskSeq1, 0, lastUpdateBy);
		newDisk.add(initDisk);
		initDisk = generatorNewDiskVO(oldDiskSeq, oldDiskType, newDiskSeq2, 1, lastUpdateBy);
		newDisk.add(initDisk);
		initDisk = generatorNewDiskVO(oldDiskSeq, oldDiskType, newDiskSeq3, 2, lastUpdateBy);
		newDisk.add(initDisk);
		// 取出所有的IDCARD 信息
		List<IdcardRelationVO> relationMembers1 = diskRelationTOIdCardRelation(newDiskRelation1, 4L);
		List<IdcardRelationVO> relationMembers2 = diskRelationTOIdCardRelation(newDiskRelation2, 4L);
		List<IdcardRelationVO> relationMembers3 = diskRelationTOIdCardRelation(newDiskRelation3, 4L);
		List<IdcardRelationVO> relationPlans1 = diskRelationTOIdCardRelation(newDiskRelation1, 3L);
		List<IdcardRelationVO> relationPlans2 = diskRelationTOIdCardRelation(newDiskRelation2, 3L);
		List<IdcardRelationVO> relationPlans3 = diskRelationTOIdCardRelation(newDiskRelation3, 3L);
		logger.info("=========================newDiskRelation1={}", newDiskRelation1);
		logger.info("=========================newDiskRelation2={}", newDiskRelation2);
		logger.info("=========================newDiskRelation3={}", newDiskRelation3);
		// 会员IDCARD 更新
		Map<String, String> idCardMap = updateIdCardFromOldIdCard(relationMembers1, newDiskSeq1);
		idCardMap.putAll(updateIdCardFromOldIdCard(relationMembers2, newDiskSeq2));
		idCardMap.putAll(updateIdCardFromOldIdCard(relationMembers3, newDiskSeq3));
		// 规划师 IDCARD 更新
		idCardMap.putAll(updateIdCardFromOldIdCard(relationPlans1, newDiskSeq1));
		idCardMap.putAll(updateIdCardFromOldIdCard(relationPlans2, newDiskSeq2));
		idCardMap.putAll(updateIdCardFromOldIdCard(relationPlans3, newDiskSeq3));
		for (WalletDiskRelationVO diskRelationVO : diskRelationMembersAndPlans) {
			diskRelationVO.setIdCard(idCardMap.get(diskRelationVO.getIdCard()));
		}
		// 插入新的盘关系表 (会员和规划师)
		walletDiskRelationDao.insertBatchDiskRelation(oldDiskType, diskRelationMembersAndPlans);
		// 更新原来盘的空点记录 为N
		emptyPointDiskDao.updateNullPointIsUsableByDiskSeq("N", oldDiskSeq);
		List<IdcardRelationVO> idcardRelationVOs1 = walletDiskRelationDao.getIdCardsByDiskSeq(newDiskSeq1);
		List<IdcardRelationVO> idcardRelationVOs2 = walletDiskRelationDao.getIdCardsByDiskSeq(newDiskSeq2);
		List<IdcardRelationVO> idcardRelationVOs3 = walletDiskRelationDao.getIdCardsByDiskSeq(newDiskSeq3);

		for (IdcardRelationVO idcardRelationVO : idcardRelationVOs1) {
			// 拿到该用户在系统的A推荐点统计表
			RecommendAPointStatisticsVO aPointStatisticsVO = pointDao
					.getUserAPointBydiskType(idcardRelationVO.getUserId(), idcardRelationVO.getCurrentDiskType());
			// 为入盘用户创建空点表
			NullPointVO nullPointVO = new NullPointVO();
			nullPointVO.setUserId(idcardRelationVO.getUserId());
			nullPointVO.setDiskType(idcardRelationVO.getCurrentDiskType());
			nullPointVO.setDiskSeq(newDiskSeq1);
			if (aPointStatisticsVO == null) {
				nullPointVO.setEmptyPoint(3);
			} else {
				nullPointVO.setEmptyPoint(3 - (aPointStatisticsVO.getRemainRecommendPoint() / 2));
			}
			nullPointVO.setIdCard(idcardRelationVO.getCurrentIdCard());
			nullPointVO.setIsUsable("Y");
			nullPointVO.setCreatedBy(lastUpdateBy);
			nullPointVO.setLastUpdatedBy(lastUpdateBy);
			pointDao.addNullPoint(nullPointVO);
		}
		for (IdcardRelationVO idcardRelationVO : idcardRelationVOs2) {
			// 拿到该用户在系统的A推荐点统计表
			RecommendAPointStatisticsVO aPointStatisticsVO = pointDao
					.getUserAPointBydiskType(idcardRelationVO.getUserId(), idcardRelationVO.getCurrentDiskType());
			// 为入盘用户创建空点表
			NullPointVO nullPointVO = new NullPointVO();
			nullPointVO.setUserId(idcardRelationVO.getUserId());
			nullPointVO.setDiskType(idcardRelationVO.getCurrentDiskType());
			nullPointVO.setDiskSeq(newDiskSeq2);
			if (aPointStatisticsVO == null) {
				nullPointVO.setEmptyPoint(3);
			} else {
				nullPointVO.setEmptyPoint(3 - (aPointStatisticsVO.getRemainRecommendPoint() / 2));
			}
			nullPointVO.setIdCard(idcardRelationVO.getCurrentIdCard());
			nullPointVO.setIsUsable("Y");
			nullPointVO.setCreatedBy(lastUpdateBy);
			nullPointVO.setLastUpdatedBy(lastUpdateBy);
			pointDao.addNullPoint(nullPointVO);
		}
		for (IdcardRelationVO idcardRelationVO : idcardRelationVOs3) {
			// 拿到该用户在系统的A推荐点统计表
			RecommendAPointStatisticsVO aPointStatisticsVO = pointDao
					.getUserAPointBydiskType(idcardRelationVO.getUserId(), idcardRelationVO.getCurrentDiskType());
			// 为入盘用户创建空点表
			NullPointVO nullPointVO = new NullPointVO();
			nullPointVO.setUserId(idcardRelationVO.getUserId());
			nullPointVO.setDiskType(idcardRelationVO.getCurrentDiskType());
			nullPointVO.setDiskSeq(newDiskSeq3);
			if (aPointStatisticsVO == null) {
				nullPointVO.setEmptyPoint(3);
			} else {
				nullPointVO.setEmptyPoint(3 - (aPointStatisticsVO.getRemainRecommendPoint() / 2));
			}
			nullPointVO.setIdCard(idcardRelationVO.getCurrentIdCard());
			nullPointVO.setIsUsable("Y");
			nullPointVO.setCreatedBy(lastUpdateBy);
			nullPointVO.setLastUpdatedBy(lastUpdateBy);
			pointDao.addNullPoint(nullPointVO);
		}
		// 原盘里的总监
		List<WalletDiskRelationVO> diskRelationDirector = new ArrayList<>();
		diskRelationDirector.add(director);
		List<IdcardRelationVO> relationDirector = diskRelationTOIdCardRelation(diskRelationDirector, 1L);
		// 如果总监的收益记录
		PointGrapRelationVO pointGrapRelationVO = pointGrapRelationDao.getProfitPointsByIdCard(director.getIdCard(),
				oldDiskSeq);
		// 结束所有的 旧 idCard
		logger.info("===================================oldDiskSeq={},Seq={}", oldDiskSeq,1);
		walletDiskRelationDao.updateAllIdCardRelationStatuxx(oldDiskSeq, null, "N");
		// 三个新盘插入盘表
		diskDao.insertBatch(newDisk);
		String diskTypeLevelUp = DiskEnum.getNextNameByName(oldDisk.getDiskType());
		Long receiveUserId = director.getUserId();
		String oldReceiveUserIdCard = director.getIdCard();
		String newReceiveUserIdCarad = SeqUtils.generateIdCardSN();
		if (pointGrapRelationVO != null && pointGrapRelationVO.getProfitPoints() == 3
				&& !oldDisk.getDiskType().equals(DiskEnum.XINGMIN.name())
				&& !oldDisk.getDiskType().equals(DiskEnum.FUMIN.name())) {

			// 大佬飞升
			// userId & userIdCard
			// 飞升
			// 系统扣除给大佬补全的金额
			// 扣除系统收益
			SystemWalletVO systemWalletVO = new SystemWalletVO();
			AssetExpenditureVO assetExpenditureVO = new AssetExpenditureVO();
			systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
			switch (oldDisk.getDiskType()) {
			case "TIYAN":
				systemWalletVO.setSystemProfitAmount(BigDecimal.valueOf(5).multiply(BigDecimal.valueOf(-1)));
				systemWalletVO.setSystemExpenditureAmount(BigDecimal.valueOf(5));
				assetExpenditureVO.setExpenditureAmount(BigDecimal.valueOf(5));
				break;
			case "HUIMIN":
				systemWalletVO.setSystemProfitAmount(BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(-1)));
				systemWalletVO.setSystemExpenditureAmount(BigDecimal.valueOf(50));
				assetExpenditureVO.setExpenditureAmount(BigDecimal.valueOf(50));
				break;
			}
			scoreDao.updateSystemProfitByVO(systemWalletVO);
			// 插入资金支付流水表
			assetExpenditureVO.setUserId(CommonConstant.SYSTEM_USER_ID);
			assetExpenditureVO.setTargetUserId(receiveUserId);
			assetExpenditureVO.setStatus(AssetExpenditureEnum.SUCCESSED.name());
			assetExpenditureVO.setCreatedBy(receiveUserId);
			assetExpenditureVO.setLastUpdatedBy(receiveUserId);
			scoreDao.addAssetExpenditureVO(assetExpenditureVO);
			// 1.0版本,如果下一个系统没有身份,则可以进入
			if (!isExistsMyRecordIndiskType(diskTypeLevelUp, receiveUserId)) {
				// 飞升 入盘
				logger.info("发生大佬飞升 ,大佬渡劫成功,飞升大佬为:" + receiveUserId);
				enterDiskTypeByFassion(diskTypeLevelUp, receiveUserId, oldReceiveUserIdCard, false);
				// 点亮该大佬的里程碑
				userDao.updateMilestone(diskTypeLevelUp, receiveUserId);
				// 扣除该大佬的 储备金
				DiskProfitVO diskProfitVO = walletProfitDetailDao.getDiskProfitByIdCard(oldReceiveUserIdCard);
				BigDecimal saveGold = diskProfitVO.getSaveAmount().multiply(new BigDecimal(-1));
				// 平台总收益表扣除
				PlatformProfitVO platformProfitVO = new PlatformProfitVO();
				platformProfitVO.setUserId(receiveUserId);
				platformProfitVO.setPlatformRemainAmount(saveGold);
				platformProfitVO.setSaveAmount(saveGold);
				platformProfitVO.setMemberRemainAmount(saveGold);
				switch (oldDisk.getDiskType()) {
				case "TIYAN":
					platformProfitVO.setTiyanSaveAmount(saveGold);
					break;
				case "HUIMIN":
					platformProfitVO.setHuiminSaveAmount(saveGold);
					break;
				case "XINGMIN":
					platformProfitVO.setXingminSaveAmount(saveGold);
					break;
				case "FUMIN":
					platformProfitVO.setFuminSaveAmount(saveGold);
					break;
				}
				platformProfitDao.updatePlatformProfitByUserId(platformProfitVO);
				// 该总监晋升下一个系统扣除的储备金将给他冲为积分（只有体验、惠民晋升加积分）
				PayDetailVO payDetailVO = new PayDetailVO();// 充值详情表
				PlatformProfitScoreVO platformProfitScoreVO = new PlatformProfitScoreVO();// 平台积分收益详情表
				WalletAmountVO walletAmountVO = new WalletAmountVO();// 钱包积分
				SystemWalletVO systemWalletVO1 = new SystemWalletVO();// 系统收益表
				switch (oldDisk.getDiskType()) {
				case "TIYAN":
					payDetailVO.setScore(new BigDecimal(10000));// 充值积分
					payDetailVO.setAmount(new BigDecimal(5000));// 充值金额
					payDetailVO.setPayAmountType(PayAmountTypeEnum.HUIMIN.name());// 充值类型
					platformProfitScoreVO.setProfitScore(new BigDecimal(10000));// 平台积分
					platformProfitScoreVO.setProfitScoreRemark("充值HUIMIN获得积分");// 平台积分来源信息
					walletAmountVO.setTotalScore(new BigDecimal(10000));// 钱包总积分
					walletAmountVO.setRemainScore(new BigDecimal(10000));// 钱包会员剩余积分
					systemWalletVO1.setSystemExpenditureScore(new BigDecimal(10000));// 系统积分支出
					break;
				case "HUIMIN":
					payDetailVO.setScore(new BigDecimal(100000));// 充值积分
					payDetailVO.setAmount(new BigDecimal(50000));// 充值金额
					payDetailVO.setPayAmountType(PayAmountTypeEnum.FUMIN.name());// 充值类型
					platformProfitScoreVO.setProfitScore(new BigDecimal(100000));// 平台积分
					platformProfitScoreVO.setProfitScoreRemark("充值FUMIN获得积分");// 平台积分来源信息
					walletAmountVO.setTotalScore(new BigDecimal(100000));// 钱包总积分
					walletAmountVO.setRemainScore(new BigDecimal(100000));// 钱包会员剩余积分
					systemWalletVO1.setSystemExpenditureScore(new BigDecimal(100000));// 系统积分支出
					break;
				}
				// 增加充值记录
				payDetailVO.setIdCard(newReceiveUserIdCarad);
				payDetailVO.setPayStatus(PayStatusEnum.PAYSUCCESSED.name());
				payDetailVO.setUserId(receiveUserId);
				payDetailVO.setIsCalcuated(IsCalcuatedEnum.N.name());
				payDetailVO.setActiveFlag(ActiveFlagEnum.Y.name());
				payDetailVO.setDeleteFlag(DeleteFlagEnum.N.name());
				payDetailVO.setCreatedBy(receiveUserId);
				payDetailVO.setLastUpdatedBy(receiveUserId);
				payDetailVO.setReturnCode("SCD" + System.currentTimeMillis());
				String seq = SeqUtils.generateRechargeOrderSN();
				payDetailVO.setPaySeq(seq);
				payDetailDao.insert(payDetailVO);
				// 更新平台积分收益详情
				platformProfitScoreVO.setUserId(receiveUserId);
				platformProfitScoreVO.setProfitScoreType(ProfitScoreTypeEnum.RECHARGEPROFIT.toString());
				platformProfitScoreVO.setSourceUserId(CommonConstant.SYSTEM_USER_ID);// 充值默认来源为-999999
				scoreDao.insertScoreProfitRecord(platformProfitScoreVO);
				// 更新钱包积分
				walletAmountVO.setUserId(receiveUserId);
				scoreDao.updateWallteScore(walletAmountVO);
				// 给平台收益表增加对应的积分支出
				systemWalletVO1.setSystemId(CommonConstant.SYSTEM_USER_ID);
				scoreDao.updateSystemProfitByVO(systemWalletVO1);

				// 定返功能（先判断是否开启定返获取充值信息）
				if (fixedRebateService.getRebateConfig(CommonConstant.ONOFF_FIXEDREBATE)) {
					logger.info("总监晋升扣除储备金,插入定返统计信息");
					// 非富民兴民总监晋升时，扣除的储备金作为定返身份进入定返
					FixedRebateVO fixedRebateVo = new FixedRebateVO();
					fixedRebateVo.setUserId(receiveUserId);
					fixedRebateVo.setIdCard(newReceiveUserIdCarad);
					switch (oldDisk.getDiskType()) {
					case "TIYAN":
						fixedRebateVo.setSysType(PayAmountTypeEnum.HUIMIN.name());
						fixedRebateVo.setPayAmount(diskProfitVO.getSaveAmount().add(new BigDecimal(5)));
						fixedRebateVo.setTotalAmount(
								(diskProfitVO.getSaveAmount().add(new BigDecimal(5))).multiply(BigDecimal.valueOf(CommonConstant.FIXEDREBATE_RATE)));
						break;
					case "HUIMIN":
						fixedRebateVo.setSysType(PayAmountTypeEnum.FUMIN.name());
						fixedRebateVo.setPayAmount(diskProfitVO.getSaveAmount().add(new BigDecimal(50)));
						fixedRebateVo.setTotalAmount(
								(diskProfitVO.getSaveAmount().add(new BigDecimal(50))).multiply(BigDecimal.valueOf(CommonConstant.FIXEDREBATE_RATE)));
						break;
					}
					fixedRebateVo.setReturnedAmount(new BigDecimal(0));// 初始已返为0
					fixedRebateVo.setRebateStauts(FixedRebateEnum.RUNNING.name());// 列盘扣除储备金直接开始新身份定返
					fixedRebateVo.setCreatedBy(receiveUserId);
					fixedRebateVo.setLastUpdatedBy(receiveUserId);
					fixedRebateDao.insertRebateInfo(fixedRebateVo);
				}
			} else {
				// 去盘等待表继续修炼
				logger.info("发生大佬飞升 ,但是大佬渡劫失败,去等待表继续修炼:" + receiveUserId);
				updateWaittingDisk(diskTypeLevelUp, receiveUserId, newReceiveUserIdCarad,
						WaittingTypeEnum.WAITTING.name());
			}
		} else {
			if (pointGrapRelationVO != null) {
				// directorToUpWaitting(relationDirector.get(0), lastUpdateBy,
				// oldDiskSeq);
				// 平台收益总表
				BigDecimal saveGold = walletProfitDetailDao.getDiskProfitByIdCard(director.getIdCard()).getSaveAmount()
						.multiply(new BigDecimal(-1));
				// 平台总收益表扣除
				PlatformProfitVO platformProfitVO = new PlatformProfitVO();
				platformProfitVO.setUserId(receiveUserId);
				//platformProfitVO.setPlatformRemainAmount(saveGold);平台剩余总金额不变
				platformProfitVO.setSaveAmount(saveGold);
				//platformProfitVO.setMemberRemainAmount(saveGold);会员剩余总金额不变
				platformProfitVO.setMemberWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
				platformProfitVO.setPlatformWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
				platformProfitVO.setLastUpdatedBy(lastUpdateBy);
				switch (oldDisk.getDiskType()) {
				case "TIYAN":
					platformProfitVO.setTiyanSaveAmount(saveGold);
					platformProfitVO.setTiyanWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
					break;
				case "HUIMIN":
					platformProfitVO.setHuiminSaveAmount(saveGold);
					platformProfitVO.setHuiminWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
					break;
				case "XINGMIN":
					platformProfitVO.setXingminSaveAmount(saveGold);
					platformProfitVO.setXingminWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
					break;
				case "FUMIN":
					platformProfitVO.setFuminSaveAmount(saveGold);
					platformProfitVO.setFuminWithdrawalsAmount(saveGold.multiply(BigDecimal.valueOf(-1)));
					break;
				}
				platformProfitDao.updatePlatformProfitByUserId(platformProfitVO);
			}
		}
		// 大佬的在等待表中的身份 进入系统
		List<WalletDiskWaittingVO> diskWaittingVOs = walletDiskWaittingDao.getDiskWaittingVOByUserId(receiveUserId,
				DiskWaittingStatusEnum.WAITTING.name(), oldDisk.getDiskType());
		if (diskWaittingVOs != null && diskWaittingVOs.size() > 0) {
			logger.info("大佬的在等待表中的身份 开始 进入系统");
			WalletDiskWaittingVO diskWaittingVO = diskWaittingVOs.get(0);
			List<IdcardRelationVO> idcardRelationVOs = walletDiskRelationDao
					.getIdCardRelationByCurrentIdCard(diskWaittingVO.getPayUserId(), diskWaittingVO.getLastIdCard());
			IdcardRelationVO lastIdcardRelationVO = new IdcardRelationVO();
			if (idcardRelationVOs.size() == 0) {
				lastIdcardRelationVO.setCurrentDiskSeq(CommonConstant.SYSTEM_USER_ID.toString());
				lastIdcardRelationVO.setCurrentDiskType(CommonConstant.SYSTEM_USER_ID.toString());
				lastIdcardRelationVO.setCurrentRoleId(CommonConstant.SYSTEM_USER_ID);
			} else {
				lastIdcardRelationVO = walletDiskRelationDao
						.getIdCardRelationByCurrentIdCard(diskWaittingVO.getPayUserId(), diskWaittingVO.getLastIdCard())
						.get(0);
			}
			IdcardRelationVO idcardRelationVO = new IdcardRelationVO();
			idcardRelationVO.setUserId(diskWaittingVO.getPayUserId());
			idcardRelationVO.setCurrentIdCard(diskWaittingVO.getPayIdCard());
			idcardRelationVO.setLastIdCard(diskWaittingVO.getLastIdCard());
			idcardRelationVO.setLastDiskSeq(lastIdcardRelationVO.getCurrentDiskSeq());
			idcardRelationVO.setCurrentDiskType(oldDisk.getDiskType());
			idcardRelationVO.setLastDiskType(lastIdcardRelationVO.getCurrentDiskType());
			idcardRelationVO.setCurrentRoleId(4L);
			idcardRelationVO.setLastRoleId(lastIdcardRelationVO.getCurrentRoleId());
			idcardRelationVO.setStatuxx("Y");
			idcardRelationVO.setCreatedBy(lastUpdateBy);
			idcardRelationVO.setLastUpdatedBy(lastUpdateBy);
			// 最早等待的身份进入系统
			tandemService(diskWaittingVO.getDiskType(), diskWaittingVO.getPayUserId(), diskWaittingVO.getPayIdCard(),
					idcardRelationVO);
			logger.info("大佬的在等待表中的身份 结束  进入系统");
		}
		logger.info("盘满,分裂:fissionDisk end");
	}

	/**
	 * 
	 * @Title: grapPoinExamManager
	 * @Description: 考核经理
	 * @param oldDisk
	 * @param diskRelationVOs
	 * @return: void
	 * @throws Exception
	 */
	private void grapPoinExamManager(DiskVO oldDisk, List<WalletDiskRelationVO> diskRelationVOs, String enterDiskIdCard,
			List<String> newDiskSeqs, Long lastUpdatedBy) throws Exception {
		logger.info("考核经理:grapPoinExamManager start param[oldDisk]:" + oldDisk + "param[diskRelationVOs]:"
				+ diskRelationVOs + "param[enterDiskIdCard]:" + enterDiskIdCard + "param[newDiskSeqs]:" + newDiskSeqs
				+ "param[lastUpdatedBy]:" + lastUpdatedBy);
		// ============================= 找到 40人 算好 抢点 《并看看 总监是否晋升
		// 找出三个即将晋升的经理
		WalletDiskRelationVO manager1 = diskRelationVOs.get(1);
		WalletDiskRelationVO manager2 = diskRelationVOs.get(2);
		WalletDiskRelationVO manager3 = diskRelationVOs.get(3);

		WalletDiskRelationVO diskRelationVO = new WalletDiskRelationVO();
		List<IdcardRelationVO> idcardRelationVOs = new ArrayList<>();

		// 得到三个经理在老盘中的idcard
		IdcardRelationVO idcardRelationVO1 = walletDiskRelationDao
				.getIdCardRelationByCurrentIdCard(null, manager1.getIdCard()).get(0);
		IdcardRelationVO idcardRelationVO2 = walletDiskRelationDao
				.getIdCardRelationByCurrentIdCard(null, manager2.getIdCard()).get(0);
		IdcardRelationVO idcardRelationVO3 = walletDiskRelationDao
				.getIdCardRelationByCurrentIdCard(null, manager3.getIdCard()).get(0);
		List<WalletDiskRelationVO> newManages = new ArrayList<>();

		// 查看 三个即将晋升的经理的A点
		RecommendAPointStatisticsVO recomAPointManer1 = pointDao.getUserAPointBydiskType(manager1.getUserId(),
				manager1.getDiskType());
		RecommendAPointStatisticsVO recomAPointManer2 = pointDao.getUserAPointBydiskType(manager2.getUserId(),
				manager2.getDiskType());
		RecommendAPointStatisticsVO recomAPointManer3 = pointDao.getUserAPointBydiskType(manager3.getUserId(),
				manager3.getDiskType());
		PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();

		// 得到三个经理得剩余a点数
		int myPointMana1 = 0;// 初始为0
		if (null != recomAPointManer1) {
			myPointMana1 = recomAPointManer1.getRemainRecommendPoint();
		}

		int myPointMana2 = 0;// 初始为0
		if (null != recomAPointManer2) {
			myPointMana2 = recomAPointManer2.getRemainRecommendPoint();
		}

		int myPointMana3 = 0;// 初始为0
		if (null != recomAPointManer3) {
			myPointMana3 = recomAPointManer3.getRemainRecommendPoint();
		}

		// 各个经理的收益点数
		List<Integer> managerProfitPoints = new ArrayList<>();
		Integer profitPoints1 = myPointMana1 / 2;
		Integer profitPoints2 = myPointMana2 / 2;
		Integer profitPoints3 = myPointMana3 / 2;
		managerProfitPoints.add(profitPoints1);
		managerProfitPoints.add(profitPoints2);
		managerProfitPoints.add(profitPoints3);

		if (myPointMana1 >= CommonConstant.ENOUGH_POINT_NUM) {
			// 不进行抢点 ;A 推荐点 减去 6，
			recomAPointManer1.setUseUpRecommendPoint(6);
			recomAPointManer1.setRemainRecommendPoint(6);
			recomAPointManer1.setReduce('Y');
			recomAPointManer1.setLastUpdatedBy(lastUpdatedBy);
			pointDao.updateAPointStatistics(recomAPointManer1);
			// 插入 新的 IDCARD 关系表,插入新的 盘关系表
			idcardRelationVOs.add(idcardRelationVO1);
			diskRelationVO.setDiskSeq(newDiskSeqs.get(0));
			diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
			diskRelationVO.setRoleId(1L);
			diskRelationVO.setLocaltion(1);
			diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
			diskRelationVO.setUserId(manager1.getUserId());
			diskRelationVO.setDiskType(manager1.getDiskType());
			diskRelationVO.setCreatedBy(lastUpdatedBy);
			diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
			diskRelationVO.setActiveFlag("Y");
			diskRelationVO.setDeleteFlag("N");
			String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(0)).get(manager1.getIdCard());
			diskRelationVO.setIdCard(idCard);
			walletDiskRelationDao.insert(diskRelationVO);
			// 插入总监职位利益分配表 满收益晋级
			pointGrapRelationVO.setIdCard(idCard);
			pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
			pointGrapRelationVO.setIsDirector("Y");
			pointGrapRelationVO.setProfitPoints(3);
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			pointGrapRelationVO.setDiskType(manager1.getDiskType());
			pointGrapRelationVO.setActiveFlag("Y");
			pointGrapRelationVO.setDeleteFlag("N");
			pointGrapRelationDao.insert(pointGrapRelationVO);
		} else {
			if (myPointMana1 < 2) {
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.add(idcardRelationVO1);
				idcardRelationVOs.add(idcardRelationVO1);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(0));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager1.getUserId());
				diskRelationVO.setDiskType(manager1.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(0))
						.get(manager1.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 直接让该经理滚粗 好像 需要在这里让 该经理 进入淘汰表...（╯－＿－）╯╧╧
				EliminateVO eliminateVO = new EliminateVO();
				eliminateVO.setDiskSeq(newDiskSeqs.get(0));
				eliminateVO.setRoleId(3L);
				eliminateVO.setEliminateType(EliminateTypeEnum.FAILUREASSESSMENT.name());
				eliminateVO.setEliminateRemark("贡献不够,经理考核失败淘汰");
				eliminateVO.setUserId(manager1.getUserId());
				eliminateVO.setIdCard(idCard);
				userDao.addEliminateVO(eliminateVO);
			} else {
				// A 推荐点 减去 晋升用的
				recomAPointManer1.setUseUpRecommendPoint(deductionAPoints(recomAPointManer1.getRemainRecommendPoint()));
				recomAPointManer1
						.setRemainRecommendPoint(deductionAPoints(recomAPointManer1.getRemainRecommendPoint()));
				recomAPointManer1.setReduce('Y');
				recomAPointManer1.setLastUpdatedBy(lastUpdatedBy);
				pointDao.updateAPointStatistics(recomAPointManer1);
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.add(idcardRelationVO1);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(0));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager1.getUserId());
				diskRelationVO.setDiskType(manager1.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(0))
						.get(manager1.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 插入总监职位利益分配表
				// 计算出收益多少点
				pointGrapRelationVO.setIdCard(idCard);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
				pointGrapRelationVO.setIsDirector("Y");
				pointGrapRelationVO.setProfitPoints(myPointMana1 / 2);
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationVO.setDiskType(manager1.getDiskType());
				pointGrapRelationVO.setActiveFlag("Y");
				pointGrapRelationVO.setDeleteFlag("N");
				pointGrapRelationDao.insert(pointGrapRelationVO);
			}
			// 总监职位利益分配
		}
		if (myPointMana2 >= CommonConstant.ENOUGH_POINT_NUM) {
			// 不进行抢点 ;A 推荐点 减去 6，
			recomAPointManer2.setUseUpRecommendPoint(6);
			recomAPointManer2.setRemainRecommendPoint(6);
			recomAPointManer2.setReduce('Y');
			recomAPointManer2.setLastUpdatedBy(lastUpdatedBy);
			pointDao.updateAPointStatistics(recomAPointManer2);
			// 插入 新的 IDCARD 关系表,插入新的 盘关系表
			idcardRelationVOs.clear();
			idcardRelationVOs.add(idcardRelationVO2);
			diskRelationVO.setDiskSeq(newDiskSeqs.get(1));
			diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
			diskRelationVO.setRoleId(1L);
			diskRelationVO.setLocaltion(1);
			diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
			diskRelationVO.setUserId(manager2.getUserId());
			diskRelationVO.setDiskType(manager2.getDiskType());
			diskRelationVO.setCreatedBy(lastUpdatedBy);
			diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
			diskRelationVO.setActiveFlag("Y");
			diskRelationVO.setDeleteFlag("N");
			String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(1)).get(manager2.getIdCard());
			diskRelationVO.setIdCard(idCard);
			walletDiskRelationDao.insert(diskRelationVO);
			// 插入总监职位利益分配表 满收益晋级
			pointGrapRelationVO.setIdCard(idCard);
			pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
			pointGrapRelationVO.setIsDirector("Y");
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			pointGrapRelationVO.setProfitPoints(3);
			pointGrapRelationVO.setDiskType(manager2.getDiskType());
			pointGrapRelationVO.setActiveFlag("Y");
			pointGrapRelationVO.setDeleteFlag("N");
			pointGrapRelationDao.insert(pointGrapRelationVO);
		} else {
			if (myPointMana2 < 2) {
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.clear();
				idcardRelationVOs.add(idcardRelationVO2);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(1));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager2.getUserId());
				diskRelationVO.setDiskType(manager2.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(1))
						.get(manager2.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 直接让该经理滚粗 好像 需要在这里让 该经理 进入淘汰表...（╯－＿－）╯╧╧
				EliminateVO eliminateVO = new EliminateVO();
				eliminateVO.setDiskSeq(newDiskSeqs.get(1));
				eliminateVO.setRoleId(3L);
				eliminateVO.setEliminateType(EliminateTypeEnum.FAILUREASSESSMENT.name());
				eliminateVO.setEliminateRemark("贡献不够,经理考核失败淘汰");
				eliminateVO.setUserId(manager2.getUserId());
				eliminateVO.setIdCard(idCard);
				userDao.addEliminateVO(eliminateVO);
			} else {
				// A 推荐点 减去 晋升用的
				recomAPointManer2.setUseUpRecommendPoint(deductionAPoints(recomAPointManer2.getRemainRecommendPoint()));
				recomAPointManer2
						.setRemainRecommendPoint(deductionAPoints(recomAPointManer2.getRemainRecommendPoint()));
				recomAPointManer2.setReduce('Y');
				recomAPointManer2.setLastUpdatedBy(lastUpdatedBy);
				pointDao.updateAPointStatistics(recomAPointManer2);
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.clear();
				idcardRelationVOs.add(idcardRelationVO2);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(1));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager2.getUserId());
				diskRelationVO.setDiskType(manager2.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(1))
						.get(manager2.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 插入总监职位利益分配表
				// 计算出收益多少点
				pointGrapRelationVO.setIdCard(idCard);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
				pointGrapRelationVO.setIsDirector("Y");
				pointGrapRelationVO.setProfitPoints(myPointMana2 / 2);
				pointGrapRelationVO.setDiskType(manager2.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationVO.setActiveFlag("Y");
				pointGrapRelationVO.setDeleteFlag("N");
				pointGrapRelationDao.insert(pointGrapRelationVO);
			}
		}
		if (myPointMana3 >= CommonConstant.ENOUGH_POINT_NUM) {
			// 不进行抢点 ;A 推荐点 减去 6，
			recomAPointManer3.setUseUpRecommendPoint(6);
			recomAPointManer3.setRemainRecommendPoint(6);
			recomAPointManer3.setReduce('Y');
			recomAPointManer3.setLastUpdatedBy(lastUpdatedBy);
			pointDao.updateAPointStatistics(recomAPointManer3);
			// 插入 新的 IDCARD 关系表,插入新的 盘关系表
			idcardRelationVOs.clear();
			idcardRelationVOs.add(idcardRelationVO3);
			diskRelationVO.setDiskSeq(newDiskSeqs.get(2));
			diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
			diskRelationVO.setRoleId(1L);
			diskRelationVO.setLocaltion(1);
			diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
			diskRelationVO.setUserId(manager3.getUserId());
			diskRelationVO.setDiskType(manager3.getDiskType());
			diskRelationVO.setCreatedBy(lastUpdatedBy);
			diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
			diskRelationVO.setActiveFlag("Y");
			diskRelationVO.setDeleteFlag("N");
			String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(2)).get(manager3.getIdCard());
			diskRelationVO.setIdCard(idCard);
			walletDiskRelationDao.insert(diskRelationVO);
			// 插入总监利益分配表 满收益晋级
			pointGrapRelationVO.setIdCard(idCard);
			pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
			pointGrapRelationVO.setIsDirector("Y");
			pointGrapRelationVO.setProfitPoints(3);
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			pointGrapRelationVO.setDiskType(manager3.getDiskType());
			pointGrapRelationVO.setActiveFlag("Y");
			pointGrapRelationVO.setDeleteFlag("N");
			pointGrapRelationDao.insert(pointGrapRelationVO);
		} else {
			if (myPointMana3 < 2) {
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.clear();
				idcardRelationVOs.add(idcardRelationVO3);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(2));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager3.getUserId());
				diskRelationVO.setDiskType(manager3.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(2))
						.get(manager3.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 直接让该经理滚粗 好像 需要在这里让 该经理 进入淘汰表...（╯－＿－）╯╧╧
				EliminateVO eliminateVO = new EliminateVO();
				eliminateVO.setDiskSeq(newDiskSeqs.get(2));
				eliminateVO.setRoleId(3L);
				eliminateVO.setEliminateType(EliminateTypeEnum.FAILUREASSESSMENT.name());
				eliminateVO.setEliminateRemark("贡献不够,经理考核失败淘汰");
				eliminateVO.setUserId(manager3.getUserId());
				eliminateVO.setIdCard(idCard);
				userDao.addEliminateVO(eliminateVO);
			} else {
				// A 推荐点 减去 晋升用的
				recomAPointManer3.setUseUpRecommendPoint(deductionAPoints(recomAPointManer3.getRemainRecommendPoint()));
				recomAPointManer3
						.setRemainRecommendPoint(deductionAPoints(recomAPointManer3.getRemainRecommendPoint()));
				recomAPointManer3.setReduce('Y');
				recomAPointManer3.setLastUpdatedBy(lastUpdatedBy);
				pointDao.updateAPointStatistics(recomAPointManer3);
				// 插入新的IDCARD 关系表,插入新的盘关系表
				idcardRelationVOs.clear();
				idcardRelationVOs.add(idcardRelationVO3);
				diskRelationVO.setDiskSeq(newDiskSeqs.get(2));
				diskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
				diskRelationVO.setRoleId(1L);
				diskRelationVO.setLocaltion(1);
				diskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
				diskRelationVO.setUserId(manager3.getUserId());
				diskRelationVO.setDiskType(manager3.getDiskType());
				diskRelationVO.setCreatedBy(lastUpdatedBy);
				diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
				diskRelationVO.setActiveFlag("Y");
				diskRelationVO.setDeleteFlag("N");
				String idCard = updateIdCardFromOldIdCard(idcardRelationVOs, newDiskSeqs.get(2))
						.get(manager3.getIdCard());
				diskRelationVO.setIdCard(idCard);
				walletDiskRelationDao.insert(diskRelationVO);
				// 插入总监职位利益分配表
				// 计算出收益多少点
				pointGrapRelationVO.setIdCard(idCard);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
				pointGrapRelationVO.setIsDirector("Y");
				pointGrapRelationVO.setProfitPoints(myPointMana3 / 2);
				pointGrapRelationVO.setDiskType(manager3.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationVO.setActiveFlag("Y");
				pointGrapRelationVO.setDeleteFlag("N");
				pointGrapRelationDao.insert(pointGrapRelationVO);
			}
		}
		newManages.add(manager1);
		newManages.add(manager2);
		newManages.add(manager3);
		grapPoit(oldDisk, diskRelationVOs, managerProfitPoints, lastUpdatedBy, newDiskSeqs);
		// 在这里把三个经理的空点记录失效掉
		NullPointVO nullPointVO = new NullPointVO();
		for (WalletDiskRelationVO walletDiskRelationVO : newManages) {
			nullPointVO.setIdCard(walletDiskRelationVO.getIdCard());
			nullPointVO.setIsUsable("N");
			pointDao.updateEmptyPoint(nullPointVO);
		}
		logger.info("grapPoinExamManager end");
	}

	/**
	 * 
	 * @Title: grapPoit
	 * @Description: 抢点啦 (╯‵□′)╯︵┻━┻
	 * @param oldDisk
	 * @param diskRelationVOs
	 * @param managerProfitPoints
	 *            三个经理各拿了多少利益点
	 * @param lastUpdatedBy
	 * @return: void
	 */
	private void grapPoit(DiskVO oldDisk, List<WalletDiskRelationVO> diskRelationVOs, List<Integer> managerProfitPoints,
			Long lastUpdatedBy, List<String> newDiskSeqs) {
		logger.info("抢点:grapPoit param start");
		logger.info("抢点:grapPoit param[oldDisk]:" + oldDisk + "param[diskRelationVOs]:" + diskRelationVOs
				+ "param[managerProfitPoints]:" + managerProfitPoints + "param[lastUpdatedBy]:" + lastUpdatedBy);
		// 总共有多少点数 可抢
		Integer remainCount = 9
				- (managerProfitPoints.get(0) + managerProfitPoints.get(1) + managerProfitPoints.get(2));
		logger.info("当前盘经理空出的点数");
		// 当前抢了几个点
		Integer currentPoint = 0;

		List<Long> membersUserIdList = new ArrayList<>();
		List<Long> plansUserIdList = new ArrayList<>();
		List<Long> managersUserIdList = new ArrayList<>();
		List<String> directorsIdCardList = new ArrayList<>();
		Map<Long, WalletDiskRelationVO> userIdAndDiskRelation = new HashMap<>();
		// 直接取人(得到会员 经理 规划师 总监List)
		for (WalletDiskRelationVO walletDiskRelationVO : diskRelationVOs) {
			int roleIdIter = Integer.valueOf(String.valueOf(walletDiskRelationVO.getRoleId()));
			if (RoleEnum.getCodeByName(RoleEnum.MEMBER.name()) == roleIdIter) {
				membersUserIdList.add(walletDiskRelationVO.getUserId());
			} else if (RoleEnum.getCodeByName(RoleEnum.PLAN.name()) == roleIdIter) {
				plansUserIdList.add(walletDiskRelationVO.getUserId());
			} else if (RoleEnum.getCodeByName(RoleEnum.MANAGER.name()) == roleIdIter) {
				managersUserIdList.add(walletDiskRelationVO.getUserId());
			} else if (RoleEnum.getCodeByName(RoleEnum.DIRECTOR.name()) == roleIdIter) {
				directorsIdCardList.add(walletDiskRelationVO.getIdCard());
			}
			userIdAndDiskRelation.put(walletDiskRelationVO.getUserId(), walletDiskRelationVO);
		}
		// 得到各层级人员的关键点记录
		List<KeyPointRecommendVO> memberKeyPointRecommendVOs = pointGrapRelationDao
				.getAKeyPointsFromUserIds(oldDisk.getDiskType(), membersUserIdList, remainCount);
		List<KeyPointRecommendVO> planKeyPointRecommendVOs = pointGrapRelationDao
				.getAKeyPointsFromUserIds(oldDisk.getDiskType(), plansUserIdList, remainCount);
		List<KeyPointRecommendVO> managerKeyPointRecommendVOs = pointGrapRelationDao
				.getAKeyPointsFromUserIds(oldDisk.getDiskType(), managersUserIdList, remainCount);
		List<KeyPointRecommendVO> directorKeyPointRecommendVOs = pointGrapRelationDao
				.getBKeyPointsFromIdCard(directorsIdCardList.get(0), remainCount);

		List<KeyPointRecommendVO> getoutDirectorKeyPointRecommendVOs = pointGrapRelationDao
				.getBKeyPointsFromGetoutDirector(oldDisk.getDiskType(), remainCount);

		// 会员开始抢
		for (KeyPointRecommendVO keyPointRecommendVO : memberKeyPointRecommendVOs) {
			// 判断是否还有空点可以抢
			if (currentPoint == remainCount) {
				return;
			}
			Integer managerLocation = -1;
			// 这里得出 该会员的直系经理,优先选择直系
			if (DiskRelationRuleUtils.firstList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.firstList.get(0);
			}
			if (DiskRelationRuleUtils.secondList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.secondList.get(0);
			}
			if (DiskRelationRuleUtils.thirdList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.thirdList.get(0);
			}
			if (3 - managerProfitPoints.get(managerLocation - 2) != 0) {
				// 优先抢直系经理的点
				// 将该 关键点记录 改为不可用
				String diskseq = newDiskSeqs.get(managerLocation - 2);
				// 将该 关键点记录 改为不可用
				keyPointRecommendVO.setIsUsable("N");
				keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
				keyPointRecommendVO.setIdCard(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getIdCard());
				pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
				// 分配总监职位利益
				PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
				// 拿到会员的IDCard
				pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
				// 该经理所在盘的盘号
				pointGrapRelationVO.setDiskSeq(diskseq);
				pointGrapRelationVO.setIsDirector("N");
				pointGrapRelationVO.setProfitPoints(1);
				pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
				managerProfitPoints.set(managerLocation - 2, managerProfitPoints.get(managerLocation - 2) + 1);
				currentPoint++;
			} else {
				keyPointRecommendVO.setIsUsable("N");
				keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
				keyPointRecommendVO.setIdCard(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getIdCard());
				pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
				// 等于三的话,就没有办法抢他的直系总监了. 在其他两个经理职位中 按 顺序给一个点
				// 分配总监职位利益
				PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
				pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());

				pointGrapRelationVO.setIsDirector("N");
				pointGrapRelationVO.setProfitPoints(1);
				pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				// 该被抢经理所在盘的盘号 (managerLocation - 1 = 该用户的直属经理是第几个经理 )
				switch (managerLocation - 1) {
				case 1:
					if (3 - managerProfitPoints.get(1) != 0) {
						managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					} else if (3 - managerProfitPoints.get(2) != 0) {
						managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					}
					break;
				case 2:
					if (3 - managerProfitPoints.get(0) != 0) {
						managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					} else if (3 - managerProfitPoints.get(2) != 0) {
						managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					}
					break;
				case 3:
					if (3 - managerProfitPoints.get(0) != 0) {
						managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					} else if (3 - managerProfitPoints.get(1) != 0) {
						managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					}
					break;
				}

				pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
				currentPoint++;
			}
		}
		// 规划师开始抢
		for (KeyPointRecommendVO keyPointRecommendVO : planKeyPointRecommendVOs) {
			// 判断是否还有空点可以抢
			if (currentPoint == remainCount) {
				return;
			}
			Integer managerLocation = -1;
			// 这里得出 该会员的直系经理,优先选择直系
			if (DiskRelationRuleUtils.firstList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.firstList.get(0);
			}
			if (DiskRelationRuleUtils.secondList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.secondList.get(0);
			}
			if (DiskRelationRuleUtils.thirdList
					.contains(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getLocaltion())) {
				managerLocation = DiskRelationRuleUtils.thirdList.get(0);
			}
			if (3 - managerProfitPoints.get(managerLocation - 2) != 0) {
				// 优先抢直系经理的点
				// 将该 关键点记录 改为不可用
				String diskSeq = newDiskSeqs.get(managerLocation - 2);
				// 将该 关键点记录 改为不可用
				keyPointRecommendVO.setIsUsable("N");
				keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
				keyPointRecommendVO.setIdCard(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getIdCard());
				pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
				// 分配总监职位利益
				PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
				pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
				// 该经理所在盘的盘号
				pointGrapRelationVO.setDiskSeq(diskSeq);
				pointGrapRelationVO.setIsDirector("N");
				pointGrapRelationVO.setProfitPoints(1);
				pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
				managerProfitPoints.set(managerLocation - 2, managerProfitPoints.get(managerLocation - 2) + 1);
				currentPoint++;
			} else {
				// 等于三的话,就没有办法抢他的直系总监了.
				keyPointRecommendVO.setIsUsable("N");
				keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
				keyPointRecommendVO.setIdCard(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getIdCard());
				pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
				// 分配总监职位利益
				PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
				pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
				// 该被抢经理所在盘的盘号
				switch (managerLocation - 1) {
				case 1:
					if (3 - managerProfitPoints.get(1) != 0) {
						managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					} else if (3 - managerProfitPoints.get(2) != 0) {
						managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					}
					break;
				case 2:
					if (3 - managerProfitPoints.get(0) != 0) {
						managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					} else if (3 - managerProfitPoints.get(2) != 0) {
						managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					}
					break;
				case 3:
					if (3 - managerProfitPoints.get(0) != 0) {
						managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					} else if (3 - managerProfitPoints.get(1) != 0) {
						managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
						pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					}
					break;
				}
				pointGrapRelationVO.setIsDirector("N");
				pointGrapRelationVO.setProfitPoints(1);
				pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
				pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
				pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
				pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);

				currentPoint++;
			}
		}
		// 经理开始抢
		for (KeyPointRecommendVO keyPointRecommendVO : managerKeyPointRecommendVOs) {
			// 判断是否还有空点可以抢
			if (currentPoint == remainCount) {
				return;
			}
			// 先算出是哪位经理 在抢
			WalletDiskRelationVO diskRelationVO = userIdAndDiskRelation.get(keyPointRecommendVO.getUserId());
			// 2,3,4
			Integer managerLocation = diskRelationVO.getLocaltion();
			keyPointRecommendVO.setIsUsable("N");
			keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
			keyPointRecommendVO.setIdCard(userIdAndDiskRelation.get(keyPointRecommendVO.getUserId()).getIdCard());
			pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
			// 分配总监职位利益
			PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
			pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
			// 被抢经理所在盘的盘号
			if (managerLocation - 1 == 3) {
				if (managerProfitPoints.get(0) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
				} else if (managerProfitPoints.get(1) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
				}
			} else if (managerLocation - 1 == 2) {
				if (managerProfitPoints.get(0) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
					managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
				} else if (managerProfitPoints.get(2) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
				}
			} else if (managerLocation - 1 == 1) {
				if (3 - managerProfitPoints.get(1) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
					managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
				} else if (3 - managerProfitPoints.get(2) != 3) {
					pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
					managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
				}
			}
			pointGrapRelationVO.setIsDirector("N");
			pointGrapRelationVO.setProfitPoints(1);
			pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
			currentPoint++;
		}
		// 本盘大佬开始抢
		for (KeyPointRecommendVO keyPointRecommendVO : directorKeyPointRecommendVOs) {
			// 判断是否还有空点可以抢
			if (currentPoint == remainCount) {
				return;
			}
			keyPointRecommendVO.setIsUsable("N");
			keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
			pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
			// 分配总监职位利益
			PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
			pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
			pointGrapRelationVO.setIsDirector("N");
			pointGrapRelationVO.setProfitPoints(1);
			pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			// 该被抢经理所在盘的盘号
			if (3 - managerProfitPoints.get(0) != 0) {
				managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
			} else if (3 - managerProfitPoints.get(1) != 0) {
				managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
			} else if (3 - managerProfitPoints.get(2) != 0) {
				managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
			}
			pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
			currentPoint++;
		}
		// 本系统出局的大佬开始抢
		for (KeyPointRecommendVO keyPointRecommendVO : getoutDirectorKeyPointRecommendVOs) {
			// 判断是否还有空点可以抢
			if (currentPoint == remainCount) {
				return;
			}
			keyPointRecommendVO.setIsUsable("N");
			keyPointRecommendVO.setLastUpdatedBy(lastUpdatedBy);
			System.out.println();
			pointGrapRelationDao.updateIsUsableToN(keyPointRecommendVO);
			// 分配总监职位利益
			PointGrapRelationVO pointGrapRelationVO = new PointGrapRelationVO();
			pointGrapRelationVO.setIdCard(keyPointRecommendVO.getIdCard());
			pointGrapRelationVO.setIsDirector("N");
			pointGrapRelationVO.setProfitPoints(1);
			pointGrapRelationVO.setDiskType(oldDisk.getDiskType());
			pointGrapRelationVO.setCreatedBy(lastUpdatedBy);
			pointGrapRelationVO.setLastUpdatedBy(lastUpdatedBy);
			// 该被抢经理所在盘的盘号
			if (3 - managerProfitPoints.get(0) != 0) {
				managerProfitPoints.set(0, managerProfitPoints.get(0) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(0));
			} else if (3 - managerProfitPoints.get(1) != 0) {
				managerProfitPoints.set(1, managerProfitPoints.get(1) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(1));
			} else if (3 - managerProfitPoints.get(2) != 0) {
				managerProfitPoints.set(2, managerProfitPoints.get(2) + 1);
				pointGrapRelationVO.setDiskSeq(newDiskSeqs.get(2));
			}
			pointGrapRelationDao.addGrapRelationVO(pointGrapRelationVO);
			currentPoint++;
		}
		logger.info("抢点:grapPoit  end");
	}

	private DiskVO generatorNewDiskVO(String oldDiskSeq, String oldDiskType, String newDiskSeq, Integer num,
			Long enterUserId) {
		DiskVO initDisk = new DiskVO();
		initDisk.setDiskHead(diskService.getDiskHead(oldDiskType));
		initDisk.setDiskTail(diskService.integerToStringFromDiskTail(diskService.getDiskTail(oldDiskType) + num));
		initDisk.setActiveFlag(ActiveFlagEnum.Y.name());
		initDisk.setCreatedBy(enterUserId);
		initDisk.setDeleteFlag(DeleteFlagEnum.N.name());
		initDisk.setDiskCounter(CommonConstant.INIT_DISK_COUNTER);
		initDisk.setDiskParentSeq(oldDiskSeq);
		initDisk.setDiskSeq(newDiskSeq);
		initDisk.setDiskStatus(DiskStatusEnum.RUNNING.name());
		initDisk.setDiskType(oldDiskType);
		initDisk.setLastUpdatedBy(enterUserId);
		return initDisk;
	}

//	private WalletDiskRelationVO generatorFisionDiskRelation(List<WalletDiskRelationVO> diskRelationVOs, int count,
//			String diskSeq, Long diretorUserId, String diretorIdCard, int i, Long lastUpdateBy) {
//		switch (count) {
//		case 1:
//			diskRelationVOs.get(i).setActiveFlag(ActiveFlagEnum.Y.name());
//			diskRelationVOs.get(i).setCreatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setDeleteFlag(DeleteFlagEnum.N.name());
//			diskRelationVOs.get(i).setDiskSeq(diskSeq);
//			diskRelationVOs.get(i).setDiskStatus(DiskStatusEnum.RUNNING.name());
//			diskRelationVOs.get(i).setIdCard(diretorIdCard);
//			diskRelationVOs.get(i).setUserId(diretorUserId);
//			diskRelationVOs.get(i).setLastUpdatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setLocaltion(count);
//			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.DIRECTOR.name())));
//			diskRelationVOs.get(i).setCurrentProfit(BigDecimal.valueOf(0));
//			return diskRelationVOs.get(i);
//		case 2:
//		case 3:
//		case 4:
//			diskRelationVOs.get(i).setActiveFlag(ActiveFlagEnum.Y.name());
//			diskRelationVOs.get(i).setCreatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setDeleteFlag(DeleteFlagEnum.N.name());
//			diskRelationVOs.get(i).setDiskSeq(diskSeq);
//			diskRelationVOs.get(i).setDiskStatus(DiskStatusEnum.RUNNING.name());
//			diskRelationVOs.get(i).setLastUpdatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setLocaltion(count);
//			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name())));
//			diskRelationVOs.get(i).setCurrentProfit(BigDecimal.valueOf(0));
//			return diskRelationVOs.get(i);
//		case 5:
//		case 6:
//		case 7:
//		case 8:
//		case 9:
//		case 10:
//		case 11:
//		case 12:
//		case 13:
//			diskRelationVOs.get(i).setActiveFlag(ActiveFlagEnum.Y.name());
//			diskRelationVOs.get(i).setCreatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setDeleteFlag(DeleteFlagEnum.N.name());
//			diskRelationVOs.get(i).setDiskSeq(diskSeq);
//			diskRelationVOs.get(i).setDiskStatus(DiskStatusEnum.RUNNING.name());
//			diskRelationVOs.get(i).setLastUpdatedBy(lastUpdateBy);
//			diskRelationVOs.get(i).setLocaltion(count);
//			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
//			diskRelationVOs.get(i).setCurrentProfit(BigDecimal.valueOf(0));
//			return diskRelationVOs.get(i);
//		default:
//			return diskRelationVOs.get(i);
//		}
//	}
	private WalletDiskRelationVO generatorFisionDiskRelation(List<WalletDiskRelationVO> diskRelationVOs, int count,
			String diskSeq, Long diretorUserId, String diretorIdCard, int i, Long lastUpdateBy, Integer location) {
		int[] array1 = { 2, 3, 4 }; // 总监
		int[] array2 = { 5, 8, 11 }; // 经理1
		int[] array3 = { 6, 9, 12 }; // 经理2
		int[] array4 = { 7, 10, 13 }; // 经理3
		int[] array5 = { 14, 15, 16 }; // 规划师1
		int[] array6 = { 23, 24, 25 }; // 规划师2
		int[] array7 = { 32, 33, 34 }; // 规划师3
		int[] array8 = { 17, 18, 19 }; // 规划师4
		int[] array9 = { 26, 27, 28 }; // 规划师5
		int[] array10 = { 35, 36, 37 }; // 规划师6
		int[] array11 = { 20, 21, 22 }; // 规划师7
		int[] array12 = { 29, 30, 31 }; // 规划师8
		int[] array13 = { 38, 39, 40 }; // 规划师9
		
		diskRelationVOs.get(i).setActiveFlag(ActiveFlagEnum.Y.name());
		diskRelationVOs.get(i).setCreatedBy(lastUpdateBy);
		diskRelationVOs.get(i).setDeleteFlag(DeleteFlagEnum.N.name());
		diskRelationVOs.get(i).setDiskSeq(diskSeq);
		diskRelationVOs.get(i).setDiskStatus(DiskStatusEnum.RUNNING.name());
		diskRelationVOs.get(i).setLastUpdatedBy(lastUpdateBy);
		diskRelationVOs.get(i).setCurrentProfit(BigDecimal.valueOf(0));
		
		if (ArrayUtils.contains(array1, location)) {
			diskRelationVOs.get(i).setIdCard(diretorIdCard);
			diskRelationVOs.get(i).setUserId(diretorUserId);
			diskRelationVOs.get(i).setActiveFlag(ActiveFlagEnum.Y.name());
			diskRelationVOs.get(i).setLocaltion(1);
		} else if (ArrayUtils.contains(array2, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name())));
			diskRelationVOs.get(i).setLocaltion(2);
		} else if (ArrayUtils.contains(array3, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name())));
			diskRelationVOs.get(i).setLocaltion(3);
		} else if (ArrayUtils.contains(array4, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name())));
			diskRelationVOs.get(i).setLocaltion(4);
		} else if (ArrayUtils.contains(array5, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(5);
		} else if (ArrayUtils.contains(array6, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(6);
		} else if (ArrayUtils.contains(array7, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(7);
		} else if (ArrayUtils.contains(array8, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(8);
		} else if (ArrayUtils.contains(array9, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(9);
		} else if (ArrayUtils.contains(array10, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(10);
		} else if (ArrayUtils.contains(array11, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(11);
		} else if (ArrayUtils.contains(array12, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(12);
		} else if (ArrayUtils.contains(array13, location)) {
			diskRelationVOs.get(i).setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())));
			diskRelationVOs.get(i).setLocaltion(13);
		}
		return diskRelationVOs.get(i);
	}
	/**
	 * 
	 */
	@Override
	public Boolean isExistsMyRecordIndiskType(String diskTypeEnum, Long userId) {
		// 查询 我在该系统中 正在活动的参与身份
		List<DiskUserRelationVO> diskUserRelationVOs = diskDao.getDiskUserRelationExistsRecord(userId, null,
				diskTypeEnum, DiskStatusEnum.RUNNING.name());
		// 如果有记录,则返回true
		if (null != diskUserRelationVOs && diskUserRelationVOs.size() > 0) {
			return true;
		}
		// 如果为空则代表没有没有 正在活动的参与身份,返回false
		return false;
	}

	@Override
	public List<DiskUserRelationVO> isExistsInviteUserIdInDiskType(String diskTypeEnum, Long inviteUserId) {

		List<DiskUserRelationVO> diskUserRelationVOs = new ArrayList<>();

		diskUserRelationVOs = diskDao.getDiskUserRelationExistsRecord(inviteUserId, null, diskTypeEnum,
				DiskStatusEnum.RUNNING.name());

		if (diskUserRelationVOs.size() > 1) {
			logger.error("用户在一个系统中存在多个 身份 " + "");
		}

		return diskUserRelationVOs;
	}

	/*
	 * 正常进入盘 ， 查看有没有邀请人，没有邀请人进入慢盘 有邀请人 查看邀请人在本盘还有没有身份,有身份跟着身份走,没有身份进入快盘
	 */
	@Override
	public void enterDiskType(String diskTypeEnum, Long receiveUserId, String receiveUserIdCard, Boolean isFirst,
			IdcardRelationVO isFromWaitingIdCardVO) throws Exception {
		logger.info("入盘开始 :enterDiskType");
		// 入盘开始
		// -1 表示默认为没有用户
		Long queryReferenceId = userDao.getUserReferenceIdByUserId(receiveUserId);
		// 验证这个邀请人id 是不是存在于 该系统中。
		Boolean flag = StringUtils.isLongEmpty(queryReferenceId);
		if (flag || queryReferenceId.equals(CommonConstant.SYSTEM_USER_ID)) {
			// 没有推荐人盘记录。用户进入慢盘
			DiskVO diskVOSlowest = diskDao.getRunningTheFastestOrTheSlowestDisk2(diskTypeEnum,
					OrderByTypeEnum.ASC.name());
			if (diskVOSlowest == null) {
				return;
			}
			logger.info("进入慢盘");
			// 进入慢盘
			map.put("diskTypeEnum", diskTypeEnum);
			map.put("enterDiskUserId", receiveUserId);
			map.put("enterDiskUserIdCard", receiveUserIdCard);
			map.put("targetDiskVO", diskVOSlowest);
			map.put("isFirst", isFirst);
			logService.writeLog("enterSlowestDisk", map, "Enter slower disk.", receiveUserId);
			map.clear();
			enterSlowestDisk(diskTypeEnum, receiveUserId, receiveUserIdCard, diskVOSlowest, isFirst,
					isFromWaitingIdCardVO);
		} else {
			// 找到推荐人跟着推荐人走 [查询出系统中正在活动的盘用户信息]
			List<WalletDiskRelationVO> diskUserRelationVOs = isExistsUserIdInDiskType(diskTypeEnum, queryReferenceId,
					DiskStatusEnum.RUNNING.name());
			// 判断推荐人在系统中存在正在活动的身份
			if (null == diskUserRelationVOs || diskUserRelationVOs.size() == 0) {
				// 有推荐人，推荐人 在盘中 没有身份
				DiskVO diskVOFastest = diskDao.getRunningTheFastestOrTheSlowestDisk(diskTypeEnum,
						OrderByTypeEnum.DESC.name());
				if (diskVOFastest == null) {
					return;
				}
				logger.info("推荐人不在该系统任何一个盘中,进入快盘");
				map.put("diskTypeEnum", diskTypeEnum);
				map.put("enterDiskUserId", receiveUserId);
				map.put("enterDiskUserIdCard", receiveUserIdCard);
				map.put("targetDiskVO", diskVOFastest);
				map.put("isFirst", isFirst);
				logService.writeLog("enterSlowestDisk", map, "Recommend users do not participate ,Enter Fast disk.",
						receiveUserId);
				map.clear();
				enterSlowestDisk(diskTypeEnum, receiveUserId, receiveUserIdCard, diskVOFastest, isFirst,
						isFromWaitingIdCardVO);
				// 有推荐人,且推荐人在系统中有正在活动的身份,进入推荐人的盘【此处暂时可以写等于1】
			} else if (diskUserRelationVOs.size() == 1) {
				// 跟着推荐人走
				String diskSeq = diskUserRelationVOs.get(0).getDiskSeq();
				// 根据系统类型和盘号查询盘信息
				DiskVO diskVOInvite = diskDao.getByDiskTypeAndDiskSeq(diskTypeEnum, diskSeq);
				if (diskVOInvite == null) {
					return;
				}
				// 进入推荐人的盘
				logger.info("进入推荐人的盘,进入快盘");
				map.put("diskTypeEnum", diskTypeEnum);
				map.put("enterDiskUserId", receiveUserId);
				map.put("enterDiskUserIdCard", receiveUserIdCard);
				map.put("targetDiskVO", diskVOInvite);
				map.put("isFirst", isFirst);
				logService.writeLog("enterSlowestDisk", map, "Enter Recommend users disk.", receiveUserId);
				map.clear();
				enterSlowestDisk(diskTypeEnum, receiveUserId, receiveUserIdCard, diskVOInvite, isFirst,
						isFromWaitingIdCardVO);
			}
		}

		logger.info("入盘结束 :enterDiskType");
	}

	@Override
	public void enterDiskTypeByFassion(String diskTypeEnum, Long receiveUserId, String oldReceiveUserIdCard,
			Boolean isFirst) throws Exception {
		logger.info("enterDiskTypeByFassion start");
		logger.info("enterDiskTypeByFassion param[diskTypeEnum]:" + diskTypeEnum + "param[receiveUserId]:"
				+ receiveUserId + "param[receiveUserIdCard]:" + oldReceiveUserIdCard + "param[isFirst]:" + isFirst);
		// 裂变进来的 ,找的到推荐人 就和推荐人走；找不到推荐人都进入最快的，
		long queryReferenceId = -1;// -1 表示默认为没有用户
		queryReferenceId = userDao.getUserReferenceIdByUserId(receiveUserId);
		// 验证这个邀请人id 是不是存在于 该系统中。
		if (-1 == queryReferenceId || queryReferenceId == CommonConstant.SYSTEM_USER_ID) {
			// 进入快盘
			DiskVO diskVOFastest = diskDao.getRunningTheFastestOrTheSlowestDisk(diskTypeEnum,
					OrderByTypeEnum.DESC.name());
			enterSlowestDisk(diskTypeEnum, receiveUserId, oldReceiveUserIdCard, diskVOFastest, isFirst, null);
		} else {
			List<DiskUserRelationVO> diskUserRelationVOs = isExistsInviteUserIdInDiskType(diskTypeEnum,
					queryReferenceId);
			if (null == diskUserRelationVOs || diskUserRelationVOs.size() == 0) {
				// 进入快盘
				DiskVO diskVOFastest = diskDao.getRunningTheFastestOrTheSlowestDisk(diskTypeEnum,
						OrderByTypeEnum.DESC.name());
				enterSlowestDisk(diskTypeEnum, receiveUserId, oldReceiveUserIdCard, diskVOFastest, isFirst, null);
			} else if (diskUserRelationVOs.size() == 1) {
				// 跟着推荐人走
				String diskSeq = diskUserRelationVOs.get(0).getDiskSeq();
				DiskVO diskVOInvite = diskDao.getByDiskSeq(diskSeq);
				enterSlowestDisk(diskTypeEnum, receiveUserId, oldReceiveUserIdCard, diskVOInvite, isFirst, null);
			}
		}
		logger.info("enterDiskTypeByFassion end");
	}

	@Override
	public void enterSlowestDisk(String diskTypeEnum, Long enterDiskUserId, String enterDiskUserIdCard,
			DiskVO targetDiskVO, Boolean isFirst, IdcardRelationVO isFromWaitingIdCardVO) throws Exception {
		logger.info("enterSlowestDisk start");
		logger.info("enterSlowestDisk param[diskTypeEnum]:" + diskTypeEnum + "param[enterDiskUserId]:" + enterDiskUserId
				+ "param[enterDiskUserIdCard]:" + enterDiskUserIdCard + "param[targetDiskVO]:" + targetDiskVO
				+ "param[isFirst]:" + isFirst);
		IdcardRelationVO idcardRelationVO = new IdcardRelationVO();
		// 如果该入盘是充值 则直接插入idCard 关系表
		if (isFirst) {
			logger.info("更新 idcard 关系表");
			if (isFromWaitingIdCardVO != null) {
				isFromWaitingIdCardVO.setCurrentDiskSeq(targetDiskVO.getDiskSeq());
				walletDiskRelationDao.addIdcardRelationByOne(isFromWaitingIdCardVO);
			} else {
				// 更新 idCard 关系表
				idcardRelationVO = generatorIdcardRelationVO(enterDiskUserId, targetDiskVO.getDiskSeq(),
						CommonConstant.SYSTEM_USER_ID.toString(), enterDiskUserIdCard,
						CommonConstant.SYSTEM_USER_ID.toString(), diskTypeEnum,
						CommonConstant.SYSTEM_USER_ID.toString(), 4L, CommonConstant.SYSTEM_USER_ID, null, null);
				walletDiskRelationDao.addIdcardRelationByOne(idcardRelationVO);
			}
		} else {
			logger.info("重新生成新的idcard 关系表");
			// 如果是裂变进来的,那么要重新生成新的idCard 关系表
			// 要拿到上一个身份关系信息
			List<IdcardRelationVO> idcardRelationVOs = walletDiskRelationDao
					.getIdCardRelationByCurrentIdCard(enterDiskUserId, enterDiskUserIdCard);
			// 如果是裂变进来的,那么当前roleId一定是4 ,上一个roleId一定是1
			idcardRelationVO = generatorIdcardRelationVO(enterDiskUserId, targetDiskVO.getDiskSeq(),
					idcardRelationVOs.get(0).getCurrentDiskSeq(), SeqUtils.generateIdCardSN(),
					idcardRelationVOs.get(0).getCurrentIdCard(), diskTypeEnum,
					idcardRelationVOs.get(0).getCurrentDiskType(), 4L, 1L, null, null);
			// 把老的记录改为不可用
			logger.info("======================================idcardRelationVOs={},Seq={}", idcardRelationVOs,2);
			walletDiskRelationDao.updateAllIdCardRelationStatuxx(null, idcardRelationVOs, "N");
			// 插入新的身份关系信息
			walletDiskRelationDao.addIdcardRelationByOne(idcardRelationVO);
		}
		// 得到 该用户进入该盘的下标
		Integer location = targetDiskVO.getDiskCounter() + 1;
		// 生成盘和用户关系对象,用于插入
		WalletDiskRelationVO walletDiskRelationVO = generatorWalletDiskRelationVO(enterDiskUserId,
				idcardRelationVO.getCurrentIdCard(), targetDiskVO.getDiskSeq(), location, diskTypeEnum);
		// 为入盘用户创建A状态推荐点统计表
		RecommendAPointStatisticsVO aPointStatisticsVO = new RecommendAPointStatisticsVO();
		aPointStatisticsVO.setDiskType(diskTypeEnum);
		aPointStatisticsVO.setUserId(enterDiskUserId);
		aPointStatisticsVO.setReduce('N');
		aPointStatisticsVO.setCreatedBy(enterDiskUserId);
		aPointStatisticsVO.setLastUpdatedBy(enterDiskUserId);
		pointDao.updateAPointStatistics(aPointStatisticsVO);
		// 拿到该用户在系统的A推荐点统计表
		aPointStatisticsVO = pointDao.getUserAPointBydiskType(enterDiskUserId, diskTypeEnum);
		// 为入盘用户创建空点表
		NullPointVO nullPointVO = new NullPointVO();
		nullPointVO.setUserId(enterDiskUserId);
		nullPointVO.setDiskType(diskTypeEnum);
		nullPointVO.setDiskSeq(targetDiskVO.getDiskSeq());
		nullPointVO.setEmptyPoint(3 - (aPointStatisticsVO.getRemainRecommendPoint() / 2));
		nullPointVO.setIdCard(enterDiskUserIdCard);
		nullPointVO.setIsUsable("Y");
		nullPointVO.setCreatedBy(enterDiskUserId);
		nullPointVO.setLastUpdatedBy(enterDiskUserId);
		pointDao.addNullPoint(nullPointVO);
		// 插入 盘用户关系表
		walletDiskRelationDao.insert(walletDiskRelationVO);
		// 盘表中更新盘人数 + 1
		diskDao.increaseDiskCounter(targetDiskVO.getDiskSeq());
		// 把该盘人数实时+1
		targetDiskVO.setDiskCounter(targetDiskVO.getDiskCounter() + 1);
		// 确认入盘(系统) 推荐点 开始分配
		payService.aboutPoint(diskTypeEnum, enterDiskUserId);

		map.put("location", location);
		map.put("diskTypeEnum", diskTypeEnum);
		map.put("diskSeq", targetDiskVO.getDiskSeq());
		map.put("enterDiskUserId", enterDiskUserId);
		map.put("enterDiskIdCard", enterDiskUserIdCard);
		map.put("targetDiskVO", targetDiskVO);
		logService.writeLog("calcProfitAndInsertBatchDiskProfit", map, "Calculate income.", enterDiskUserId);
		map.clear();

		// 计算收益
		calcProfitAndInsertBatchDiskProfit(location, diskTypeEnum, targetDiskVO.getDiskSeq(), enterDiskUserId,
				enterDiskUserIdCard, targetDiskVO);
		// 如果是第40个人,则开始盘分裂
		if (location == CommonConstant.MAX_DISK_COUNTER) {
			logger.info("盘满,分裂");
			map.put("diskTypeEnum", diskTypeEnum);
			map.put("diskSeq", targetDiskVO.getDiskSeq());
			map.put("lastUpdateBy", enterDiskUserId);
			logService.writeLog("finiskedDiskAndRelation", map, "This disk is over.", enterDiskUserId);
			map.clear();
			// 结束盘,结束盘关系表
			finiskedDiskAndRelation(diskTypeEnum, targetDiskVO.getDiskSeq(), enterDiskUserId);
			map.put("diskSeq", targetDiskVO.getDiskSeq());
			map.put("lastUpdateBy", enterDiskUserId);
			map.put("enterDiskIdCard", enterDiskUserIdCard);
			map.put("idcardRelationVO1", idcardRelationVO);
			logService.writeLog("fissionDisk", map, "Disk fission.", enterDiskUserId);
			map.clear();
			// 调用裂变盘
			fissionDisk(targetDiskVO.getDiskSeq(), enterDiskUserId, enterDiskUserIdCard);
		}
		logger.info("enterSlowestDisk end");
	}

	/**
	 * 
	 * @Title: generatorWalletDiskRelationVO
	 * @Description: 生成插入盘关系对象
	 * @param enterDiskUserId
	 * @param enterDiskUserIdCard
	 * @param String
	 * @param location
	 * @return
	 * @return: WalletDiskRelationVO
	 */
	private WalletDiskRelationVO generatorWalletDiskRelationVO(Long enterDiskUserId, String enterDiskUserIdCard,
			String targetDiskSeq, Integer location, String diskTypeEnum) {
		WalletDiskRelationVO walletDiskRelationVO = new WalletDiskRelationVO();
		walletDiskRelationVO.setActiveFlag(ActiveFlagEnum.Y.name());
		walletDiskRelationVO.setCreatedBy(enterDiskUserId);
		walletDiskRelationVO.setDeleteFlag(DeleteFlagEnum.N.name());
		walletDiskRelationVO.setDiskSeq(targetDiskSeq);
		walletDiskRelationVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
		walletDiskRelationVO.setIdCard(enterDiskUserIdCard);
		walletDiskRelationVO.setLastUpdatedBy(enterDiskUserId);
		walletDiskRelationVO.setLocaltion(location);
		walletDiskRelationVO.setRoleId(Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MEMBER.name())));
		walletDiskRelationVO.setDiskType(diskTypeEnum);
		walletDiskRelationVO.setUserId(enterDiskUserId);
		walletDiskRelationVO.setCurrentProfit(BigDecimal.valueOf(0));
		return walletDiskRelationVO;
	}

	private IdcardRelationVO generatorIdcardRelationVO(Long userId, String currentDiskSeq, String lastDiskSeq,
			String currentIdCard, String lastIdCard, String currentDiskType, String lastDiskType, Long currentRoleId,
			Long lastRoleId, Long lastUpdatedBy, Long createdBy) {
		IdcardRelationVO idcardRelationVO = new IdcardRelationVO();
		idcardRelationVO.setCurrentDiskSeq(currentDiskSeq);
		idcardRelationVO.setLastDiskSeq(lastDiskSeq);
		idcardRelationVO.setCurrentDiskType(currentDiskType);
		idcardRelationVO.setLastDiskType(lastDiskType);
		idcardRelationVO.setCurrentIdCard(currentIdCard);
		idcardRelationVO.setLastIdCard(lastIdCard);
		idcardRelationVO.setUserId(userId);
		idcardRelationVO.setCurrentRoleId(currentRoleId);
		idcardRelationVO.setLastRoleId(lastRoleId);
		idcardRelationVO.setStatuxx("Y");
		idcardRelationVO.setLastUpdatedBy(lastUpdatedBy);
		idcardRelationVO.setCreatedBy(createdBy);
		return idcardRelationVO;
	}

	/*
	 * 计算盘相关人收益 盘收益详情表插入记录 盘总收益表插入记录 系统类型收益表插入记录 平台总收益表插入记录 钱包表插入记录
	 */
	@Override
	public Boolean calcProfitAndInsertBatchDiskProfit(Integer location, String diskTypeEnum, String diskSeq,
			Long enterDiskUserId, String enterDiskIdCard, DiskVO targetDiskVO) throws Exception {
		// 找到我的规划师，我的经理，我的总监， 给他们相关收益
		List<DefinedProfitConfigVO> configVOs = ToolsUtil.getProfitConfigFromJson(location);
		DefinedProfitConfigVO definedProfitConfigVO = configVOs.get(0);
		Integer planLocation = Integer.valueOf(definedProfitConfigVO.getProfitManList().get(0));
		Integer managerLocation = Integer.valueOf(definedProfitConfigVO.getProfitManList().get(1));
		Integer directorLocation = Integer.valueOf(definedProfitConfigVO.getProfitManList().get(2));

		// 找出这三个人;分别给他们收益; 总监要查看是否有空点。
		// 获取规划师的盘关系信息
		WalletDiskRelationVO planDiskRel = new WalletDiskRelationVO();
		planDiskRel.setLocaltion(planLocation);
		planDiskRel.setDiskSeq(diskSeq);
		planDiskRel.setDiskType(diskTypeEnum);
		List<WalletDiskRelationVO> planDiskRels = walletDiskRelationDao.selectDiskRelationBySelective(planDiskRel);

		// 获取经理的盘关系信息
		WalletDiskRelationVO managerDiskRel = new WalletDiskRelationVO();
		managerDiskRel.setLocaltion(managerLocation);
		managerDiskRel.setDiskSeq(diskSeq);
		managerDiskRel.setDiskType(diskTypeEnum);
		List<WalletDiskRelationVO> managerDiskRels = walletDiskRelationDao
				.selectDiskRelationBySelective(managerDiskRel);

		// 获取总监的盘关系信息
		WalletDiskRelationVO directorDiskel = new WalletDiskRelationVO();
		directorDiskel.setLocaltion(directorLocation);
		directorDiskel.setDiskSeq(diskSeq);
		directorDiskel.setDiskType(diskTypeEnum);
		// 查看该盘的总监职位的利益分配
		List<PointGrapRelationVO> grapRelationVOs = pointGrapRelationDao.getProfitPointsByDiskSeq(diskSeq);

		map.put("grapRelationVOs", grapRelationVOs);
		map.put("lastUpdatedBy", enterDiskUserId);
		map.put("location", location);
		map.put("diskCounter", targetDiskVO.getDiskCounter());
		logService.writeLog("directorPositionProfitSharing", map,
				"This method is to benefit the distribution of director position.", enterDiskUserId);
		map.clear();
		// 总监职位利益分配
		directorPositionProfitSharing(grapRelationVOs, enterDiskUserId, location, targetDiskVO.getDiskCounter());
		// ===================== 计算出相关金额 Start===============

		List<SingleProfitAmountConfigVO> allSingleProConfig = singleProfitAmountConfigDao.getAllConfig();

		long planRoleId = Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name()));
		long managerRoleId = Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name()));

		// 规划师奖励
		BigDecimal planAwardAmo = null;
		// 经理奖励
		BigDecimal managerAwardAmo = null;

		// 循环取出经理和规划师 的单笔收入
		for (SingleProfitAmountConfigVO singleProfitAmountConfigVO : allSingleProConfig) {
			if (null == singleProfitAmountConfigVO) {
				continue;
			}
			long iteratorRoleId = singleProfitAmountConfigVO.getRoleId();

			if ((iteratorRoleId == planRoleId) && (singleProfitAmountConfigVO.getDiskType().equals(diskTypeEnum))) {
				planAwardAmo = singleProfitAmountConfigVO.getAmount();
			}
			if ((iteratorRoleId == managerRoleId) && (singleProfitAmountConfigVO.getDiskType().equals(diskTypeEnum))) {
				managerAwardAmo = singleProfitAmountConfigVO.getAmount();
			}
		}
		// ===================== 计算出相关金额 End ===================

		// new 三条收益
		Long planUserId = null != planDiskRels && planDiskRels.size() > 0 ? planDiskRels.get(0).getUserId() : null;
		Long managerUserId = null != managerDiskRels && managerDiskRels.size() > 0 ? managerDiskRels.get(0).getUserId()
				: null;
		String targetDiskSeq = targetDiskVO.getDiskSeq();
		String targetDiskType = targetDiskVO.getDiskType();
		Date joinDiskTime = targetDiskVO.getCreatedDate();
		// PLAN
		ProfitDetailVO planProfitDetailVO = generatorProfitDetail(enterDiskUserId, planAwardAmo, planUserId,
				targetDiskSeq, targetDiskType, joinDiskTime, Long.valueOf(RoleEnum.getCodeByName(RoleEnum.PLAN.name())),
				planDiskRels.get(0).getIdCard());
		// MANAGER
		ProfitDetailVO managerProfitDetailVO = generatorProfitDetail(enterDiskUserId, managerAwardAmo, managerUserId,
				targetDiskSeq, targetDiskType, joinDiskTime,
				Long.valueOf(RoleEnum.getCodeByName(RoleEnum.MANAGER.name())), managerDiskRels.get(0).getIdCard());
		// 给规划师收益
		ccalcProfit(location, diskTypeEnum, diskSeq, planAwardAmo, BigDecimal.valueOf(0), planProfitDetailVO, false,
				joinDiskTime, enterDiskUserId);
		// 给经理收益
		ccalcProfit(location, diskTypeEnum, diskSeq, managerAwardAmo, BigDecimal.valueOf(0), managerProfitDetailVO,
				false, joinDiskTime, enterDiskUserId);

		// 给平台增加收益
		// 插入平台收益流水表.
		WalletProfitDetailVO walletProfitDetailVO = new WalletProfitDetailVO();
		walletProfitDetailVO.setUserId(CommonConstant.SYSTEM_USER_ID);
		walletProfitDetailVO.setProfitTargetType(WalletProfitTargetTypeEnum.NORMAL.name());
		RegionUserVO regionUserVO = userDao.getRegionByUserId(enterDiskUserId);
		if (regionUserVO != null) {
			// 入盘用户在地区关系有记录
			String regionCode = "";
			if (regionUserVO.getRegionLevel().equals(3)) {
				regionCode = regionUserVO.getRegionCode();
			} else {
				AreaVO areaVO = certificationDao.getAreaByAreaCode(regionUserVO.getRegionCode());
				regionCode = areaVO.getCityCode();
			}
			// 拿到该用户的所属城市
			CityVO cityVO = certificationDao.getCityByCityCode(regionCode);
			// 先判断市
			if (cityVO != null) {
				// 如果该用户属于市级别,那找到该市的代理人
				AgentRegionVO cityAgentRegionVO = certificationDao.getAgentByRegionCode(cityVO.getCityCode());
				if (cityAgentRegionVO == null) {
					// 如果该市没有代理人
					SystemWalletVO systemWalletVO = new SystemWalletVO();
					systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
					switch (diskTypeEnum) {
					case "TIYAN":
						systemWalletVO
								.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
						walletProfitDetailVO
								.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
						break;
					case "HUIMIN":
						systemWalletVO
								.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
						walletProfitDetailVO
								.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
						break;
					case "FUMIN":
						systemWalletVO
								.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
						walletProfitDetailVO
								.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
						break;
					case "XINGMIN":
						systemWalletVO.setSystemProfitAmount(
								CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
						walletProfitDetailVO
								.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
						break;
					}
					earningDao.addWalletProfitDetailVO(walletProfitDetailVO);
					scoreDao.updateSystemProfitByVO(systemWalletVO);
				} else {
					// 该市有代理人
					BigDecimal cityProfitAmount = BigDecimal.valueOf(0);
					BigDecimal wwhProfitAmount = BigDecimal.valueOf(0);
					if (regionCode.equals(CommonConstant.XIAN_CODE)) {
						switch (diskTypeEnum) {
						case "TIYAN":
							wwhProfitAmount = BigDecimal.valueOf(0.03).multiply(CommonConstant.TIYAN_AMOUNT);
							cityProfitAmount = BigDecimal.valueOf(0.08).multiply(CommonConstant.TIYAN_AMOUNT);
							break;
						case "HUIMIN":
							wwhProfitAmount = BigDecimal.valueOf(0.03).multiply(CommonConstant.HUIMIN_AMOUNT);
							cityProfitAmount = BigDecimal.valueOf(0.08).multiply(CommonConstant.HUIMIN_AMOUNT);
							break;
						case "FUMIN":
							wwhProfitAmount = BigDecimal.valueOf(0.03).multiply(CommonConstant.FUMIN_AMOUNT);
							cityProfitAmount = BigDecimal.valueOf(0.08).multiply(CommonConstant.FUMIN_AMOUNT);
							break;
						case "XINGMIN":
							wwhProfitAmount = BigDecimal.valueOf(0.03).multiply(CommonConstant.XINGMIN_AMOUNT);
							cityProfitAmount = BigDecimal.valueOf(0.08).multiply(CommonConstant.XINGMIN_AMOUNT);
							break;
						}
					} else {
						switch (diskTypeEnum) {
						case "TIYAN":
							wwhProfitAmount = (CommonConstant.ALL_RATIO.subtract(CommonConstant.CITY_RATIO))
									.multiply(CommonConstant.TIYAN_AMOUNT);
							cityProfitAmount = CommonConstant.CITY_RATIO.multiply(CommonConstant.TIYAN_AMOUNT);
							break;
						case "HUIMIN":
							wwhProfitAmount = (CommonConstant.ALL_RATIO.subtract(CommonConstant.CITY_RATIO))
									.multiply(CommonConstant.HUIMIN_AMOUNT);
							cityProfitAmount = CommonConstant.CITY_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT);
							break;
						case "FUMIN":
							wwhProfitAmount = (CommonConstant.ALL_RATIO.subtract(CommonConstant.CITY_RATIO))
									.multiply(CommonConstant.FUMIN_AMOUNT);
							cityProfitAmount = CommonConstant.CITY_RATIO.multiply(CommonConstant.FUMIN_AMOUNT);
							break;
						case "XINGMIN":
							wwhProfitAmount = (CommonConstant.ALL_RATIO.subtract(CommonConstant.CITY_RATIO))
									.multiply(CommonConstant.XINGMIN_AMOUNT);
							cityProfitAmount = CommonConstant.CITY_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT);
							break;
						}
					}
					// 结算市代理
					Long cityAgentId = cityAgentRegionVO.getAgentId();
					AgentProfitVO cityAgentProfitVO = new AgentProfitVO();
					cityAgentProfitVO.setAgentId(cityAgentId);
					cityAgentProfitVO.setTotalAmount(cityProfitAmount);
					cityAgentProfitVO.setRemainAmount(cityProfitAmount);
					cityAgentProfitVO.setUsedAmount(cityProfitAmount);
					cityAgentProfitVO.setCreatedBy(enterDiskUserId);
					cityAgentProfitVO.setLastUpdatedBy(enterDiskUserId);
					earningDao.updateAgentProfitVOByAgentId(cityAgentProfitVO);
					AgentProfitDetailVO cityAgentProfitDetailVO = new AgentProfitDetailVO();
					cityAgentProfitDetailVO.setAgentId(cityAgentId);
					cityAgentProfitDetailVO.setProfitAmount(cityProfitAmount);
					cityAgentProfitDetailVO.setProfitStatus("Y");
					cityAgentProfitDetailVO.setProfitId(SeqUtils.generateAgentProfitDetailId());
					cityAgentProfitDetailVO.setCreatedBy(enterDiskUserId);
					cityAgentProfitDetailVO.setLastUpdatedBy(enterDiskUserId);
					earningDao.addAgentProfitDetailVO(cityAgentProfitDetailVO);
					// 剩余全部给平台
					SystemWalletVO systemWalletVO = new SystemWalletVO();
					systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
					systemWalletVO.setSystemProfitAmount(wwhProfitAmount);
					walletProfitDetailVO.setProfitAmount(wwhProfitAmount);
					earningDao.addWalletProfitDetailVO(walletProfitDetailVO);
					scoreDao.updateSystemProfitByVO(systemWalletVO);
				}
			} else {
				// 该用户所属地区是省级别,11%全部给平台
				SystemWalletVO systemWalletVO = new SystemWalletVO();
				systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
				switch (diskTypeEnum) {
				case "TIYAN":
					systemWalletVO
							.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
					walletProfitDetailVO
							.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
					break;
				case "HUIMIN":
					systemWalletVO
							.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
					walletProfitDetailVO
							.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
					break;
				case "FUMIN":
					systemWalletVO
							.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
					walletProfitDetailVO
							.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
					break;
				case "XINGMIN":
					systemWalletVO
							.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
					walletProfitDetailVO
							.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
					break;
				}
				earningDao.addWalletProfitDetailVO(walletProfitDetailVO);
				scoreDao.updateSystemProfitByVO(systemWalletVO);
			}
		} else {
			// 入盘用户在地区表没有任何记录, 11%全部给平台
			SystemWalletVO systemWalletVO = new SystemWalletVO();
			systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
			switch (diskTypeEnum) {
			case "TIYAN":
				systemWalletVO.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
				walletProfitDetailVO.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.TIYAN_AMOUNT));
				break;
			case "HUIMIN":
				systemWalletVO.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
				walletProfitDetailVO.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.HUIMIN_AMOUNT));
				break;
			case "FUMIN":
				systemWalletVO.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
				walletProfitDetailVO.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.FUMIN_AMOUNT));
				break;
			case "XINGMIN":
				systemWalletVO.setSystemProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
				walletProfitDetailVO.setProfitAmount(CommonConstant.ALL_RATIO.multiply(CommonConstant.XINGMIN_AMOUNT));
				break;
			}
			earningDao.addWalletProfitDetailVO(walletProfitDetailVO);
			scoreDao.updateSystemProfitByVO(systemWalletVO);
		}
		return true;
	}

	/**
	 * 
	 * @Title: ccalcProfit
	 * @Description: 计算收益
	 * @param location
	 * @param diskTypeEnum
	 * @param diskSeq
	 * @param awardAmo
	 * @param saveAmount
	 * @param profitDetailVO
	 * @param isDirector
	 * @param joinDiskTime
	 * @param enterDiskUserId
	 * @return: void
	 */
	private void ccalcProfit(Integer location, String diskTypeEnum, String diskSeq, BigDecimal awardAmo,
			BigDecimal saveAmount, ProfitDetailVO profitDetailVO, boolean isDirector, Date joinDiskTime,
			Long enterDiskUserId) {
		//以前有坑，现在要查询到这个用户初始进入系统的会员idcard
		IdcardRelationVO sidcardRelationVO=new IdcardRelationVO();
		sidcardRelationVO.setCurrentDiskType(profitDetailVO.getDiskType());
		sidcardRelationVO.setCurrentIdCard(profitDetailVO.getIdCard());
		sidcardRelationVO.setUserId(profitDetailVO.getProfitUserId());
		IdcardRelationVO idcardRelationVO=walletDiskRelationDao.getStartIdCardRelationByInfo(sidcardRelationVO);
		//定返控制，定返已返总金额加此次收益是否已经超过定返总额，超过则直接补满总额
		FixedRebateVO ofixedRebateVO=fixedRebateDao.getRebateInfoByIdCard(idcardRelationVO.getCurrentIdCard());
		BigDecimal finallyFixedRebate=new BigDecimal(0);
		int resultFixedRebate=-1;
		if(null!=ofixedRebateVO){
			//已定返额加收益与总额差额
			resultFixedRebate=ofixedRebateVO.getReturnedAmount().add(profitDetailVO.getProfitAmount())
					.compareTo(ofixedRebateVO.getTotalAmount());
			//定返总金额与已返总金额差额
			finallyFixedRebate=ofixedRebateVO.getTotalAmount().subtract(ofixedRebateVO.getReturnedAmount());
		}
		if(resultFixedRebate<0){
			// 计算收益详细表 wallet_profit_detail_****_t
			// 计算盘收益,用于收益 wallet_disk_profit_t
			walletProfitDetailDao.insertProfitDetail(profitDetailVO);
			ccalcDiskProdiUseProfit(location, diskTypeEnum, diskSeq, awardAmo, profitDetailVO, joinDiskTime);
			// 计算盘类型收益
			ccalcDiskTypeProfit(diskTypeEnum, awardAmo, saveAmount, profitDetailVO, isDirector, enterDiskUserId);
			// 计算平台总收益表
			ccalcPlatformProfit(diskTypeEnum, awardAmo, saveAmount, profitDetailVO);
			// 修改钱包 正常
			WalletAmountVO walletAmountVO = new WalletAmountVO();
			walletAmountVO.setUserId(profitDetailVO.getProfitUserId());
			walletAmountVO.setPlatformTotalAmount(awardAmo.add(saveAmount));
			walletAmountVO.setCreatedBy(enterDiskUserId);
			walletAmountVO.setLastUpdatedBy(enterDiskUserId);
			walletAmountDao.updateByUserIdSelective(walletAmountVO);
			// 修改对应盘的关系表 字段, 当前收益
			DiskRelationVO diskRelationVO = new DiskRelationVO();
			diskRelationVO.setDiskType(diskTypeEnum);
			diskRelationVO.setIdCard(profitDetailVO.getIdCard());
			diskRelationVO.setCurrentProfit(awardAmo.add(saveAmount));
			diskRelationVO.setCreatedBy(enterDiskUserId);
			diskRelationVO.setLastUpdatedBy(enterDiskUserId);
			diskDao.updateUserCurrentProfit(diskRelationVO);
			if(null!=ofixedRebateVO){
				//更新定返统计表
				ofixedRebateVO.setReturnedAmount(awardAmo.add(saveAmount));
				fixedRebateDao.updateRebateInfo(ofixedRebateVO);
			}
			// 有盘位收益时，在定返收益详情表插入记录 wallet_platform_fixedrebate_detail_t
			FixedRebateVO fixedRebateVO = new FixedRebateVO();
			fixedRebateVO.setUserId(profitDetailVO.getProfitUserId());
			fixedRebateVO.setIdCard(profitDetailVO.getIdCard());
			fixedRebateVO.setProfitAmount(awardAmo.add(saveAmount));
			fixedRebateVO.setProfitType(FixedRebateTypeEnum.PW.name());
			fixedRebateVO.setSysType(profitDetailVO.getDiskType());
			fixedRebateVO.setCreatedBy(enterDiskUserId);
			fixedRebateVO.setLastUpdatedBy(enterDiskUserId);
			fixedRebateDao.insertRebateDetailInfo(fixedRebateVO);
		}else{
			// 计算收益详细表 wallet_profit_detail_****_t
			// 计算盘收益,用于收益 wallet_disk_profit_t
			//若超过补定返的差额
			profitDetailVO.setProfitAmount(finallyFixedRebate);
			walletProfitDetailDao.insertProfitDetail(profitDetailVO);
			ccalcDiskProdiUseProfit(location, diskTypeEnum, diskSeq, finallyFixedRebate, profitDetailVO, joinDiskTime);
			// 计算盘类型收益
			ccalcDiskTypeProfit(diskTypeEnum, finallyFixedRebate, saveAmount, profitDetailVO, isDirector, enterDiskUserId);
			// 计算平台总收益表
			ccalcPlatformProfit(diskTypeEnum, finallyFixedRebate, saveAmount, profitDetailVO);
			// 修改钱包 正常
			WalletAmountVO walletAmountVO = new WalletAmountVO();
			walletAmountVO.setUserId(profitDetailVO.getProfitUserId());
			walletAmountVO.setPlatformTotalAmount(finallyFixedRebate.add(saveAmount));
			walletAmountVO.setCreatedBy(enterDiskUserId);
			walletAmountVO.setLastUpdatedBy(enterDiskUserId);
			walletAmountDao.updateByUserIdSelective(walletAmountVO);
			// 修改对应盘的关系表 字段, 当前收益
			DiskRelationVO diskRelationVO = new DiskRelationVO();
			diskRelationVO.setDiskType(diskTypeEnum);
			diskRelationVO.setIdCard(profitDetailVO.getIdCard());
			diskRelationVO.setCurrentProfit(finallyFixedRebate.add(saveAmount));
			diskRelationVO.setCreatedBy(enterDiskUserId);
			diskRelationVO.setLastUpdatedBy(enterDiskUserId);
			diskDao.updateUserCurrentProfit(diskRelationVO);
			//已经返满，更新定返统计表,将此定状态finish掉
			ofixedRebateVO.setReturnedAmount(finallyFixedRebate);
			ofixedRebateVO.setRebateStauts(FixedRebateEnum.FINISHED.name());
			fixedRebateDao.updateRebateInfo(ofixedRebateVO);
			// 有盘位收益时，在定返收益详情表插入记录 wallet_platform_fixedrebate_detail_t
			FixedRebateVO fixedRebateVO = new FixedRebateVO();
			fixedRebateVO.setUserId(profitDetailVO.getProfitUserId());
			fixedRebateVO.setIdCard(profitDetailVO.getIdCard());
			fixedRebateVO.setProfitAmount(finallyFixedRebate.add(saveAmount));
			fixedRebateVO.setProfitType(FixedRebateTypeEnum.PW.name());
			fixedRebateVO.setSysType(profitDetailVO.getDiskType());
			fixedRebateVO.setCreatedBy(enterDiskUserId);
			fixedRebateVO.setLastUpdatedBy(enterDiskUserId);
			fixedRebateDao.insertRebateDetailInfo(fixedRebateVO);
		}
	}

	/**
	 * 
	 * @Title: ccalcPlatformProfit
	 * @Description: 计算平台总收益表
	 * @param diskTypeEnum
	 * @param planAwardAmo
	 * @param planProfitDetailVO
	 * @return: void
	 */
	private void ccalcPlatformProfit(String diskTypeEnum, BigDecimal planAwardAmo, BigDecimal saveAmount,
			ProfitDetailVO planProfitDetailVO) {

		PlatformProfitVO platformProfitVO = new PlatformProfitVO();
		platformProfitVO.setActiveFlag(ActiveFlagEnum.Y.name());
		platformProfitVO.setCreatedBy(planProfitDetailVO.getPayUserId());
		platformProfitVO.setDeleteFlag(DeleteFlagEnum.N.name());
		platformProfitVO.setLastUpdatedBy(planProfitDetailVO.getPayUserId());
		platformProfitVO.setUserId(planProfitDetailVO.getProfitUserId());
		if (DiskEnum.TIYAN.name().equals(diskTypeEnum)) {
			platformProfitVO.setTiyanDiskProfitAmount(planAwardAmo);
			platformProfitVO.setTiyanWithdrawalsAmount(saveAmount.add(planAwardAmo));
			platformProfitVO.setTiyanSaveAmount(saveAmount);
		} else if (DiskEnum.HUIMIN.name().equals(diskTypeEnum)) {
			platformProfitVO.setHuiminDiskProfitAmount(planAwardAmo);
			platformProfitVO.setHuiminSaveAmount(saveAmount);
			platformProfitVO.setHuiminWithdrawalsAmount(saveAmount.add(planAwardAmo));
		} else if (DiskEnum.FUMIN.name().equals(diskTypeEnum)) {
			platformProfitVO.setFuminDiskProfitAmount(planAwardAmo);
			platformProfitVO.setFuminSaveAmount(saveAmount);
			platformProfitVO.setFuminWithdrawalsAmount(saveAmount.add(planAwardAmo));
		} else if (DiskEnum.XINGMIN.name().equals(diskTypeEnum)) {
			platformProfitVO.setXingminDiskProfitAmount(planAwardAmo);
			platformProfitVO.setXingminSaveAmount(saveAmount);
			platformProfitVO.setXingminWithdrawalsAmount(saveAmount.add(planAwardAmo));
		}

		platformProfitVO.setPlatformRemainAmount(saveAmount.add(planAwardAmo));
		platformProfitVO.setPlatformTotalAmount(saveAmount.add(planAwardAmo));
		platformProfitVO.setSaveAmount(saveAmount);
		platformProfitVO.setMemberRemainAmount(saveAmount.add(planAwardAmo));
		platformProfitVO.setMemberTotalAmount(saveAmount.add(planAwardAmo));
		platformProfitVO.setPlatformWithdrawalsAmount(planAwardAmo);
		platformProfitVO.setMemberWithdrawalsAmount(saveAmount.add(planAwardAmo));

		platformProfitDao.updatePlatformProfitByUserId(platformProfitVO);

	}

	/**
	 * 
	 * @Title: ccalcDiskTypeProfit
	 * @Description: 计算盘类型收益
	 * @param diskTypeEnum
	 * @param planAwardAmo
	 * @param planProfitDetailVO
	 * @return: void
	 */
	private void ccalcDiskTypeProfit(String diskTypeEnum, BigDecimal planAwardAmo, BigDecimal saveAmount,
			ProfitDetailVO planProfitDetailVO, Boolean isDirector, Long lastUpdatedBy) {

		DiskTypeProfitVO diskTypeProfitVO = new DiskTypeProfitVO();
		diskTypeProfitVO.setActiveFlag(ActiveFlagEnum.Y.name());
		diskTypeProfitVO.setCreatedBy(lastUpdatedBy);
		diskTypeProfitVO.setDeleteFlag(DeleteFlagEnum.N.name());
		if (isDirector && null != planAwardAmo && null != saveAmount) {
			diskTypeProfitVO
					.setDiskProfitAmount(BigDecimal.valueOf(planAwardAmo.doubleValue() + saveAmount.doubleValue()));
		} else {
			diskTypeProfitVO.setDiskProfitAmount(planAwardAmo);
		}
		diskTypeProfitVO.setDiskType(diskTypeEnum);
		diskTypeProfitVO.setLastUpdatedBy(lastUpdatedBy);
		diskTypeProfitVO.setUserId(planProfitDetailVO.getProfitUserId());
		diskTypeProfitDao.updateDiskTypeProfitByUserId(diskTypeProfitVO);

	}

	/**
	 * 
	 * @Title: ccalcDiskProdiUseProfit
	 * @Description: 计算盘收益,用于收益
	 * @param location
	 * @param diskTypeEnum
	 * @param diskSeq
	 * @param planAwardAmo
	 * @param planProfitDetailVO
	 * @param isDirector
	 *            true 表示 总监 false 表示 非总监，即规划师， 或经理
	 * @return: void
	 */
	private void ccalcDiskProdiUseProfit(Integer location, String diskTypeEnum, String diskSeq, BigDecimal planAwardAmo,
			ProfitDetailVO planProfitDetailVO, Date joinDiskTime) {

		// 下面一系列操作是 对盘总收益表进行数据更新
		// 可以使用 唯一索引进行数据操作
		DiskProfitVO diskProfitVO = new DiskProfitVO();
		diskProfitVO.setActiveFlag(ActiveFlagEnum.Y.name());
		diskProfitVO.setCreatedBy(planProfitDetailVO.getPayUserId());
		diskProfitVO.setDeleteFlag(DeleteFlagEnum.N.name());
		// 这里要获取当前盘人数
		DiskVO diskVO = diskDao.getByDiskSeq(diskSeq);
		diskProfitVO.setDiskCounter(diskVO.getDiskCounter());// 人数
		diskProfitVO.setDiskProfitAmount(planAwardAmo);// 累加
		diskProfitVO.setDiskSeq(diskSeq);
		// 判断是否是第40个人进来了,如果是第40个人,则把盘状态改为结束
		if (location == 40) {
			diskProfitVO.setDiskStatus(DiskStatusEnum.FINISHED.name());
			// 将所有的人盘收益记录表 中的对应记录 盘状态改为 FINISHED 和 盘人数改为40
			walletProfitDetailDao.updateDiskStatus(diskSeq, DiskStatusEnum.FINISHED.name(), 40);
		} else {
			diskProfitVO.setDiskStatus(DiskStatusEnum.RUNNING.name());
			walletProfitDetailDao.updateDiskStatus(diskSeq, null, diskVO.getDiskCounter());
		}
		diskProfitVO.setDiskType(diskTypeEnum);
		diskProfitVO.setIdCard(planProfitDetailVO.getIdCard());
		diskProfitVO.setJoinDiskTime(joinDiskTime);
		diskProfitVO.setLastUpdatedBy(planProfitDetailVO.getPayUserId());
		diskProfitVO.setRoleId(planProfitDetailVO.getRoleId());

		diskProfitVO.setSaveAmount(BigDecimal.valueOf(0));

		diskProfitVO.setUserId(planProfitDetailVO.getProfitUserId());
		diskProfitVO.setWithdrawalsAmount(planAwardAmo);// 累加

		walletProfitDetailDao.increaseDiskProfit(diskProfitVO);

	}

	private ProfitDetailVO generatorProfitDetail(Long enterDiskUserId, BigDecimal awardAmo, Long planUserId,
			String targetDiskSeq, String targetDiskType, Date joinDiskTime, Long roleId, String idCard) {
		ProfitDetailVO profitDetailVO = new ProfitDetailVO();
		profitDetailVO.setActiveFlag(ActiveFlagEnum.Y.name());
		profitDetailVO.setCreatedBy(enterDiskUserId);
		profitDetailVO.setDeleteFlag(DeleteFlagEnum.N.name());
		profitDetailVO.setDiskSeq(targetDiskSeq);
		profitDetailVO.setDiskType(targetDiskType);
		profitDetailVO.setJoinDiskTime(joinDiskTime);
		profitDetailVO.setLastUpdatedBy(enterDiskUserId);
		profitDetailVO.setPayUserId(enterDiskUserId);
		profitDetailVO.setProfitUserId(planUserId);
		profitDetailVO.setProfitAmount(awardAmo);
		profitDetailVO.setRoleId(roleId);
		profitDetailVO.setSaveAmount(BigDecimal.valueOf(0));
		profitDetailVO.setWithdrawalsAmount(awardAmo);
		profitDetailVO.setIdCard(idCard);
		profitDetailVO.setProfitTargetType(ProfitTargetTypeEnum.MEMBER.name());

		return profitDetailVO;
	}

	@Override
	public Boolean finiskedDiskAndRelation(String diskTypeEnum, String diskSeq, Long lastUpdateBy) {
		logger.info("盘满,结束老盘");
		// 结束这个盘
		// 盘表结束
		diskDao.diskFinished(diskTypeEnum, diskSeq, lastUpdateBy);
		// 盘用户关系表结束
		walletDiskRelationDao.diskRelationFinished(diskTypeEnum, diskSeq, lastUpdateBy);
		return true;
	}

	@Override
	public void tandemService(String diskTypeEnum, Long receiveUserId, String receiveUserIdCard,
			IdcardRelationVO isFromWaitingIdCardVO) throws Exception {
		logger.info("判断是否可以入盘 开始 :tandemService");
		// 根据我在系统中是否还有身份 , 判断我是进入对应系统还是等待
		boolean flag = isExistsMyRecordIndiskType(diskTypeEnum, receiveUserId);
		logger.info("tandemService flag:" + flag);
		if (!flag) {
			logger.info("可以入盘");
			map.put("diskTypeEnum", diskTypeEnum);
			map.put("receiveUserId", receiveUserId);
			map.put("receiveUserIdCard", receiveUserIdCard);
			logService.writeLog("enterDiskType", map, "Enter disk method step 2.", receiveUserId);
			map.clear();

			// 在对应系统中没有正在活动的身份,可以进入系统(使用新入盘方式)
			//enterDiskType(diskTypeEnum, receiveUserId, receiveUserIdCard, true, isFromWaitingIdCardVO);
			//使用新入盘方法
			stapDiskEnterService.enterOverIdentityDisk(diskTypeEnum, receiveUserId, receiveUserIdCard, true, isFromWaitingIdCardVO);
			
			map.put("diskTypeEnum", diskTypeEnum);
			map.put("receiveUserId", receiveUserId);
			map.put("receiveUserIdCard", receiveUserIdCard);
			map.put("waittingType", WaittingTypeEnum.FINISHED.name());
			logService.writeLog("enterDiskType", map, "Removal of IDcard from waitting.", receiveUserId);
			map.clear();
			// 把该充值身份在等待盘中消除
			updateWaittingDisk(diskTypeEnum, receiveUserId, receiveUserIdCard, WaittingTypeEnum.FINISHED.name());
			// 先判断是否开启定返入盘成功后该身份的定返开启
			if (fixedRebateService.getRebateConfig(CommonConstant.ONOFF_DISKFIXEDREBATE)) {
				logger.info("入盘成功,开始定返");
				FixedRebateVO fixedRebateVo = new FixedRebateVO();
				// 入盘成功更新定返状态，开始定返。
				fixedRebateVo.setUserId(receiveUserId);
				fixedRebateVo.setIdCard(receiveUserIdCard);
				fixedRebateVo.setRebateStauts(FixedRebateEnum.RUNNING.name());// 开始定返
				fixedRebateDao.updateRebateInfo(fixedRebateVo);
			}
		} else {
			// 在对应系统中有正在活动的身份,1.0版本中只能等待
			logger.info("入盘失败,进入等待表");
		}
		logger.info("判断是否可以入盘 结束 :tandemService");
	}

	@Override
	public List<WalletDiskRelationVO> isExistsUserIdInDiskType(String diskTypeEnum, Long userId, String diskStatus)
			throws Exception {
		WalletDiskRelationVO walletDiskRelationVO = new WalletDiskRelationVO();
		walletDiskRelationVO.setDiskStatus(diskStatus);
		walletDiskRelationVO.setDiskType(diskTypeEnum);
		walletDiskRelationVO.setUserId(userId);

		List<WalletDiskRelationVO> list = new ArrayList<>();
		// 查询出我的推荐人 在 该系统中的所有正在活动的 身份的信息
		list = walletDiskRelationDao.selectDiskRelationBySelective(walletDiskRelationVO);
		return list;
	}

	@Override
	public void updateWaittingDisk(String diskTypeEnum, Long receiveUserId, String receiveUserIdCard,
			String waittingStatus) throws Exception {
		logger.info("updateWaittingDisk start" + waittingStatus);
		WalletDiskWaittingVO walletDiskWaittingVO = new WalletDiskWaittingVO();
		walletDiskWaittingVO.setActiveFlag(ActiveFlagEnum.Y.name());
		walletDiskWaittingVO.setLastIdCard(receiveUserIdCard);
		walletDiskWaittingVO.setCreatedBy(receiveUserId);
		walletDiskWaittingVO.setDeleteFlag(DeleteFlagEnum.N.name());
		walletDiskWaittingVO.setDiskType(diskTypeEnum);
		walletDiskWaittingVO.setLastUpdatedBy(receiveUserId);
		walletDiskWaittingVO.setPayUserId(receiveUserId);
		walletDiskWaittingVO.setPayIdCard(SeqUtils.generateIdCardSN());
		walletDiskWaittingVO.setWaittingStatus(waittingStatus);
		walletDiskWaittingDao.insert(walletDiskWaittingVO);
		logger.info("enterWaittingDisk end" + waittingStatus);
	}

	/**
	 * 
	 * @Title: directorPositionProfitSharing
	 * @Description: 总监职位利益分配
	 * @param grapRelationVOs
	 *            盘总监职位利益分配集合
	 * @param lastUpdatedBy
	 *            谁进入盘
	 * @param location
	 *            在盘中的位置
	 * @param diskCounter
	 *            该盘当前人数
	 * @return: void
	 */
	public void directorPositionProfitSharing(List<PointGrapRelationVO> grapRelationVOs, Long lastUpdatedBy,
			Integer location, Integer diskCounter) {
		logger.info("directorPositionProfitSharing start");
		// 读取职位奖励配置， 可提现储备金配置
		List<DirectorConfigVO> allWithReser = withdrawReserveConfigDao.getAllConfig();
		String diskStatus = DiskStatusEnum.FINISHED.name();
		if (location < 40) {
			diskStatus = DiskStatusEnum.RUNNING.name();
		}
		Integer sysProfit = 3;
		BigDecimal remainAmount = new BigDecimal(0);
		for (PointGrapRelationVO pointGrapRelationVO : grapRelationVOs) {
			sysProfit -= pointGrapRelationVO.getProfitPoints(); // 获取到总监的真实收益点数
			// 查询idcardrelation信息
			IdcardRelationVO idcardRelationVO = walletDiskRelationDao
					.getIdCardRelationByCurrentIdCard(null, pointGrapRelationVO.getIdCard()).get(0);
			BigDecimal allProfitAmount = new BigDecimal(0); // 总收益
			BigDecimal oneProfitAmount = new BigDecimal(0); // 得到空一个点的时候可提现和储备金的总和
			BigDecimal saveGoldAmount = new BigDecimal(0); // 储备金
			BigDecimal withdrawAmount = new BigDecimal(0); // 可提现金额
			String diskType = pointGrapRelationVO.getDiskType();
			for (DirectorConfigVO directorConfigVO : allWithReser) {
				if (directorConfigVO.getGetEmptyPoints() == 1
						&& directorConfigVO.getDiskType().equals(pointGrapRelationVO.getDiskType())) {
					oneProfitAmount = directorConfigVO.getCanWithdrawAmount().add(directorConfigVO.getReserveAmount());
				}
				if (directorConfigVO.getGetEmptyPoints() == pointGrapRelationVO.getProfitPoints()
						&& directorConfigVO.getDiskType().equals(pointGrapRelationVO.getDiskType())) {
					allProfitAmount = directorConfigVO.getCanWithdrawAmount().add(directorConfigVO.getReserveAmount());
					saveGoldAmount = directorConfigVO.getReserveAmount();
					withdrawAmount = directorConfigVO.getCanWithdrawAmount();
					break;
				}
			}
				if (!pointGrapRelationVO.getIsDirector().equals("Y")) {
					// 1. 抢点的收益详情表 wallet_grap_profit_detail_t
					ProfitDetailVO profitDetailVO = new ProfitDetailVO();
					profitDetailVO.setProfitUserId(idcardRelationVO.getUserId());
					profitDetailVO.setProfitAmount(allProfitAmount);
					profitDetailVO.setProfitTargetType(ProfitTargetTypeEnum.MEMBER.name());
					profitDetailVO.setDiskSeq(pointGrapRelationVO.getDiskSeq());
					profitDetailVO.setIdCard(pointGrapRelationVO.getIdCard());
					profitDetailVO.setPayUserId(lastUpdatedBy);
					profitDetailVO.setDiskType(pointGrapRelationVO.getDiskType());
					profitDetailVO.setCreatedBy(lastUpdatedBy);
					profitDetailVO.setLastUpdatedBy(lastUpdatedBy);
					walletProfitDetailDao.insertProfitDetail(profitDetailVO);
					// 定返功能，抢点收益存入
					// 有盘位收益时，在定返收益详情表插入记录 wallet_platform_fixedrebate_detail_t
					FixedRebateVO fixedRebateVO = new FixedRebateVO();
					fixedRebateVO.setUserId(idcardRelationVO.getUserId());
					fixedRebateVO.setIdCard(pointGrapRelationVO.getIdCard());
					fixedRebateVO.setProfitAmount(allProfitAmount);
					fixedRebateVO.setProfitType(FixedRebateTypeEnum.QD.name());
					fixedRebateVO.setSysType(pointGrapRelationVO.getDiskType());
					fixedRebateVO.setCreatedBy(lastUpdatedBy);
					fixedRebateVO.setLastUpdatedBy(lastUpdatedBy);
					fixedRebateDao.insertRebateDetailInfo(fixedRebateVO);
					// 2. 钱包表 wallet_amount_t
					WalletAmountVO walletAmountVO = new WalletAmountVO();
					walletAmountVO.setUserId(idcardRelationVO.getUserId());
					walletAmountVO.setPlatformTotalAmount(profitDetailVO.getProfitAmount());
					walletAmountVO.setCreatedBy(lastUpdatedBy);
					walletAmountVO.setLastUpdatedBy(lastUpdatedBy);
					walletAmountDao.updateByUserIdSelective(walletAmountVO);
					// 3. 平台总收益表 wallet_platform_profit_t
					PlatformProfitVO platformProfitVO = new PlatformProfitVO();
					platformProfitVO.setUserId(idcardRelationVO.getUserId());
					platformProfitVO.setPlatformTotalAmount(allProfitAmount);
					platformProfitVO.setPlatformWithdrawalsAmount(allProfitAmount);
					platformProfitVO.setPlatformRemainAmount(allProfitAmount);
					platformProfitVO.setMemberTotalAmount(allProfitAmount);
					platformProfitVO.setMemberWithdrawalsAmount(allProfitAmount);
					platformProfitVO.setMemberRemainAmount(allProfitAmount);
					platformProfitVO.setPlatformGrapTotalAmount(allProfitAmount);
					switch (diskType) {
					case "TIYAN":
						platformProfitVO.setTiyanGrapAmount(allProfitAmount);
						platformProfitVO.setTiyanWithdrawalsAmount(allProfitAmount);
						break;
					case "HUIMIN":
						platformProfitVO.setHuiminGrapAmount(allProfitAmount);
						platformProfitVO.setHuiminWithdrawalsAmount(allProfitAmount);
						break;
					case "FUMIN":
						platformProfitVO.setFuminGrapAmount(allProfitAmount);
						platformProfitVO.setFuminWithdrawalsAmount(allProfitAmount);
						break;
					case "XINGMIN":
						platformProfitVO.setXingminGrapAmount(allProfitAmount);
						platformProfitVO.setXingminWithdrawalsAmount(allProfitAmount);
						break;
					default:
						logger.error("directorPositionProfitSharing error!");
					}
					platformProfitVO.setCreatedBy(lastUpdatedBy);
					platformProfitVO.setLastUpdatedBy(lastUpdatedBy);
					// 把平台总收益表插入
					platformProfitDao.updatePlatformProfitByUserId(platformProfitVO);
					// 4. 系统类型收益表 wallet_disk_type_profit_t
					DiskTypeProfitVO diskTypeProfitVO = new DiskTypeProfitVO();
					diskTypeProfitVO.setUserId(idcardRelationVO.getUserId());
					diskTypeProfitVO.setDiskProfitAmount(allProfitAmount);
					diskTypeProfitVO.setDiskType(diskType);
					diskTypeProfitVO.setCreatedBy(lastUpdatedBy);
					diskTypeProfitVO.setLastUpdatedBy(lastUpdatedBy);
					diskTypeProfitDao.updateDiskTypeProfitByUserId(diskTypeProfitVO);
					// 5. 盘收益表更新 wallet_disk_profit_t
					DiskProfitVO diskProfitVO = new DiskProfitVO();
					diskProfitVO.setUserId(idcardRelationVO.getUserId());
					diskProfitVO.setDiskSeq(pointGrapRelationVO.getDiskSeq());
					diskProfitVO.setDiskType(diskType);
					diskProfitVO.setWithdrawalsAmount(withdrawAmount);
					diskProfitVO.setSaveAmount(saveGoldAmount);
					diskProfitVO.setRoleId(idcardRelationVO.getCurrentRoleId());
					diskProfitVO.setDiskCounter(diskCounter);
					diskProfitVO.setJoinDiskTime(pointGrapRelationVO.getCreatedDate());
					diskProfitVO.setIdCard(pointGrapRelationVO.getIdCard());
					diskProfitVO.setDiskStatus(diskStatus);
					diskProfitVO.setCreatedBy(lastUpdatedBy);
					diskProfitVO.setLastUpdatedBy(lastUpdatedBy);
					diskProfitVO.setIsThisDisk("N");
					walletProfitDetailDao.increaseDiskProfit(diskProfitVO);
				} else {
					// 开始更新收益,总共要更新6个表
					// 1. 对应系统的收益详情表 wallet_profit_detail_****_t
					ProfitDetailVO profitDetailVO = new ProfitDetailVO();
					profitDetailVO.setProfitUserId(idcardRelationVO.getUserId());
					profitDetailVO.setProfitAmount(allProfitAmount);
					profitDetailVO.setSaveAmount(saveGoldAmount);
					profitDetailVO.setWithdrawalsAmount(withdrawAmount);
					profitDetailVO.setProfitTargetType(ProfitTargetTypeEnum.MEMBER.name());
					profitDetailVO.setDiskSeq(pointGrapRelationVO.getDiskSeq());
					profitDetailVO.setIdCard(pointGrapRelationVO.getIdCard());
					profitDetailVO.setPayUserId(lastUpdatedBy);
					profitDetailVO.setJoinDiskTime(pointGrapRelationVO.getCreatedDate());
					// 总监 的roleId是1
					profitDetailVO.setRoleId(1L);
					profitDetailVO.setDiskType(pointGrapRelationVO.getDiskType());
					profitDetailVO.setCreatedBy(lastUpdatedBy);
					profitDetailVO.setLastUpdatedBy(lastUpdatedBy);
					walletProfitDetailDao.insertProfitDetail(profitDetailVO);
					// 定返功能
					// 有盘位收益时，在定返收益详情表插入记录 wallet_platform_fixedrebate_detail_t
					FixedRebateVO fixedRebateVO = new FixedRebateVO();
					fixedRebateVO.setUserId(idcardRelationVO.getUserId());
					fixedRebateVO.setIdCard(pointGrapRelationVO.getIdCard());
					fixedRebateVO.setProfitAmount(allProfitAmount);
					fixedRebateVO.setProfitType(FixedRebateTypeEnum.PW.name());
					fixedRebateVO.setSysType(pointGrapRelationVO.getDiskType());
					fixedRebateVO.setCreatedBy(lastUpdatedBy);
					fixedRebateVO.setLastUpdatedBy(lastUpdatedBy);
					fixedRebateDao.insertRebateDetailInfo(fixedRebateVO);
					// 2. 系统类型收益表 wallet_disk_type_profit_t
					DiskTypeProfitVO diskTypeProfitVO = new DiskTypeProfitVO();
					diskTypeProfitVO.setUserId(idcardRelationVO.getUserId());
					diskTypeProfitVO.setDiskProfitAmount(allProfitAmount);
					diskTypeProfitVO.setDiskType(diskType);
					diskTypeProfitVO.setCreatedBy(lastUpdatedBy);
					diskTypeProfitVO.setLastUpdatedBy(lastUpdatedBy);
					diskTypeProfitDao.updateDiskTypeProfitByUserId(diskTypeProfitVO);
					// 3. 盘总收益表 wallet_disk_profit_t
					DiskProfitVO diskProfitVO = new DiskProfitVO();
					diskProfitVO.setUserId(idcardRelationVO.getUserId());
					diskProfitVO.setDiskSeq(pointGrapRelationVO.getDiskSeq());
					diskProfitVO.setDiskType(diskType);
					diskProfitVO.setDiskProfitAmount(allProfitAmount);
					diskProfitVO.setSaveAmount(saveGoldAmount);
					diskProfitVO.setWithdrawalsAmount(withdrawAmount);
					diskProfitVO.setRoleId(idcardRelationVO.getCurrentRoleId());
					diskProfitVO.setDiskCounter(diskCounter);
					diskProfitVO.setJoinDiskTime(pointGrapRelationVO.getCreatedDate());
					diskProfitVO.setIdCard(pointGrapRelationVO.getIdCard());
					diskProfitVO.setDiskStatus(diskStatus);
					diskProfitVO.setCreatedBy(lastUpdatedBy);
					diskProfitVO.setIsThisDisk("Y");
					diskProfitVO.setLastUpdatedBy(lastUpdatedBy);
					walletProfitDetailDao.increaseDiskProfit(diskProfitVO);
					// 4. 钱包表 wallet_amount_t
					WalletAmountVO walletAmountVO = new WalletAmountVO();
					walletAmountVO.setUserId(idcardRelationVO.getUserId());
					walletAmountVO.setPlatformTotalAmount(allProfitAmount);
					walletAmountVO.setCreatedBy(lastUpdatedBy);
					walletAmountVO.setLastUpdatedBy(lastUpdatedBy);
					walletAmountDao.updateByUserIdSelective(walletAmountVO);
					// 5. 平台总收益表 wallet_platform_profit_t
					PlatformProfitVO platformProfitVO = new PlatformProfitVO();
					platformProfitVO.setUserId(idcardRelationVO.getUserId());
					platformProfitVO.setPlatformTotalAmount(allProfitAmount);
					platformProfitVO.setPlatformRemainAmount(allProfitAmount);
					platformProfitVO.setPlatformWithdrawalsAmount(withdrawAmount);
					platformProfitVO.setSaveAmount(saveGoldAmount);
					platformProfitVO.setMemberTotalAmount(allProfitAmount);
					platformProfitVO.setMemberRemainAmount(allProfitAmount);
					platformProfitVO.setMemberWithdrawalsAmount(withdrawAmount);
					switch (diskType) {
					case "TIYAN":
						platformProfitVO.setTiyanDiskProfitAmount(withdrawAmount.add(saveGoldAmount));
						platformProfitVO.setTiyanSaveAmount(saveGoldAmount);
						platformProfitVO.setTiyanWithdrawalsAmount(withdrawAmount);
						break;
					case "HUIMIN":
						platformProfitVO.setHuiminDiskProfitAmount(withdrawAmount.add(saveGoldAmount));
						platformProfitVO.setHuiminSaveAmount(saveGoldAmount);
						platformProfitVO.setHuiminWithdrawalsAmount(withdrawAmount);
						break;
					case "FUMIN":
						platformProfitVO.setFuminDiskProfitAmount(withdrawAmount.add(saveGoldAmount));
						platformProfitVO.setFuminSaveAmount(saveGoldAmount);
						platformProfitVO.setFuminWithdrawalsAmount(withdrawAmount);
						break;
					case "XINGMIN":
						platformProfitVO.setXingminDiskProfitAmount(withdrawAmount.add(saveGoldAmount));
						platformProfitVO.setXingminSaveAmount(saveGoldAmount);
						platformProfitVO.setXingminWithdrawalsAmount(withdrawAmount);
						break;
					default:
						logger.error("directorPositionProfitSharing error!");
					}
					platformProfitVO.setCreatedBy(lastUpdatedBy);
					platformProfitVO.setLastUpdatedBy(lastUpdatedBy);
					platformProfitDao.updatePlatformProfitByUserId(platformProfitVO);
					// 6. 对应系统的盘用户关系表 wallet_disk_relation_****_t
					DiskRelationVO diskRelationVO = new DiskRelationVO();
					diskRelationVO.setDiskType(diskType);
					diskRelationVO.setDiskSeq(pointGrapRelationVO.getDiskSeq());
					diskRelationVO.setDiskStatus(diskStatus);
					diskRelationVO.setRoleId(idcardRelationVO.getCurrentRoleId());
					diskRelationVO.setLocaltion(location);
					diskRelationVO.setCurrentProfit(allProfitAmount);
					diskRelationVO.setUserId(idcardRelationVO.getUserId());
					diskRelationVO.setIdCard(pointGrapRelationVO.getIdCard());
					diskRelationVO.setCreatedBy(lastUpdatedBy);
					diskRelationVO.setLastUpdatedBy(lastUpdatedBy);
					diskDao.updateUserCurrentProfit(diskRelationVO);
				}
				
			remainAmount = oneProfitAmount.multiply(BigDecimal.valueOf(sysProfit));
		}
		// 给系统钱
		SystemWalletVO systemWalletVO = new SystemWalletVO();
		systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
		systemWalletVO.setSystemProfitAmount(remainAmount);
		scoreDao.updateSystemProfitByVO(systemWalletVO);
		logger.info("directorPositionProfitSharing end");
	}

	/**
	 * 
	 * @Title: updateIdCardFromOldIdCard
	 * @Description: 传入需要更新idCard 的 idCard 集合, 更新他们的IDCARD关系表,并返回他们的map<老的,新的>
	 * @param relationVOs
	 *            旧的IDCARD 集合
	 * @param diskSeq
	 *            盘号
	 * @return
	 * @return: Map<String,String>
	 */
	public Map<String, String> updateIdCardFromOldIdCard(List<IdcardRelationVO> relationVOs, String diskSeq) {
		List<IdcardRelationVO> relationVOsNew = new ArrayList<IdcardRelationVO>();
		Map<String, String> idCardRelationMap = new HashMap<String, String>();
		String idCardTemp = null;
		IdcardRelationVO idCardVO = null;
		// 循环所有的idCardRelationvo
		for (IdcardRelationVO idcardRelationVO : relationVOs) {
			idCardVO = new IdcardRelationVO();
			// 把当前系统类型变成下一个系统类型
			idCardVO.setLastDiskType(idcardRelationVO.getCurrentDiskType());
			idCardVO.setUserId(idcardRelationVO.getUserId());
			idCardVO.setCurrentDiskType(idcardRelationVO.getCurrentDiskType());
			idCardVO.setCreatedBy(idcardRelationVO.getCreatedBy());
			idCardVO.setLastUpdatedBy(idcardRelationVO.getLastUpdatedBy());
			// 确定当前系统类型
			if (idcardRelationVO.getCurrentRoleId() == 1) {
				// 如果是总监把系统类型改为下个系统类型
				idCardVO.setLastDiskType(idcardRelationVO.getCurrentDiskType());
				idCardVO.setCurrentDiskType(DiskEnum.getNextNameByName(idcardRelationVO.getCurrentDiskType()));
			}
			// 把当前IDCARD 变成 上一个IDCARD
			idCardVO.setLastIdCard(idcardRelationVO.getCurrentIdCard());
			idCardTemp = SeqUtils.generateIdCardSN();
			// 把idCard 的对应关系保存下来
			idCardRelationMap.put(idcardRelationVO.getCurrentIdCard(), idCardTemp);
			// 重新生成一个IDcard
			idCardVO.setCurrentIdCard(idCardTemp);
			// 把当前盘号变成上一个盘号
			idCardVO.setLastDiskSeq(idcardRelationVO.getCurrentDiskSeq());
			// 重新生成一个盘号
			idCardVO.setCurrentDiskSeq(diskSeq);
			idCardVO.setLastRoleId(idcardRelationVO.getCurrentRoleId());
			if (idcardRelationVO.getCurrentRoleId() == 1L) {
				idCardVO.setCurrentRoleId(4L);
			} else {
				idCardVO.setCurrentRoleId(idcardRelationVO.getCurrentRoleId() - 1L);
			}
			idCardVO.setStatuxx("Y");
			relationVOsNew.add(idCardVO);
		}
		// 把对应的老IDCARD关系表记录 改为状态不可用
		logger.info("=====================================relationVOs={},Seq={}", relationVOs,3);
		walletDiskRelationDao.updateAllIdCardRelationStatuxx(null, relationVOs, "N");
		// 批量插入 新的 IDCARD 关系表记录
		walletDiskRelationDao.addIdcardRelationByList(relationVOsNew);
		return idCardRelationMap;
	}

	/**
	 * 
	 * @Title: directorToUpWaitting
	 * @Description: 把总监放入 晋升等待补钱表
	 * @param idcardRelationVO
	 * @return: void
	 */
	public void directorToUpWaitting(IdcardRelationVO idcardRelationVO, Long lastUpdatedBy, String diskSeq) {
		// 读取职位奖励配置， 可提现储备金配置
		List<DirectorConfigVO> allWithReser = withdrawReserveConfigDao.getAllConfig();
		PointGrapRelationVO pointGrapRelationVO = pointGrapRelationDao
				.getProfitPointsByIdCard(idcardRelationVO.getCurrentIdCard(), diskSeq);
		BigDecimal differenceAmount = new BigDecimal(0);
		for (DirectorConfigVO directorConfigVO : allWithReser) {
			if (directorConfigVO.getDiskType().equals(pointGrapRelationVO.getDiskType())
					&& directorConfigVO.getGetEmptyPoints() == pointGrapRelationVO.getProfitPoints()) {
				differenceAmount = BigDecimal.valueOf(5000)
						.subtract(directorConfigVO.getReserveAmount().multiply(BigDecimal.valueOf(27)));
			}
		}

		DiskTypeUpWaittingVO diskTypeUpWaittingVO = new DiskTypeUpWaittingVO();
		diskTypeUpWaittingVO.setDiskType(idcardRelationVO.getCurrentDiskType());
		diskTypeUpWaittingVO.setLastIdCard(idcardRelationVO.getCurrentIdCard());
		diskTypeUpWaittingVO.setUserId(idcardRelationVO.getUserId());
		diskTypeUpWaittingVO.setIdCard(SeqUtils.generateIdCardSN());
		diskTypeUpWaittingVO.setWaittingStatus(WaittingStatusEnum.WAITTING.name());
		diskTypeUpWaittingVO.setDifferenceAmount(differenceAmount);
		diskTypeUpWaittingVO.setCreatedBy(lastUpdatedBy);
		diskTypeUpWaittingVO.setLastUpdatedBy(lastUpdatedBy);
		if (idcardRelationVO.getCurrentDiskType().equals(DiskEnum.FUMIN.name())
				&& pointGrapRelationVO.getProfitPoints().equals(3)) {
			// 系统赠送金额
			diskTypeUpWaittingVO.setSystemGiveAmount(BigDecimal.valueOf(500));
			// 扣除系统收益
			SystemWalletVO systemWalletVO = new SystemWalletVO();
			systemWalletVO.setSystemId(CommonConstant.SYSTEM_USER_ID);
			systemWalletVO.setSystemProfitAmount(BigDecimal.valueOf(500).multiply(BigDecimal.valueOf(-1)));
			systemWalletVO.setSystemExpenditureAmount(BigDecimal.valueOf(500));
			scoreDao.updateSystemProfitByVO(systemWalletVO);
		}
		walletDiskWaittingDao.addDiskTypeUpWaitting(diskTypeUpWaittingVO);
	}

	/**
	 * 
	 * @Title: diskRelationTOIdCardRelation
	 * @Description: 盘关系 查询出 对应的IDCARD 关系
	 * @param diskRelationVOs
	 * @param roleId
	 * @return
	 * @return: List<IdcardRelationVO>
	 */
	public List<IdcardRelationVO> diskRelationTOIdCardRelation(List<WalletDiskRelationVO> diskRelationVOs,
			Long roleId) {
		List<IdcardRelationVO> idcardRelationVOs = new ArrayList<>();
		List<IdcardRelationVO> returnIdcardRelationVOs = new ArrayList<>();
		List<String> idCardOldLists = new ArrayList<>();
		// 循环老盘详情，得到所有的idCard
		for (WalletDiskRelationVO diskRelationVO : diskRelationVOs) {
			idCardOldLists.add(diskRelationVO.getIdCard());
		}
		// 查询40个用户的idCardRelationvo
		idcardRelationVOs = walletDiskRelationDao.getAllIdCardRelationByCurrentIdCards(idCardOldLists);
		// 根据用户roleId拿到相应的idCardRelationVo
		for (IdcardRelationVO idcardRelationVO : idcardRelationVOs) {
			if (idcardRelationVO.getCurrentRoleId().equals(roleId)) {
				returnIdcardRelationVOs.add(idcardRelationVO);
			} else if (idcardRelationVO.getCurrentRoleId().equals(roleId)) {
				returnIdcardRelationVOs.add(idcardRelationVO);
			} else if (idcardRelationVO.getCurrentRoleId().equals(roleId)) {
				returnIdcardRelationVOs.add(idcardRelationVO);
			} else if (idcardRelationVO.getCurrentRoleId().equals(roleId)) {
				returnIdcardRelationVOs.add(idcardRelationVO);
			}
		}
		return returnIdcardRelationVOs;
	}

	public Integer deductionAPoints(Integer pointCount) {
		if (pointCount % 2 != 0) {
			return pointCount - 1;
		}
		return pointCount;
	}

	public Integer whichManager(Integer pointNum) {
		if (pointNum >= 1 && pointNum <= 3) {
			return 1;
		} else if (pointNum >= 4 && pointNum <= 6) {
			return 2;
		} else if (pointNum >= 7 && pointNum <= 9) {
			return 3;
		} else {
			return -999999;
		}
	}

	public static void main(String[] args) {

		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		System.out.println(list.toArray().toString());
		List<Integer> list2 = a(list);
		System.out.println(list2);
	}

	public static List<Integer> a(List<Integer> list) {
		list.set(0, list.get(0) + 1);
		System.out.println(list);
		Integer b = list.get(0);
		System.out.println(b);
		return list;
	}
	

}
