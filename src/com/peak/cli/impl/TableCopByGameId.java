package com.peak.cli.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.peak.cli.ExecuteCommand;
import com.peak.cli.model.SDKDb2PeakDataConfig;
import com.peak.core.db.sql.impl.SelectStatement;
import com.peak.core.spring.SpringContext;
@Component
public class TableCopByGameId extends ExecuteCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(SDKDb2PeakData.class);

	static SelectStatement selectSql = new SelectStatement();
	
	
	SDKDb2PeakDataConfig config;
	

	JdbcTemplate peakDataJdbc;
	JdbcTemplate peakDealJdbc;
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "table_copy_by_gameid";
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
		LOGGER.info("Please enter the 'game_id' you want to copy.");

		try {
			byte[] buffer = new byte[512];
			System.in.read(buffer);
			
			int gameId = Integer.valueOf(new String(buffer).trim());
			if (gameId > 0) {
				loadInfo();
			
				copyTable(gameId,peakDataJdbc);
			}
		} catch (Exception ex) {
			LOGGER.error("{}", ex);
		}
		LOGGER.info("exit!");
		System.exit(-9);
	}
	private void loadInfo() {
		peakDataJdbc = getJdbc(config.peak_data_db_ip, config.peak_data_user_name, config.peak_data_password, config.peak_data_db);
		peakDealJdbc = getJdbc(config.peak_data_db_ip, config.peak_data_user_name, config.peak_data_password, config.peak_deal_db);

	}
	
	private void copyTable(int gameId,JdbcTemplate peakDataJdbc) {
		String sqlUser = String.format("CREATE TABLE user_%s LIKE _user_template", gameId);
		peakDataJdbc.execute(sqlUser);
		String sqlOrder = String.format("CREATE TABLE order_%s LIKE _order_template", gameId);
		peakDataJdbc.execute(sqlOrder);
		LOGGER.info("copy over...");		
		
	}
		
	}


