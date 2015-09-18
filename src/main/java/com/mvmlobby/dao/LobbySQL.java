package com.mvmlobby.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.apache.log4j.Logger;

import uk.co.thomasc.steamkit.types.steamid.SteamID;
import eu.jhomlala.steambot.utils.Log;

public class LobbySQL {

	private Connection connection;
	private PreparedStatement preparedStmt;
	private ResultSet rs;

	public LobbySQL() {}

	public boolean insertPlayerLobbyId(SteamID steamid, SteamID lobbyid)
			throws Exception {
		boolean success = false;
		try {
			final String steamidString = Long.toString(steamid.convertToLong());
			final String lobbyidString = Long.toString(lobbyid.convertToLong());
			
			connection = ConnectionFactory.getConnection();
			
			
			// Get the player_id (player must exist in at least 1 group first)
			int player_id = -1;
			String sql = "select p.id from player p where p.steamid = ? and exists (select * from player_group pg where pg.player_id = p.id)";
			preparedStmt = connection.prepareStatement(sql);
			preparedStmt.setString(1, steamidString);
			rs = preparedStmt.executeQuery();
			
			while(rs.next()) {
				player_id = rs.getInt("id");
			}
			if (player_id == -1) {
				// Steam ID not found in the mvmlobby.com database because:
				// 1) The user really has no entry (not possible for group members anyway)
				// 2) The user does not belong to a valid MvM group
				return false;
			}
			
			
			// Delete any existing lobby this player has
			sql = "delete from group_lobby where player_id = ?";
			preparedStmt = connection.prepareStatement(sql);
			preparedStmt.setInt(1, player_id);
			preparedStmt.executeUpdate();
			
			
			// Create the new lobby record
			sql = "insert group_lobby (player_id, lobby_id, created) select ?, ?, now()";
			preparedStmt = connection.prepareStatement(sql);
			preparedStmt.setInt(1, player_id);
			preparedStmt.setString(2, lobbyidString);
			int rowsAffected = preparedStmt.executeUpdate();
			
			if (rowsAffected == 1) {
				success = true;
			}

			SQLWarning warning = preparedStmt.getWarnings();
			if (warning != null)
				throw new Exception(warning.getMessage());
		} catch (SQLException e) {
			Exception exception = new Exception(e.getMessage(), e);
			throw exception;
		} finally {
			DbUtil.close(rs);
			DbUtil.close(preparedStmt);
			DbUtil.close(connection);
		}
		return success;
	}


	public boolean isMvMGroupMember(SteamID steamId) {
		Logger log = Log.getInstance();

		Connection connection = null;
		ResultSet results = null;
		boolean isMvMGroupMember = false;
		
		try {
			connection = ConnectionFactory.getConnection();
			preparedStmt = connection.prepareStatement("select count(*) as group_count from player p inner join player_group pg on pg.player_id = p.id where p.steamid = ?");
			preparedStmt.setString(1, new Long(steamId.convertToLong()).toString());
			results = preparedStmt.executeQuery();

			while (results.next()) {
				if (results.getInt("group_count") > 0) {
					isMvMGroupMember = true;
				}
			}

			SQLWarning warning = preparedStmt.getWarnings();
			if (warning != null) {
				log.info(warning.getMessage());
			}
		} catch (SQLException e) {
			log.info("sql error: " + e.toString());
		} finally {
			DbUtil.close(results);
			DbUtil.close(preparedStmt);
			DbUtil.close(connection);
		}

		return isMvMGroupMember;
	}

}
