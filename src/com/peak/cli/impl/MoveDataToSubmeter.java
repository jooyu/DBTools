package com.peak.cli.impl;

import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.PreparedStatement;
import com.peak.cli.ExecuteCommand;
import com.peak.cli.model.OpenIdPlayerIds;
import com.peak.cli.model.SDKDb2PeakDataConfig;
import com.peak.core.db.sql.impl.SelectStatement;
import com.peak.core.spring.SpringContext;
@Component
public class MoveDataToSubmeter extends ExecuteCommand{
	private static final Logger LOGGER = LoggerFactory.getLogger(SDKDb2PeakData.class);

	static SelectStatement selectSql = new SelectStatement();
	
	
	SDKDb2PeakDataConfig config;
	
	int countNu=0;
	JdbcTemplate peakDataJdbc;
	JdbcTemplate peakDealJdbc;

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "move_data_to_Submeter";
	}

	@Override
	public void execute(String[] args) {
		// 显示 输出参数和配置参数
				// 用户输入确认
				// 检测 数据库权限
				// 执行迁移脚本
		
		config = SpringContext.getBean(SDKDb2PeakDataConfig.class);

		LOGGER.info("config info:");
		LOGGER.info("---------------------------------------------------------");
		for (Entry<String, Object> entry : config.getFieldMpas().entrySet()) {
			LOGGER.info(entry.getKey() + " = " + entry.getValue());
		}
		LOGGER.info("---------------------------------------------------------");
		LOGGER.info("start to copy and migrate.");
		//具体业务层
		//首先将查询所有结果放到一个Map
		try {

				loadInfo();
				LOGGER.info("init table");
				//初始化所有的gameid表
				initTableByGameId(peakDataJdbc);

		} catch (Exception ex) {
			LOGGER.error("{}", ex);
		}
		LOGGER.info("exit!");
		System.exit(-9);
	}
	




	//根据去重的gameid初始化表
	private void initTableByGameId(JdbcTemplate peakDataJdbc) {
		String gameidSql = String.format("select distinct  game_id from user_jieo;");
		
		List<Map<String, Object>> list = peakDataJdbc.queryForList(gameidSql);
		for (Map<String, Object> item : list) {
			String gameId = item.get("game_id").toString();
			copyTable(Integer.valueOf(gameId), peakDataJdbc);
		}
		
	}

	private void loadInfo() {
		peakDataJdbc = getJdbc(config.peak_data_db_ip, config.peak_data_user_name, config.peak_data_password, config.peak_data_db);
		peakDealJdbc = getJdbc(config.peak_data_db_ip, config.peak_data_user_name, config.peak_data_password, config.peak_deal_db);

	}
	
	private void copyTable(int gameId,JdbcTemplate peakDataJdbc) {
	try {
		LOGGER.info("create user_{}", gameId);
		String sqlUser = String.format("CREATE TABLE IF NOT EXISTS user_%s LIKE _user_template ", gameId);
		peakDataJdbc.execute(sqlUser);
		
		LOGGER.info("insert user_{}", gameId);
		String sqlData = String.format("replace into user_%s select * from user_jieo where game_id=%s;", gameId,gameId);
		peakDataJdbc.execute(sqlData);
		String sqlOrder = String.format("CREATE TABLE IF NOT EXISTS order_%s LIKE _order_template ", gameId);
		peakDataJdbc.execute(sqlOrder);
	
	} catch (Exception e) {
		LOGGER.warn("e.getMessage()!");
	}
	finally {
	
	}
	
		LOGGER.info(gameId+" copy finish...");	
		
	}
	
}
