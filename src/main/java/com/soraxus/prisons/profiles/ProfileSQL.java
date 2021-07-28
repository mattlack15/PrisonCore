package com.soraxus.prisons.profiles;

import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class ProfileSQL {
    private Connection connection;

    private static final String TABLE = "profiles";

    public void connect(String host, String database, int port, String username, String password) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true";
        connection = DriverManager.getConnection(url, username, password);
    }

    public void createTables() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + TABLE + " (identifier varchar(64), data MEDIUMBLOB, lastEdit BIGINT)")) {
            statement.executeUpdate();
        }
    }

    public void insertProfiles(List<? extends PlayerProfile> profiles) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE + " (identifier, data, lastEdit) VALUES (?, ?, ?) ON DUPLICATE KEY " +
                "UPDATE data=VALUES(data), lastEdit=VALUES(lastEdit)")) {
            for (PlayerProfile p : profiles) {
                GravSerializer serializer = new GravSerializer();
                p.getMeta().serialize(serializer);
                statement.setString(1, p.getPlayerId().toString());
                statement.setBytes(2, serializer.toByteArray());
                statement.setLong(3, System.currentTimeMillis());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public void updateProfiles(List<? extends PlayerProfile> profiles) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE + " SET data=?, lastEdit=? WHERE identifier=?")) {
            for (PlayerProfile p : profiles) {
                GravSerializer serializer = new GravSerializer();
                p.getMeta().serialize(serializer);
                statement.setBytes(1, serializer.toByteArray());
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, p.getPlayerId().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public void deleteProfiles(List<? extends PlayerProfile> profiles) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE + " WHERE identifier=?")) {
            for (PlayerProfile p : profiles) {
                GravSerializer serializer = new GravSerializer();
                p.getMeta().serialize(serializer);
                statement.setString(1, p.getPlayerId().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public Meta getProfile(UUID playerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT data FROM " + TABLE + " WHERE identifier=?")) {
            statement.setString(1, playerId.toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                byte[] b = set.getBytes(1);
                return new Meta(new GravSerializer(b));
            }
            return null;
        }
    }

    public void disconnect() throws SQLException {
        this.connection.close();
    }
}
