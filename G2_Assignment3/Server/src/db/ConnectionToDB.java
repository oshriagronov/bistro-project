package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import communication.StatusCounts;
import logic.CurrentDinerRow;
import logic.Reservation;
import logic.Status;
import logic.Subscriber;
import logic.Table;
import logic.TableStatus;
import logic.Worker;
import logic.WorkerType;

import java.sql.Statement;

/**
 * Provides database access helpers for reservations, tables, subscribers, and workers.
 */
public class ConnectionToDB {

	/**
	 * Returns the database password currently stored in the connection pool.
	 *
	 * @return database password string
	 */
	public static String getDbPassword() {
		return MySQLConnectionPool.getDbPassword();
	}

	/**
	 * Updates the database password used by the connection pool.
	 *
	 * @param password new database password
	 */
	public static void setPassword(String password) {
		MySQLConnectionPool.setPassword(password);
	}

	// ** Order related methods **
	/**
	 * Updates an existing order by order number (primary key). Fields updated:
	 * order_date, number_of_guests.
	 * 
	 * @param order_number     int type
	 * @param order_date       LocalDate type, need to use java.sql.Date.valueOf
	 *                         method to get valid value for mySQL table
	 * @param number_of_guests int type
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int updateOrder(int order_number, LocalDate order_date, int number_of_guests) {
		String sql = "UPDATE `reservations` SET order_date = ?, num_of_diners = ? WHERE res_id = ?";
		return executeWriteQuery(sql, order_date, number_of_guests, order_number);
	}

	/**
	 * Searches for the latest order by phone number and returns the order details.
	 * 
	 * @param phone_number phone number to search by
	 * @return Reservation containing the values returned from the DB, or null if
	 *         not found
	 */
	public Reservation searchOrderByPhoneNumber(String phone_number) {
		String sql = "SELECT res_id, confirmation_code, phone, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ?";

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setString(1, phone_number);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
					LocalDate placingDate = rs.getObject("date_of_placing_order", LocalDate.class);

					LocalTime startTime = rs.getObject("start_time", LocalTime.class);
					LocalTime finishTime = rs.getObject("finish_time", LocalTime.class);

					int diners = rs.getInt("num_diners");
					int confirmationCode = rs.getInt("confirmation_code");
					int subId = rs.getInt("sub_id");

					Status status = Status.valueOf(rs.getString("order_status"));

					Reservation r = new Reservation(orderDate, diners, confirmationCode, subId, placingDate, startTime,
							finishTime, rs.getString("phone"), status);

					// res_id into orderNumber (add setter in Reservation)
					r.setOrderNumber(rs.getInt("res_id"));

					return r;
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException: searchOrderByPhoneNumber failed.");
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return null;
	}

	/**
	 * Searches for all orders by phone number and returns the order details list.
	 * 
	 * @param phone_number phone number to search by
	 * @return list of reservations returned from the DB (empty if none found)
	 */
	public List<Reservation> searchOrdersByPhoneNumberList(String phone_number) {
		String sql = "SELECT res_id, confirmation_code, phone, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ?";

		List<Reservation> reservations = new ArrayList<>();

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setString(1, phone_number);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int resId = rs.getInt("res_id");
					LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
					LocalDate placingDate = rs.getObject("date_of_placing_order", LocalDate.class);

					LocalTime startTime = rs.getObject("start_time", LocalTime.class);
					LocalTime finishTime = rs.getObject("finish_time", LocalTime.class);

					int diners = rs.getInt("num_diners");
					int confirmationCode = rs.getInt("confirmation_code");
					int subId = rs.getInt("sub_id");

					Status status = Status.valueOf(rs.getString("order_status"));

					// Use the constructor you DO have (7 args)
					Reservation r = new Reservation(orderDate, diners, confirmationCode, subId, placingDate, startTime,
							rs.getString("phone"));

					// Override DB values that the 7-arg constructor doesn't set correctly
					r.setFinish_time(finishTime);
					r.setStatus(status);

					// Save res_id into your object (add this setter if missing)
					r.setOrderNumber(resId);

					reservations.add(r);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return reservations;
	}

