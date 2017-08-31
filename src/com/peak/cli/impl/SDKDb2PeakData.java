package com.peak.cli.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.peak.cli.ExecuteCommand;
import com.peak.cli.model.OpenIdPlayerIds;
import com.peak.cli.model.SDKDb2PeakDataConfig;
import com.peak.core.db.sql.impl.SelectStatement;
import com.peak.core.spring.SpringContext;

@Component
public class SDKDb2PeakData extends ExecuteCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(SDKDb2PeakData.class);

	static SelectStatement selectSql = new SelectStatement();
	
	int countTest=0;
	SDKDb2PeakDataConfig config;
	
	Map<String, Integer> identifierMaps = new HashMap<>();
	JdbcTemplate peakDataJdbc;
	JdbcTemplate peakDealJdbc;
	
	public String name() {
		return "sdk_db_2_peak_data";
	}

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
		LOGGER.info("Please enter the 'game_id' you want to migrate.");

		try {
			byte[] buffer = new byte[512];
			System.in.read(buffer);
			
			int gameId = Integer.valueOf(new String(buffer).trim());
			if (gameId > 0) {
				
				loadInfo();
				if (identifierMaps.isEmpty()) {
					LOGGER.info("identifier map is emtpy.");
					return;
				}
				
				moveData(gameId);
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
		
		identifierMaps = getChannelIdentifierList(config);
		
		LOGGER.info("identifier load complete! {} size.", identifierMaps.size());
	}
	
	/**
	 * 
	 * @param config
	 */
	public void moveData(int gameId) {		
		LOGGER.info("game_id:{} start migrate data~~~~~~~~~~~~~~", gameId);
		
		int count = 0;
		for (int i = config.union_db_hash_min; i <= config.union_db_hash_max; i++) {
			String dbName = config.union_db_prefix + i;
			JdbcTemplate jdbc = getJdbc(config.union_db_ip, config.union_user_name, config.union_password, dbName);			
			int pageSize = 500;
			for (int t = config.union_table_hash_min; t < config.union_table_hash_max; t++) {
				int pageIndex = 0;
				while (true) {
					String tableName = config.union_table_prefix + t;
					// where game_id=%s gameId,
					String sql = String.format("select * from %s limit %s,%s", tableName, pageIndex * pageSize, pageSize);

					List<Map<String, Object>> list = jdbc.queryForList(sql);
					if (list == null || list.size() < 1) {
						break;
					}
					count += list.size();

					List<OpenIdPlayerIds> openIdList = new ArrayList<>();
					for (Map<String, Object> item : list) {
						OpenIdPlayerIds openIdItem = new OpenIdPlayerIds(item);
						openIdList.add(openIdItem);
					}
				//	moveData2PeakData(openIdList);
					moveData2PeakData(openIdList);
					pageIndex++;
				}
			}
		}
		LOGGER.info("execute complete! {} total records.", count);
	}

	public void moveData2PeakData(List<OpenIdPlayerIds> openIdList) {
		if (openIdList.isEmpty()) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO `user_jieo` (`game_id`,`open_id`,`channel_id`,`channel_uid`,`ledou_player_id`) VALUES ");
		//sb.append("INSERT INTO `user_").append(gameId).append("` (`game_id`,`open_id`,`channel_id`,`channel_uid`,`ledou_player_id`) VALUES ");
		openIdList.forEach(item -> {
			Integer channelId = identifierMaps.get(item.channel_id);
			if (channelId == null) {
				LOGGER.warn("channelId not found... channel_identifier:{}", item.channel_id);
			} else {
				sb.append(String.format("('%s','%s','%s','%s','%s'),", item.game_id, item.openid, channelId, item.channel_openid, item.player_id));	
			}
		});
		sb.deleteCharAt(sb.length() - 1);
		
		peakDataJdbc.update(sb.toString());
		countTest++;
		LOGGER.info("count {} .", countTest);		
		LOGGER.info("import data complete! {} record.", openIdList.size());		
	}
	
	public Map<String, Integer> getChannelIdentifierList(SDKDb2PeakDataConfig config) {
		Map<String, Integer> identifierList = new HashMap<>();
		
		String sql = "select * from channel_package";

		List<Map<String, Object>> list = peakDealJdbc.queryForList(sql);
		for (Map<String, Object> item : list) {
			String identifier = item.get("channel_identifier").toString();
			int channelId = Integer.valueOf(item.get("channel_id").toString());
			identifierList.put(identifier, channelId);
		}

		return identifierList;
	}

}
