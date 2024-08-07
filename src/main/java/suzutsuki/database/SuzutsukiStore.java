package suzutsuki.database;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import suzutsuki.struct.store.SuzutsukiStoreEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SuzutsukiStore {
	private final JdbcConnectionPool pool;

	public SuzutsukiStore(Logger logger, String location) throws SQLException {
		String driver = "jdbc:h2:file:";
		String database = "database\\store;MODE=MYSQL";
		this.pool = JdbcConnectionPool.create(
			driver + location + database,
			"",
			""
		);
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS SuzutsukiStore(" +
				"guildId TINYTEXT NOT NULL," +
				"userId TINYTEXT NOT NULL," +
				"timestamp BIGINT," +
				"UNIQUE (userId, guildId)" +
				")"
			)) {
				statement.execute();
			}
		}

		logger.info("Using database driver: {} and is connected to: {}", driver, database);
	}

	public void add(String guild, String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("INSERT INTO SuzutsukiStore(guildId, userId) " +
				"VALUES(?, ?) " +
				"ON DUPLICATE KEY UPDATE " +
				"userId = userId"
			)) {
				statement.setString(1, guild);
				statement.setString(2, user);
				statement.execute();
			}
		}
	}

	public boolean some(String guild, String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM SuzutsukiStore WHERE guildId = ? AND userId = ?")) {
				statement.setString(1, guild);
				statement.setString(2, user);
				try (ResultSet results = statement.executeQuery()) {
					return results.first();
				}
			}
		}
	}

	public List<SuzutsukiStoreEntry> filter(String guild) throws SQLException {
		List<SuzutsukiStoreEntry> store = new ArrayList<>();
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM SuzutsukiStore WHERE guildId = ?")) {
				statement.setString(1, guild);
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						SuzutsukiStoreEntry entry = new SuzutsukiStoreEntry();
						entry.guildId = results.getString("guildId");
						entry.userId = results.getString("userId");
						store.add(entry);
					}
				}
			}
		}
		return store;
	}

	public void delete(String guild, String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM SuzutsukiStore WHERE guildId = ? AND userId = ?")) {
				statement.setString(1, guild);
				statement.setString(2, user);
				statement.execute();
			}
		}
	}

	public List<SuzutsukiStoreEntry> list(String user) throws SQLException {
		List<SuzutsukiStoreEntry> store = new ArrayList<>();
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM SuzutsukiStore WHERE userId = ?")) {
				statement.setString(1, user);
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						SuzutsukiStoreEntry entry = new SuzutsukiStoreEntry();
						entry.guildId = results.getString("guildId");
						entry.userId = results.getString("userId");
						store.add(entry);
					}
				}
			}
		}
		return store;
	}

	public void schedule(String user, long timestamp) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("UPDATE SuzutsukiStore SET timestamp = ? WHERE userId = ?")) {
				statement.setLong(1, timestamp);
				statement.setString(2, user);
				statement.execute();
			}
		}
	}

	public void unschedule(String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("UPDATE SuzutsukiStore SET timestamp = NULL WHERE userId = ?")) {
				statement.setString(1, user);
				statement.execute();
			}
		}
	}

	public List<SuzutsukiStoreEntry> scheduled() throws SQLException {
		List<SuzutsukiStoreEntry> store = new ArrayList<>();
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM SuzutsukiStore WHERE timestamp IS NOT NULL")) {
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						SuzutsukiStoreEntry entry = new SuzutsukiStoreEntry();
						entry.guildId = results.getString("guildId");
						entry.userId = results.getString("userId");
						store.add(entry);
					}
				}
			}
		}
		return store;
	}

	public List<SuzutsukiStoreEntry> active() throws SQLException {
		List<SuzutsukiStoreEntry> store = new ArrayList<>();
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM SuzutsukiStore WHERE timestamp IS NULL")) {
				try (ResultSet results = statement.executeQuery()) {
					while (results.next()) {
						SuzutsukiStoreEntry entry = new SuzutsukiStoreEntry();
						entry.guildId = results.getString("guildId");
						entry.userId = results.getString("userId");
						store.add(entry);
					}
				}
			}
		}
		return store;
	}

	public int size(String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM SuzutsukiStore WHERE userId = ?")) {
				statement.setString(1, user);
				try (ResultSet results = statement.executeQuery()) {
					return results.first() ? results.getInt("count") : 0;
				}
			}
		}
	}

	public void clean(String user) throws SQLException {
		try (Connection connection = this.pool.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM SuzutsukiStore WHERE userId = ?")) {
				statement.setString(1, user);
				statement.execute();
			}
		}
	}

	public int purge() throws SQLException {
		int cleaned;
		try (Connection connection = pool.getConnection()) {
			PreparedStatement statement = connection.prepareStatement("DELETE FROM SuzutsukiStore WHERE timestamp IS NOT NULL AND timestamp <= ?");
			statement.setLong(1, Instant.now().toEpochMilli());
			cleaned = statement.executeUpdate();
		}
		return cleaned;
	}
}