	/**
	 * Searches for an order by order number (primary key) and returns the order
	 * details.
	 * 
	 * @param order_number order number to search by
	 * @return Reservation containing the values returned from the DB, or null if
	 *         not found
	 */
	public Reservation searchOrderByOrderNumber(int order_number) {
		// order_number == res_id in your DB
		String sql = "SELECT res_id, confirmation_code, phone, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE res_id = ?";

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setInt(1, order_number);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
					LocalDate placingDate = rs.getObject("date_of_placing_order", LocalDate.class);

					LocalTime startTime = rs.getObject("start_time", LocalTime.class);
					LocalTime finishTime = rs.getObject("finish_time", LocalTime.class);

					int diners = rs.getInt("num_diners");
					int confirmationCode = rs.getInt("confirmation_code");
					int subId = rs.getInt("sub_id");

					Status status = Status.valueOf(rs.getString("order_status"));
					String phone = rs.getString("phone");

					Reservation r = new Reservation(orderDate, diners, confirmationCode, subId, placingDate, startTime,
							phone);

					r.setFinish_time(finishTime);
					r.setStatus(status);

					// store res_id inside the object (add setter if missing)
					r.setOrderNumber(rs.getInt("res_id"));

					return r;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return null;
	}

	/**
	 * Retrieves a reservation from the database using phone number and confirmation
	 * code.
	 *
	 * @param phone            the phone number associated with the reservation
	 * @param confirmationCode the confirmation code of the reservation
	 * @return a Reservation object if found, otherwise null
	 */
	public Reservation getOrderByPhoneAndCode(String phone, int confirmationCode) {
		String sql = "SELECT res_id, confirmation_code, phone, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ? AND confirmation_code = ?";

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setString(1, phone);
			stmt.setInt(2, confirmationCode);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					LocalDate orderDate = rs.getObject("order_date", LocalDate.class);
					LocalDate placingDate = rs.getObject("date_of_placing_order", LocalDate.class);

					LocalTime startTime = rs.getObject("start_time", LocalTime.class);
					LocalTime finishTime = rs.getObject("finish_time", LocalTime.class);

					int diners = rs.getInt("num_diners");
					int subId = rs.getInt("sub_id");

					Status status = Status.valueOf(rs.getString("order_status"));

					Reservation r = new Reservation(orderDate, diners, confirmationCode, subId, placingDate, startTime,
							rs.getString("phone"));

					r.setFinish_time(finishTime);
					r.setStatus(status);

					// store res_id into your object
					r.setOrderNumber(rs.getInt("res_id")); // add setter if missing

					return r;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return null;
	}

