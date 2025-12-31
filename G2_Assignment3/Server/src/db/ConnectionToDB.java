package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import logic.CurrentDinerRow;
import logic.Reservation;
import logic.Status;
import logic.Subscriber;
import logic.Table;
import logic.TableStatus;
import logic.Worker;
import logic.WorkerType;

import java.sql.Statement;

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
	 * Updates an existing order by order number (primary key).
	 * Fields updated: order_date, number_of_guests.
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
	 * @return Reservation containing the values returned from the DB, or null if not found
	 */
	public Reservation searchOrderByPhoneNumber(String phone_number) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
	               + "order_date, order_status, num_of_diners, date_of_placing_order, order_number "
	               + "FROM reservations WHERE phone = ?";

		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return null;
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setString(1, phone_number);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				orderDate = LocalDate.parse(rs.getString("order_date"));
				numberOfGuests = rs.getInt("number_of_guests");
				confirmationCode = rs.getInt("confirmation_code");
				subscriberId = rs.getInt("subscriber_id");
				DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
				return new Reservation(orderDate, numberOfGuests, confirmationCode, subscriberId, DateOfPlacingOrder,
						phone_number);
			}
		} catch (SQLException e) {
			System.out.println("SQLException: " + "searchOrder failed.");
			e.printStackTrace();
		}
		finally {
			// Crucial: Return connection to the pool here!
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
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
	               + "order_date, order_status, num_of_diners, date_of_placing_order, order_number "
	               + "FROM reservations WHERE phone = ? ";

		List<Reservation> reservations = new ArrayList<>();
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int orderNumber;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		Status status;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return null;
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setString(1, phone_number);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				orderNumber = rs.getInt("order_number");
				orderDate = LocalDate.parse(rs.getString("order_date"));
				numberOfGuests = rs.getInt("number_of_guests");
				confirmationCode = rs.getInt("confirmation_code");
				subscriberId = rs.getInt("subscriber_id");
				status = Status.valueOf(rs.getString("order_status"));
				DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
				Reservation reservation = new Reservation(orderDate, orderNumber, numberOfGuests, confirmationCode,
						subscriberId, DateOfPlacingOrder, phone_number, status);
				reservations.add(reservation);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }

		return reservations;
	}
	/**
	 * Searches for an order by order number (primary key) and returns the order details.
	 * 
	 * @param order_number order number to search by
	 * @return Reservation containing the values returned from the DB, or null if not found
	 */
	public Reservation searchOrderByOrderNumber(int order_number) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
	               + "order_date, order_status, num_of_diners, date_of_placing_order, order_number "
	               + "FROM reservations WHERE order_number = ?;";
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		String phone_number;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return null;
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setInt(1, order_number);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				orderDate = LocalDate.parse(rs.getString("order_date"));
				numberOfGuests = rs.getInt("number_of_guests");
				confirmationCode = rs.getInt("confirmation_code");
				subscriberId = rs.getInt("subscriber_id");
				DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
				phone_number = rs.getString("phone_number");
				return new Reservation(orderDate, numberOfGuests, confirmationCode, subscriberId, DateOfPlacingOrder,
						phone_number);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Retrieves a reservation from the database using phone number and confirmation code.
	 *
	 * @param phone the phone number associated with the reservation
	 * @param confirmationCode the confirmation code of the reservation
	 * @return a Reservation object if found, otherwise null
	 */
	public Reservation getOrderByPhoneAndCode(String phone, int confirmationCode) {
	    String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
	               + "order_date, order_status, num_of_diners, date_of_placing_order, order_number "
	               + "FROM reservations WHERE phone = ? AND confirmation_code = ?";

	    Reservation reservation = null;
	    MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
	    PooledConnection pConn = pool.getConnection();
	    if (pConn == null) return null;

	    try {
	        PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
	        stmt.setString(1, phone);
	        stmt.setInt(2, confirmationCode);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            int orderNumber = rs.getInt("order_number");
	            LocalDate orderDate = LocalDate.parse(rs.getString("order_date"));
	            int numberOfGuests = rs.getInt("num_of_diners");
	            int subscriberId = rs.getInt("sub_id");
	            Status status = Status.valueOf(rs.getString("order_status"));
	            LocalDate placingDate = LocalDate.parse(rs.getString("date_of_placing_order"));

	            reservation = new Reservation(orderDate, orderNumber, numberOfGuests, confirmationCode,
	                                          subscriberId, placingDate, phone, status);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        pool.releaseConnection(pConn);
	    }

	    return reservation;
	}

	/**
	 * Retrieves the active order number (reservation ID) associated with a given table number.
	 * 
	 * @param tableNumber the table number to check
	 * @return the order number (reservation ID) currently assigned to the table, or 0 if not found/empty
	 */
	public int getOrderNumberByTableNumber (int tableNumber){
		String sql = "SELECT res_id FROM `tablestable` WHERE table_number = ?";
		int orderNumber = 0;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return orderNumber;
		try {
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setInt(1, tableNumber);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				orderNumber = rs.getInt("res_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
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
	 * @param status new order status
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
	    if (pConn == null) return new ArrayList<>();

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
	 * Searches for an available table that can accommodate the specified number of guests.
	 * 
	 * @param number_of_guests the minimum number of seats required
	 * @return the table number of a suitable table, or 0 if no such table is found
	 */
	public int searchAvailableTableBySize(int number_of_guests){
		String sql = "SELECT table_number, table_size FROM `tablestable` WHERE res_id IS NULL AND table_size >= ?";
		int min = 10;
		int table_number = 0;
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) 
			return table_number; //TODO: numbers of tables start from 1
		try{
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			stmt.setInt(1, number_of_guests);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int tableNumber = rs.getInt("table_number");
				int tableSize = rs.getInt("table_size");
				if (tableSize < min){
					table_number = tableNumber;
				}
					
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			// Crucial: Return connection to the pool here!
			pool.releaseConnection(pConn);
		}
		return table_number;
	}

	/**
	 * Updates the size for a table by table number.
	 *
	 * @param table_number table number to update
	 * @param table_size new table size
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int changeTableSize(int table_number, int table_size) {
	    String sql = "UPDATE `tablestable` SET size = ? WHERE table_number = ?";
	    return executeWriteQuery(sql, String.valueOf(table_size), table_number);
	}
	
	/**
	 * Updates the reservation ID (res_id) of a table to 0, effectively clearing
	 * any active reservation for that table.
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
	 * @param tableNumber the table number (primary key)
	 * @param tableSize number of seats
	 * @param status initial table status
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
	 * @throws SQLException when the insert fails or no pooled connection is available
	 */
	public void addSubscriber(Subscriber subscriber) throws SQLException {
		
		String sql = """
			INSERT INTO subscriber
			(username, first_name, last_name, email, phone, password_hash)
			VALUES (?, ?, ?, ?, ?, ?)
			""";
			
			MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
			PooledConnection pConn = null;
			
			try {
				pConn = pool.getConnection();
				if (pConn == null) throw new SQLException("No connection available from pool");
				
				try (PreparedStatement ps = pConn.getConnection()
				.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
					
					ps.setString(1, subscriber.getUsername());
					ps.setString(2, subscriber.getFirstName());
					ps.setString(3, subscriber.getLastName());
					ps.setString(4, subscriber.getEmail());
					ps.setString(5, subscriber.getPhone());
					ps.setString(6, subscriber.getPasswordHash());
					
					ps.executeUpdate();
					
					// set generated sub_id back into object
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (rs.next()) {
							subscriber.setSubscriberId(rs.getInt(1));
						}
					}
				}
				
			} finally {
				pool.releaseConnection(pConn);
			}
		}

	/**
	 * Checks subscriber credentials against the DB.
	 *
	 * @param username subscriber username
	 * @param passwordHash hashed password
	 * @return list of reservations if credentials match, null otherwise
	 */
	public List<Reservation> subscriberLogin(String username, String passwordHash) {
		String loginSql = "SELECT sub_id FROM subscriber WHERE username = ? AND password_hash = ?";
		String ordersSql = "SELECT res_id, order_date, num_diners, confirmation_code, sub_id, date_of_placing_order, phone, order_status FROM reservations WHERE sub_id = ?";
		List<Reservation> reservations = null;

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null) return null;

		try {
			PreparedStatement loginStmt = pConn.getConnection().prepareStatement(loginSql);
			loginStmt.setString(1, username);
			loginStmt.setString(2, passwordHash);
			ResultSet loginRs = loginStmt.executeQuery();

			if (loginRs.next()) {
				int subId = loginRs.getInt("sub_id");
				reservations = new ArrayList<>();

				PreparedStatement ordersStmt = pConn.getConnection().prepareStatement(ordersSql);
				ordersStmt.setInt(1, subId);
				ResultSet ordersRs = ordersStmt.executeQuery();

				while (ordersRs.next()) {
					reservations.add(new Reservation(
						LocalDate.parse(ordersRs.getString("order_date")),
						ordersRs.getInt("res_id"),
						ordersRs.getInt("num_diners"),
						ordersRs.getInt("confirmation_code"),
						ordersRs.getInt("sub_id"),
						LocalDate.parse(ordersRs.getString("date_of_placing_order")),
						ordersRs.getString("phone"),
						Status.valueOf(ordersRs.getString("order_status"))
					));
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException: subscriberLogin failed.");
			e.printStackTrace();
			return null;
		} finally {
			pool.releaseConnection(pConn);
		}
		return reservations;
	}
	
	public Worker workerLogin(String username, String rawPassword) {
	    String sql = """
	        SELECT worker_id, username, password_hash, worker_type 
	        FROM workers
	        WHERE username = ?
	    """;

	    MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
	    PooledConnection pConn = pool.getConnection();
	    if (pConn == null) return null;

	    try {
	        PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
	        if (!rs.next()) return null;

	        String storedHash = rs.getString("password_hash");

	        // optional sanity prints while debugging:
	        // System.out.println("stored len=" + (storedHash == null ? -1 : storedHash.length()));
	        // System.out.println("check=" + BCrypt.checkpw(rawPassword, storedHash));

	        if (!BCrypt.checkpw(rawPassword, storedHash)) return null;

	        return new Worker(
	            rs.getInt("worker_id"),
	            rs.getString("username"),
	            WorkerType.valueOf(rs.getString("worker_type").toUpperCase())
	        );

	    } catch (SQLException e) {
	        System.out.println("SQLException: workerLogin failed.");
	        e.printStackTrace();
	        return null;
	    } finally {
	        pool.releaseConnection(pConn);
	    }
	}
	
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
	    if (pConn == null) return rows;

	    try {
	        PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            rows.add(new CurrentDinerRow(
	                rs.getInt("table_number"),
	                rs.getString("phone"),
	                rs.getString("email"),
	                rs.getObject("sub_id", Integer.class),
	                rs.getObject("num_diners", Integer.class),
	                rs.getObject("res_id", Integer.class)
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        pool.releaseConnection(pConn);
	    }

	    return rows;
	}


	/**
	 * Executes a write query with positional parameters.
	 *
	 * @param sql SQL string with placeholders
	 * @param params parameters to bind in order
	 * @return number of rows affected
	 */
	private int executeWriteQuery(String sql, Object... params){
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return 0;
		// get the actual connection from the class
		Connection conn = pConn.getConnection();
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				Object p = params[i];
				int idx = i + 1;
				if (p instanceof Integer) stmt.setInt(idx, (Integer) p);
				else if (p instanceof String) stmt.setString(idx, (String) p);
				else if(p instanceof LocalDate) stmt.setDate(1, java.sql.Date.valueOf((LocalDate)p));
				else throw new SQLException();
        	}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException: " + "executeWriteQuery failed.");
			e.printStackTrace();
		}
		finally {
            // Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
		return 0;
	}

}
