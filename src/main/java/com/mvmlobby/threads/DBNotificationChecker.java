package com.mvmlobby.threads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mvmlobby.dao.ConnectionFactory;
import com.mvmlobby.dao.DbUtil;

import eu.jhomlala.steambot.utils.Log;

public class DBNotificationChecker extends Thread {
	
	private NotifyUsers notifyUsersDelegate;
	
	public DBNotificationChecker(NotifyUsers notifyUsers) {
		this.notifyUsersDelegate = notifyUsers;
	}

	@Override
	public void run() {
		Logger log = Log.getInstance();
		Connection connection = ConnectionFactory.getConnection();;
		PreparedStatement preparedStmt;
		Statement statement = null;
		ResultSet results = null;
		
		StringBuilder sqlSB = new StringBuilder();
		sqlSB
			.append("select gl.id, p.name as 'player_name', m.name as 'mission_name', ")
			.append("	t.short_name as 'tour_name', r.name as 'region_name', ")
			.append("	gl.slots_available, mg.name as 'mvm_group_name', gl.lobby_id ")
			.append("from group_lobby gl ")
			.append("inner join player p on p.id = gl.player_id ")
			.append("inner join mission m on m.id = gl.mission_id ")
			.append("inner join tour t on t.id = m.tour_id ")
			.append("inner join region r on r.id = gl.region_id ")
			.append("inner join mvm_group mg on mg.id = gl.mvm_group_id ")
			.append("where gl.player_initialized = 1 ")
			.append("and gl.invitations_sent = 0");

		final String GET_NEW_TEAMS_SQL = sqlSB.toString();
		
		while (true) {

			try {
				statement = connection.createStatement();
				results = statement.executeQuery(GET_NEW_TEAMS_SQL);
	
				while (results.next()) {
					
					// Update to prevent further notifications
					String sql = "update group_lobby set invitations_sent=1 where id=?";
					preparedStmt = connection.prepareStatement(sql);
					preparedStmt.setInt(1, results.getInt("id"));
					preparedStmt.executeUpdate();

					// Send the notifications
					log.info("calling notifyUsersDelegate for teamId " + results.getInt("id"));
					this.notifyUsersDelegate.sendNewTeamNotification(
							results.getLong("id"),
							results.getLong("lobby_id"), 
							results.getString("player_name"),
							results.getString("mission_name"),
							results.getString("tour_name"),
							results.getString("region_name"),
							results.getInt("slots_available"),
							results.getString("mvm_group_name")
					);
					
				}
	
				SQLWarning warning = statement.getWarnings();
				if (warning != null) {
					log.info(warning.getMessage());
				}
				
			} catch (SQLException e) {
				log.info("sql error: " + e.toString());
			} finally {
				DbUtil.close(results);
				DbUtil.close(statement);
			}
			
			try { Thread.sleep(1000); } catch (Exception e) {}
		}
	}
	

	public static interface NotifyUsers {
		public void sendNewTeamNotification(Long team_id, Long lobby_id,
				String player_name, String mission_name, String tour_name,
				String region_name, Integer slots_available,
				String mvm_group_name);
	}
	

}