	/**
	 * Retrieves the active order number (reservation ID) associated with a given
	 * table number.
	 * 
	 * @param tableNumber the table number to check
	 * @return the order number (reservation ID) currently assigned to the table, or
	 *         0 if not found/empty
	 */
	public int getOrderNumberByTableNumber(int tableNumber) {
		String sql = "SELECT res_id FROM `tablestable` WHERE table_number = ?";
		int orderNumber = 0;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null)
			return orderNumber;
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setInt(1, tableNumber);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				orderNumber = rs.getInt("res_id");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Crucial: Return connection to the pool here!
			pool.releaseConnection(pConn);
		}
		return orderNumber;
	}

	/**
	 * Deletes an order from the DB by order number (primary key)
	 * 
	 * @param order_number order number to delete
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int deleteOrderByOrderNumber(int order_number) {
		String sql = "DELETE FROM `reservations` WHERE res_id = ?";
		return executeWriteQuery(sql, order_number);
	}

	/**
	 * Updates the status for an existing order.
	 *
	 * @param order_number order number to update
	 * @param status       new order status
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int changeOrderStatus(int order_number, Status status) {
		String sql = "UPDATE `reservations` SET order_status = ? WHERE res_id = ?";
		return executeWriteQuery(sql, status.name(), order_number);
	}

	// ** Tables related methods **
	/**
	 * Loads all tables and their current status from the DB.
	 *
	 * @return list of tables in the system (empty if none found)
	 */
	public List<Table> loadTables() {
		String sql = "SELECT table_number, size, res_id FROM `tablestable`";
		List<Table> tables = new ArrayList<>();

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return new ArrayList<>();

		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int tableNumber = rs.getInt("table_number");
				int tableSize = rs.getInt("size"); // maps to table_size in your Java class
				Integer resId = rs.getObject("res_id", Integer.class); // NULL-safe

				tables.add(new Table(tableNumber, tableSize, resId));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return tables;
	}

	/**
	 * Searches for an available table that can accommodate the specified number of
	 * guests.
	 * 
	 * @param number_of_guests the minimum number of seats required
	 * @return the table number of a suitable table, or 0 if no such table is found
	 */
	public int searchAvailableTableBySize(int number_of_guests) {
		String sql = "SELECT table_number, table_size FROM `tablestable` WHERE res_id IS NULL AND table_size >= ?";
		int min = 10;
		int table_number = 0;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null)
			return table_number; // TODO: numbers of tables start from 1
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setInt(1, number_of_guests);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int tableNumber = rs.getInt("table_number");
				int tableSize = rs.getInt("table_size");
				if (tableSize < min) {
					table_number = tableNumber;
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Crucial: Return connection to the pool here!
			pool.releaseConnection(pConn);
		}
		return table_number;
	}

	/**
	 * Updates the size for a table by table number.
	 *
	 * @param table_number table number to update
	 * @param table_size   new table size
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int changeTableSize(int table_number, int table_size) {
		String sql = "UPDATE `tablestable` SET size = ? WHERE table_number = ?";
		return executeWriteQuery(sql, String.valueOf(table_size), table_number);
	}

	/**
	 * Updates the reservation ID (res_id) of a table to 0, effectively clearing any
	 * active reservation for that table.
	 *
	 * @param table_number the table number whose reservation ID should be reset
	 * @return number of rows affected (1 = success, 0 = table not found)
	 */
	public int changeTableResId(int table_number) {
		String sql = "UPDATE `tablestable` SET res_id=? WHERE table_number = ?";
		return executeWriteQuery(sql, String.valueOf(0), table_number);
	}

	/**
	 * Adds a new table to the tables table.
	 *
	 * @param tableSize number of seats
	 * @return number of rows affected (1 = success, 0 = failure)
	 */
	public int addTable(int tableSize) {
		String sql = "INSERT INTO tablestable (size) VALUES (?)";
		return executeWriteQuery(sql, String.valueOf(tableSize));
	}

	/**
	 * Deletes a table from the DB by table number.
	 *
	 * @param tableNumber the table number (primary key)
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int deleteTable(int tableNumber) {
		String sql = "DELETE FROM tablestable WHERE table_number = ?";
		return executeWriteQuery(sql, tableNumber);
	}

	// ** Subscriber related methods **

	/**
	 * Adds a new subscriber to the DB and updates the subscriber ID in the object.
	 *
	 * @param subscriber subscriber details to insert
	 * @throws SQLException when the insert fails or no pooled connection is
	 *                      available
	 */
	public void addSubscriber(Subscriber subscriber) throws SQLException {

		String sql = """
				INSERT INTO subscriber
				(username, first_name, last_name, email, phone, password_hash)
				VALUES (?, ?, ?, ?, ?, ?)
				""";
		executeWriteQuery(sql,subscriber.getUsername(), subscriber.getFirstName(), subscriber.getLastName(), subscriber.getEmail(), subscriber.getPhone(), subscriber.getPasswordHash());
	}

	/**
	 * Checks subscriber credentials against the DB.
	 *
	 * @param subscriberId subscriber ID
	 * @param rawPassword  subscriber raw password
	 * @return list of reservations if credentials match, null otherwise
	 */
	public boolean subscriberLogin(int subscriberId, String rawPassword) {
		String sql = "SELECT password_hash FROM subscriber WHERE sub_id = ?";
		List<Reservation> reservations = null;
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return false;

		try (PreparedStatement loginStmt = pConn.getConnection().prepareStatement(sql)) {
			loginStmt.setInt(1, subscriberId);
			try (ResultSet rs = loginStmt.executeQuery()) {
				if (!rs.next())
					return false;

				if(!BCrypt.checkpw(rawPassword, rs.getString("password_hash")))
					return false;
			}
		} catch (SQLException e) {
			System.out.println("SQLException: subscriberLogin failed.");
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return true;
	}
	public List<Reservation> getSubscriberHistory(int subscriberId){
		String sql = "SELECT confirmation_code, phone_number, start_time, finish_time, order_date,  order_status, num_diners, date_of_placing_order FROM reservations WHERE sub_id = ?";
		List<List<Object>> rows = executeReadQuery(sql, subscriberId);
		if (rows.isEmpty())
			return null;
		List<Reservation> reservations = new ArrayList<>();
		for(Object obj: rows){
			List<Object> row = (List<Object>) obj;
			Integer confirmationCode = (Integer) row.get(0);
			String phoneNumber = (String) row.get(1);
			LocalTime startTime = (LocalTime) row.get(2);
			LocalTime finishTime = (LocalTime) row.get(3);
			LocalDate orderDate = (LocalDate) row.get(4);
			Status status = Status.valueOf((String) row.get(5));
			Integer numDiners = (Integer) row.get(6);
			LocalDate placingDate = (LocalDate) row.get(7);
			reservations.add(new Reservation(orderDate, numDiners, confirmationCode, subscriberId, placingDate, startTime, finishTime , phoneNumber, status));
		}
		return reservations;
	} 
	/**
	 * Authenticates a worker by username and password.
	 *
	 * @param username    worker username
	 * @param rawPassword worker raw password
	 * @return a Worker on success, or null if authentication fails
	 */
	public Worker workerLogin(String username, String rawPassword) {
		String sql = """
				    SELECT worker_id, username, password_hash, worker_type
				    FROM workers
				    WHERE username = ?
				""";

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setString(1, username);

			ResultSet rs = stmt.executeQuery();
			if (!rs.next())
				return null;

			String storedHash = rs.getString("password_hash");

			// optional sanity prints while debugging:
			// System.out.println("stored len=" + (storedHash == null ? -1 :
			// storedHash.length()));
			// System.out.println("check=" + BCrypt.checkpw(rawPassword, storedHash));

			if (!BCrypt.checkpw(rawPassword, storedHash))
				return null;

			return new Worker(rs.getInt("worker_id"), rs.getString("username"),
					WorkerType.valueOf(rs.getString("worker_type").toUpperCase()));

		} catch (SQLException e) {
			System.out.println("SQLException: workerLogin failed.");
			e.printStackTrace();
			return null;
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * Loads the current diners per table, including reservation details when present.
	 *
	 * @return list of current diner rows (empty if none found)
	 */
	public List<CurrentDinerRow> loadCurrentDiners() {
		String sql = """
				    SELECT
				        t.table_number,
				        r.phone,
				        r.email,
				        r.sub_id,
				        r.num_diners,
				        r.res_id
				    FROM tablestable t
				    LEFT JOIN reservations r
				        ON t.res_id = r.res_id
				        ORDER BY t.table_number ASC;
				""";

		List<CurrentDinerRow> rows = new ArrayList<>();

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return rows;

		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				rows.add(new CurrentDinerRow(rs.getInt("table_number"), rs.getString("phone"), rs.getString("email"),
						rs.getObject("sub_id", Integer.class), rs.getObject("num_diners", Integer.class),
						rs.getObject("res_id", Integer.class)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return rows;
	}
	/**
	 * Fetches the most recent confirmation code and its start time for today's
	 * confirmed reservation tied to the given phone, within the last 15 minutes.
	 *
	 * @param phone phone number to search by.
	 * @return list with confirmation code and start time, or null if none found.
	 */
	public ArrayList<String> getForgotConfirmationCode(String phone) {
		String sql = "SELECT confirmation_code, start_time FROM reservations WHERE phone = ? AND order_date = CURDATE() AND order_status = 'CONFIRMED' AND start_time >= DATE_SUB(CURTIME(), INTERVAL 15 MINUTE) ORDER BY start_time ASC LIMIT 1";
		List<List<Object>> rows = executeReadQuery(sql, phone);
		if (rows.isEmpty())
			return null;
		List<Object> row = rows.get(0);
		Integer code = row.get(0) instanceof Integer ? (Integer) row.get(0) : null;
		String startTime = null;
		Object timeObj = row.get(1);
		if (timeObj instanceof java.sql.Time)
			startTime = ((java.sql.Time) timeObj).toLocalTime().toString();
		else if (timeObj instanceof java.time.LocalTime)
			startTime = ((java.time.LocalTime) timeObj).toString();
		else if (timeObj instanceof String)
			startTime = (String) timeObj;
		if (code == null || startTime == null)
			return null;
		ArrayList<String> result = new ArrayList<>(2);
		result.add(String.valueOf(code));
		result.add(startTime);
		return result;
	}
	/**
	 * Executes a write query with positional parameters.
	 *
	 * @param sql    SQL string with placeholders
	 * @param params parameters to bind in order
	 * @return number of rows affected
	 */
	private int executeWriteQuery(String sql, Object... params) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null)
			return 0;
		// get the actual connection from the class
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				Object p = params[i];
				int idx = i + 1;
				if (p instanceof Integer)
					stmt.setInt(idx, (Integer) p);
				else if (p instanceof String)
					stmt.setString(idx, (String) p);
				else if (p instanceof LocalDate)
					stmt.setDate(1, java.sql.Date.valueOf((LocalDate) p));
				else
					throw new SQLException();
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException: " + "executeWriteQuery failed.");
			e.printStackTrace();
		} finally {
			// Crucial: Return connection to the pool here!
			pool.releaseConnection(pConn);
		}
		return 0;
	}

	/**
	 * Executes a read-only SQL query and returns the results as a list of rows.
	 * Each row is represented as a List<Object> in column.
	 * rs.getObject(c) is used for all columns, so callers are responsible
	 * for casting each value to the expected type.
	 * Supported parameter types: Integer, String, LocalDate.
	 * Example usage:
	 * String sql = "SELECT res_id, phone FROM reservations WHERE phone = ?";
	 * List<List<Object>> rows = executeReadQuery(sql, "0521234567");
	 * for (List<Object> row : rows) {
	 *     int resId = (Integer) row.get(0);
	 *     String phone = (String) row.get(1);
	 * }
	 * Note: add safety checks when you casting!
	 * @param sql    SQL query with positional {@code ?} parameters
	 * @param params parameters to bind in order
	 * @return list of rows; empty list when no results or on failure
	 */
	public List<List<Object>> executeReadQuery(String sql, Object... params) {
	MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
	PooledConnection pConn = pool.getConnection();
	if (pConn == null)
		return new ArrayList<>();
	List<List<Object>> rows = new ArrayList<>();
	try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
		for (int i = 0; i < params.length; i++) {
			Object p = params[i];
			int idx = i + 1;
			if (p instanceof Integer)
				stmt.setInt(idx, (Integer) p);
			else if (p instanceof String)
				stmt.setString(idx, (String) p);
			else if (p instanceof LocalDate)
				stmt.setDate(idx, java.sql.Date.valueOf((LocalDate) p));
			else
				throw new SQLException("Unsupported param type: " + p.getClass());
		}

		ResultSet rs = stmt.executeQuery();
		int colCount = rs.getMetaData().getColumnCount();

		while (rs.next()) {
			List<Object> row = new ArrayList<>(colCount);
			for (int c = 1; c <= colCount; c++) {
				row.add(rs.getObject(c));
			}
			rows.add(row);
		}
	} catch (SQLException e) {
		System.out.println("SQLException: executeReadQuery failed.");
		e.printStackTrace();
		return new ArrayList<>();
	} finally {
		pool.releaseConnection(pConn);
	}
	return rows;
}


	
	public StatusCounts getReservationStatusCountsForBarChart() {
	    String sql =
	        "SELECT " +
	        "  SUM(CASE WHEN order_status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed, " +
	        "  SUM(CASE WHEN order_status = 'PENDING' THEN 1 ELSE 0 END) AS pending, " +
	        "  SUM(CASE WHEN order_status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled " +
	        "FROM reservations";

	    MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
	    PooledConnection pConn = pool.getConnection();
	    if (pConn == null) return null;

	    try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        if (!rs.next()) return new StatusCounts(0, 0, 0);

	        int confirmed = rs.getInt("confirmed");
	        int pending = rs.getInt("pending");
	        int cancelled = rs.getInt("cancelled");

	        return new StatusCounts(confirmed, pending, cancelled);

	    } catch (SQLException e) {
	        System.out.println("SQLException: getReservationStatusCountsForBarChart failed.");
	        e.printStackTrace();
	        return null;
	    } finally {
	        pool.releaseConnection(pConn);
	    }
	}

}
