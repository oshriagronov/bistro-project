package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;
import communication.AvgStayCounts;
import communication.StatusCounts;
import logic.CurrentDinerRow;
import logic.Reservation;
import logic.SpecialDay;
import logic.Status;
import logic.Subscriber;
import logic.Table;
import logic.WeeklySchedule;
import logic.Worker;
import logic.WorkerType;

/**
 * Provides database access helpers for reservations, tables, subscribers, and
 * workers.
 */
public class ConnectionToDB {
	/**
	 * Safely converts an object into a {@link LocalDate}, supporting both
	 * {@link LocalDate} and {@link java.sql.Date} types.
	 *
	 * @param o the object to convert (may be null)
	 * @return a LocalDate if conversion is possible, otherwise null
	 */
	private static LocalDate toLocalDate(Object o) {
		if (o == null)
			return null;
		if (o instanceof LocalDate)
			return (LocalDate) o;
		if (o instanceof java.sql.Date)
			return ((java.sql.Date) o).toLocalDate();
		return null;
	}

	/**
	 * Safely converts an object into a {@link LocalTime}, supporting both
	 * {@link LocalTime} and {@link java.sql.Time} types.
	 *
	 * @param o the object to convert (may be null)
	 * @return a LocalTime if conversion is possible, otherwise null
	 */
	private static LocalTime toLocalTime(Object o) {
		if (o == null)
			return null;
		if (o instanceof LocalTime)
			return (LocalTime) o;
		if (o instanceof java.sql.Time)
			return ((java.sql.Time) o).toLocalTime();
		return null;
	}

	/**
	 * Safely converts an object into an Integer.
	 *
	 * @param o the object to convert (may be null)
	 * @return the Integer value or null if conversion fails
	 */
	private static Integer toInteger(Object o) {
		if (o == null)
			return null;
		if (o instanceof Integer)
			return (Integer) o;
		if (o instanceof Number)
			return ((Number) o).intValue();
		if (o instanceof String) {
			try {
				return Integer.parseInt((String) o);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Safely converts an object into a {@link Status} enum.
	 *
	 * @param o the object to convert (may be null)
	 * @return Status value or null if conversion fails
	 */
	private static Status toStatus(Object o) {
		if (o == null)
			return null;
		try {
			return Status.valueOf(o.toString().trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static Integer toInt(Object o) {
		if (o == null)
			return null;
		if (o instanceof Integer i)
			return i;
		if (o instanceof Long l)
			return Math.toIntExact(l);
		if (o instanceof Number n)
			return n.intValue(); // כולל BigDecimal
		if (o instanceof String s && !s.isBlank())
			return Integer.parseInt(s.trim());
		throw new IllegalArgumentException("Cannot convert to Integer: " + o.getClass());
	}

	private static String toStr(Object o) {
		return o == null ? null : o.toString();
	}

	/**
	 * Maps a raw row from {@link #executeReadQuery} into a {@link Reservation}.
	 * Used by {@link #getOrderByPhoneAndCode(String, int)} and
	 * {@link #getOrderByEmailAndCode(String, int)}.
	 *
	 * @param row row values in the expected reservation column order
	 * @return populated Reservation, or null if required fields are missing
	 */
	private static Reservation buildReservationFromRow(List<Object> row) {
		if (row == null || row.size() < 11)
			return null;

		Integer resId = toInteger(row.get(0));
		String confirmationCode = toStr(row.get(1));
		String phone = row.get(2) == null ? null : row.get(2).toString();
		String email = row.get(3) == null ? null : row.get(3).toString();
		Integer subId = toInteger(row.get(4));
		LocalTime startTime = toLocalTime(row.get(5));
		LocalTime finishTime = toLocalTime(row.get(6));
		LocalDate orderDate = toLocalDate(row.get(7));
		Status status = toStatus(row.get(8));
		Integer diners = toInteger(row.get(9));
		LocalDate placingDate = toLocalDate(row.get(10));

		if (status == null)
			return null;

		Reservation r = new Reservation(orderDate, diners != null ? diners : 0, confirmationCode,
				subId != null ? subId : 0, placingDate, startTime, finishTime, phone, status, email);
		if (resId != null)
			r.setOrderNumber(resId);
		return r;
	}

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
	 * Sets the reservation start time to the current time and the finish time to
	 * two hours after that for the given reservation id.
	 *
	 * @param order_number reservation id to update
	 * @return number of rows affected
	 */
	public int updateReservationTimesAfterAcceptation(int order_number) {
		String sql = "UPDATE reservations SET start_time = CURTIME(), finish_time = ADDTIME(CURTIME(), '02:00:00'), order_status = 'ACCEPTED' WHERE res_id = ?";
		return executeWriteQuery(sql, order_number);
	}

	/**
	 * Sets the reservation start time to the current time and the finish time to
	 * two hours after that for the given reservation id.
	 *
	 * @param order_number reservation id to update
	 * @return number of rows affected
	 */
	public int updateReservationTimesAfterCompleting(int order_number) {
		String sql = "UPDATE `reservations` SET finish_time = CURTIME() WHERE res_id = ?";
		return executeWriteQuery(sql, order_number);
	}
	
	/**
	 * Returns all PENDING reservations ordered by reservation date,
	 * start time, and the time the reservation was placed (oldest first).
	 *
	 * @return ordered list of all pending reservations
	 */
	public List<Reservation> getAllPendingReservationsOrdered() {

	    String sql = "SELECT * FROM reservations "
	               + "WHERE order_status = 'PENDING' "
	               + "ORDER BY order_date ASC, start_time ASC, date_of_placing_order ASC";

	    List<List<Object>> rows = executeReadQuery(sql);

	    List<Reservation> result = new ArrayList<>();

	    for (List<Object> row : rows) {
	        result.add(buildReservationFromRow(row));
	    }

	    return result;
	}



	/**
	 * Cancels a reservation by confirmation code and either email or phone. Uses
	 * phone when email is null; otherwise uses email. Updates order_status to
	 * CANCELLED.
	 *
	 * @param confirmationCode reservation confirmation code
	 * @param email            reservation email; if null, phone is used instead
	 * @param phone            reservation phone; used only when email is null
	 * @return number of rows affected
	 */
	public int CancelReservation(int confirmationCode, String email, String phone) {
		StringBuilder sql = new StringBuilder("UPDATE `reservations` SET order_status = 'CANCELLED' WHERE ");
		if (email == null) {
			sql.append("confirmation_code = ? AND phone = ? limit 1");
			return executeWriteQuery(sql.toString(), confirmationCode, phone);
		} else {
			sql.append("confirmation_code = ? AND email = ? limit 1");
			return executeWriteQuery(sql.toString(), confirmationCode, email);
		}
	}

	/**
	 * Inserts a new reservation into the 'reservations' table. Uses a prepared SQL
	 * statement with placeholders to safely insert all fields.
	 *
	 * @param r the Reservation object containing all reservation details
	 * @return number of affected rows (1 if insert succeeded, 0 if failed)
	 */
	public String insertReservation(Reservation reservation) {
		String insertSql = """
				    INSERT INTO reservations
				    (phone, email, sub_id, start_time, finish_time, order_date, order_status, num_diners, date_of_placing_order)
				    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return null;

		try {
			PreparedStatement insertStmt = pConn.getConnection().prepareStatement(insertSql,
					java.sql.Statement.RETURN_GENERATED_KEYS);

			insertStmt.setString(1, reservation.getPhone_number());
			insertStmt.setString(2, reservation.getEmail());
			insertStmt.setInt(3, reservation.getSubscriberId());
			insertStmt.setTime(4, java.sql.Time.valueOf(reservation.getStart_time()));
			insertStmt.setTime(5, java.sql.Time.valueOf(reservation.getFinish_time()));
			insertStmt.setDate(6, java.sql.Date.valueOf(reservation.getOrderDate()));
			insertStmt.setString(7, reservation.getStatus().name());
			insertStmt.setInt(8, reservation.getNumberOfGuests());
			insertStmt.setDate(9, java.sql.Date.valueOf(reservation.getDateOfPlacingOrder()));

			int affected = insertStmt.executeUpdate();
			if (affected == 0)
				return null;

			Integer resId = null;
			try (ResultSet keys = insertStmt.getGeneratedKeys()) {
				if (keys.next()) {
					resId = keys.getInt(1);
				}
			}
			if (resId == null)
				return null;
			String selectSql = "SELECT confirmation_code FROM reservations WHERE res_id = ?";
			try (PreparedStatement selectStmt = pConn.getConnection().prepareStatement(selectSql)) {
				selectStmt.setInt(1, resId);
				try (ResultSet rs = selectStmt.executeQuery()) {
					if (rs.next()) {
						String confirmationCode = rs.getString("confirmation_code");
						reservation.setOrderNumber(resId);
						reservation.setConfirmation_code(confirmationCode);
						return confirmationCode;
					}
				}
			}

			return null;

		} catch (SQLException e) {
			System.out.println("SQLException: insertReservationAndGetConfirmationCode failed.");
			e.printStackTrace();
			return null;
		} finally {
			pool.releaseConnection(pConn);
		}
	}

	/**
	 * TODO: FIX LATER Finds the first PENDING reservation that fits the table
	 * capacity, confirms it, assigns it to the table, and returns the reservation.
	 *
	 * @param tableNumber the table that just became free
	 * @return the assigned Reservation, or null if none matched
	 */
	// public Reservation assignPendingReservationToTable(int tableNumber) {

	// try {
	// int tableSeats = getTableSeats(tableNumber);

	// String sql = "SELECT * FROM reservations " +
	// "WHERE order_status = 'PENDING' AND num_diners <= ? " +
	// "ORDER BY date_of_placing_order ASC LIMIT 1";

	// PreparedStatement ps = conn.prepareStatement(sql);
	// ps.setInt(1, tableSeats);
	// ResultSet rs = ps.executeQuery();

	// if (!rs.next()) {
	// return null;
	// }

	// Reservation pending = new Reservation(
	// rs.getString("phone"),
	// rs.getString("email"),
	// rs.getInt("sub_id"),
	// rs.getTime("start_time").toLocalTime(),
	// rs.getTime("finish_time").toLocalTime(),
	// rs.getDate("order_date").toLocalDate(),
	// ReservationStatus.valueOf(rs.getString("order_status")),
	// rs.getInt("num_diners"),
	// rs.getDate("date_of_placing_order").toLocalDate()
	// );

	// int resId = pending.getResId();
	// String updateStatus = "UPDATE reservations SET order_status = 'CONFIRMED'
	// WHERE res_id = ?";
	// PreparedStatement ps2 = conn.prepareStatement(updateStatus);
	// ps2.setInt(1, resId);
	// ps2.executeUpdate();

	// String updateTable = "UPDATE tables SET res_id = ? WHERE table_number = ?";
	// PreparedStatement ps3 = conn.prepareStatement(updateTable);
	// ps3.setInt(1, resId);
	// ps3.setInt(2, tableNumber);
	// ps3.executeUpdate();

	// return pending;

	// } catch (SQLException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	/**
	 * Searches for the latest order by phone number and returns the order details.
	 * 
	 * @param phone_number phone number to search by
	 * @return Reservation containing the values returned from the DB, or null if
	 *         not found
	 */
	public Reservation searchOrderByPhoneNumber(String phoneNumber) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ? ORDER BY order_date DESC LIMIT 1";

		List<List<Object>> rows = executeReadQuery(sql, phoneNumber);
		if (rows.isEmpty())
			return null;

		List<Object> row = rows.get(0);

		Integer resId = (Integer) row.get(0);
		String confirmationCode = (String) row.get(1);
		String phone = (String) row.get(2);
		String email = (String) row.get(3);
		Integer subId = (Integer) row.get(4);

		LocalTime startTime = toLocalTime(row.get(5));
		LocalTime finishTime = toLocalTime(row.get(6));
		LocalDate orderDate = toLocalDate(row.get(7));
		Status status = Status.valueOf(((String) row.get(8)).trim());

		Integer diners = (Integer) row.get(9);
		LocalDate placingDate = toLocalDate(row.get(10));

		Reservation r = new Reservation(orderDate, diners != null ? diners : 0, confirmationCode,
				subId != null ? subId : 0, placingDate, startTime, finishTime, phone, status, email);

		if (resId != null)
			r.setOrderNumber(resId);
		return r;
	}

	/**
	 * Searches for all orders by phone number and returns the order details list.
	 * 
	 * @param phone_number phone number to search by
	 * @return list of reservations returned from the DB (empty if none found)
	 */
	public List<Reservation> searchOrdersByPhoneNumberList(String phone) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ? ORDER BY order_date DESC";

		List<List<Object>> rows = executeReadQuery(sql, phone);
		List<Reservation> list = new ArrayList<>();

		for (List<Object> row : rows) {
			Integer resId = toInt(row.get(0));
			String confirmationCode = toStr(row.get(1));
			String phoneNumber = toStr(row.get(2));
			String email = toStr(row.get(3));
			Integer subId = toInt(row.get(4));

			LocalTime startTime = toLocalTime(row.get(5));
			LocalTime finishTime = toLocalTime(row.get(6));
			LocalDate orderDate = toLocalDate(row.get(7));

			Status status = toStatus(row.get(8));
			Integer diners = toInt(row.get(9));
			LocalDate placingDate = toLocalDate(row.get(10));

			Reservation r = new Reservation(orderDate, diners != null ? diners : 0, confirmationCode,
					subId != null ? subId : 0, placingDate, startTime, finishTime, phone, status, email);

			if (resId != null)
				r.setOrderNumber(resId);
			list.add(r);
		}

		return list;
	}

	/**
	 * Searches for all reservations associated with a given email address.
	 * <p>
	 * This method executes a read-only SQL query using {@link #executeReadQuery} to
	 * retrieve all orders whose email field matches the provided value. The results
	 * are ordered by reservation date in descending order (most recent orders
	 * first).
	 * </p>
	 * <p>
	 * Each row returned from the database is mapped into a {@link Reservation}
	 * object. If no matching orders are found, an empty list is returned.
	 * </p>
	 *
	 * @param email the email address to search reservations by
	 * @return a list of {@link Reservation} objects associated with the given
	 *         email; an empty list if no reservations are found
	 */

	public List<Reservation> searchOrdersByEmail(String email) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE email = ? ORDER BY order_date DESC";

		List<List<Object>> rows = executeReadQuery(sql, email);
		List<Reservation> list = new ArrayList<>();

		for (List<Object> row : rows) {
			Integer resId = toInt(row.get(0));
			String confirmationCode = toStr(row.get(1));
			String phoneNumber = toStr(row.get(2));
			String emailValue = toStr(row.get(3));
			Integer subId = toInt(row.get(4));

			LocalTime startTime = toLocalTime(row.get(5));
			LocalTime finishTime = toLocalTime(row.get(6));
			LocalDate orderDate = toLocalDate(row.get(7));

			Status status = toStatus(row.get(8));
			Integer diners = toInt(row.get(9));
			LocalDate placingDate = toLocalDate(row.get(10));

			Reservation res = new Reservation(orderDate, diners != null ? diners : 0,
					confirmationCode, subId != null ? subId : 0, placingDate, startTime,
					finishTime, phoneNumber, status, emailValue);

			if (resId != null)
				res.setOrderNumber(resId);

			list.add(res);
		}

		return list;
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
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
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
					String confirmationCode = rs.getString("confirmation_code");
					int subId = rs.getInt("sub_id");

					Status status = Status.valueOf(rs.getString("order_status"));
					String phone = rs.getString("phone");
					String email = rs.getString("email");

					Reservation r = new Reservation(orderDate, diners, confirmationCode, subId, placingDate, startTime,
							finishTime, phone, status, email);

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
	 * code. Uses {@link #executeReadQuery} and maps the first matching row into a
	 * {@link Reservation}.
	 *
	 * @param phone            the phone number associated with the reservation
	 * @param confirmationCode the confirmation code of the reservation
	 * @return a Reservation object if found, otherwise null
	 */
	public Reservation getOrderByPhoneAndCode(String phone, int confirmationCode, String status) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE phone = ? AND confirmation_code = ? " + "AND order_status = ? "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) "
				+ "AND NOW() <= TIMESTAMPADD(MINUTE, 15, TIMESTAMP(order_date, start_time))";
		List<List<Object>> rows = executeReadQuery(sql, phone, confirmationCode, status);
		return rows.isEmpty() ? null : buildReservationFromRow(rows.get(0));
	}

	/**
	 * Retrieves a reservation from the database using confirmation code only.
	 * Uses {@link #executeReadQuery} and maps the first matching row into a
	 * {@link Reservation}.
	 *
	 * @param confirmationCode the confirmation code of the reservation
	 * @return a Reservation object if found, otherwise null
	 */
	public Reservation getConfirmedReservationByConfirmationCode(int confirmationCode) {
		String sql = "SELECT * "
				+ "FROM reservations WHERE confirmation_code = ? " + "AND order_status = 'CONFIRMED' "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) "
				+ "AND NOW() <= TIMESTAMPADD(MINUTE, 15, TIMESTAMP(order_date, start_time)) LIMIT 1";
		List<List<Object>> rows = executeReadQuery(sql, confirmationCode);
		return rows.isEmpty() ? null : buildReservationFromRow(rows.get(0));
	}

	public Reservation getAcceptedReservationByConfirmationCode(int confirmationCode) {
		String sql = "SELECT * FROM reservations "
				+ "WHERE confirmation_code = ? AND order_status = 'ACCEPTED' "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) "
				+ "AND NOW() <= TIMESTAMP(order_date, finish_time) LIMIT 1";
		List<List<Object>> rows = executeReadQuery(sql, confirmationCode);
		return rows.isEmpty() ? null : buildReservationFromRow(rows.get(0));
	}


	public Reservation getOrderByEmailAndCode(String email, int confirmationCode, String status) {
		String sql = "SELECT res_id, confirmation_code, phone, email, sub_id, start_time, finish_time, "
				+ "       order_date, order_status, num_diners, date_of_placing_order "
				+ "FROM reservations WHERE confirmation_code = ? " + "AND order_status = ? "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) "
				+ "AND NOW() <= TIMESTAMPADD(MINUTE, 15, TIMESTAMP(order_date, start_time))";
		List<List<Object>> rows = executeReadQuery(sql, confirmationCode, status);
		return rows.isEmpty() ? null : buildReservationFromRow(rows.get(0));
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
	public int changeOrderStatus(String phone, int order_number, Status status) {
		String sql = "UPDATE `reservations` SET order_status = ? WHERE phone = ? AND res_id = ?";
		return executeWriteQuery(sql, status.name(), phone, order_number);
	}

	public LocalTime[] getOpeningHours(LocalDate date) {

		// 1. special days
		String sqlSpecial = "SELECT opening_time, closing_time FROM specialdates WHERE date = ?";
		List<List<Object>> rows = executeReadQuery(sqlSpecial, date);
		if (!rows.isEmpty()) {
			return extractTimes(rows.get(0));
		}

		// 2. regular days
		String sqlRegular = "SELECT opening_time, closing_time FROM regulartimes WHERE day = ?";

		String dayName = date.getDayOfWeek().name();
		String dbDay = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();

		rows = executeReadQuery(sqlRegular, dbDay);

		if (!rows.isEmpty()) {
			return extractTimes(rows.get(0));
		}

		return null;
	}

	private LocalTime[] extractTimes(List<Object> row) {
		LocalTime start = toLocalTime(row.get(0));
		LocalTime end = toLocalTime(row.get(1));
		return new LocalTime[] { start, end };
	}

	/**
	 * Executes a write query with positional parameters.
	 *
	 * @param sql    SQL string with placeholders
	 * @param params parameters to bind in order
	 * @return number of rows affected
	 */

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
	 * Returns a list of num_diners for all CONFIRMED reservations on the given date
	 * that intersect the window [time-2h, time+2h].
	 *
	 * Window logic: existing.start_time < (time + 2h) AND existing.finish_time >
	 * (time - 2h)
	 *
	 * @param orderDate the date to check (reservations.order_date)
	 * @param time      the reference time (HH:mm or HH:mm:ss)
	 * @return list of diners counts (num_diners) for matching reservations (empty
	 *         if none)
	 */
	public List<Integer> getNumDinersInTwoHoursWindow(LocalDate orderDate, LocalTime time) {

		String sql = "SELECT num_diners " + "FROM reservations " + "WHERE order_date = ? "
				+ "  AND order_status IN ('CONFIRMED','ACCEPTED') " + "  AND start_time < ADDTIME(TIME(?), '02:00:00') "
				+ "  AND finish_time > TIME(?)";

		String timeStr = time.toString();
		List<List<Object>> rows = executeReadQuery(sql, orderDate, timeStr, timeStr);

		List<Integer> diners = new ArrayList<>();
		for (List<Object> row : rows) {
			if (row == null || row.isEmpty())
				continue;

			Object v = row.get(0);
			if (v instanceof Integer)
				diners.add((Integer) v);
			else if (v instanceof Number)
				diners.add(((Number) v).intValue());
			else if (v != null) {
				try {
					diners.add(Integer.parseInt(v.toString()));
				} catch (NumberFormatException ignore) {
				}
			}
		}
		return diners;
	}

	/**
	 * Searches for the smallest available table that can accommodate the specified
	 * number of guests.
	 *
	 * @param number_of_guests the minimum number of seats required
	 * @return the table number of the best-fitting table, or 0 if none found
	 */
	public int searchAvailableTableBySize(int number_of_guests) {
		String sql = "SELECT table_number FROM tablestable WHERE res_id IS NULL AND size >= ? "
				+ "ORDER BY size ASC, table_number ASC LIMIT 1";
		List<List<Object>> rows = executeReadQuery(sql, number_of_guests);
		if (rows.isEmpty() || rows.get(0).isEmpty()) {
			return 0;
		}
		Object value = rows.get(0).get(0);
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
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

	// TODO: should be instead of changeTableResId
	public int clearTableByResId(int resId) {
		String sql = "UPDATE tablestable SET res_id = NULL WHERE res_id = ?";
		return executeWriteQuery(sql, resId);
	}

	/**
	 * Updates the reservation assignment for a specific table. TODO: check if we
	 * can use only changeTableResId instead
	 * 
	 * @param table_number table whose reservation id should be set
	 * @param res_id       new reservation id (0 clears the table)
	 * @return number of rows affected
	 */
	public int updateTableResId(int table_number, int res_id) {
		String sql = "UPDATE `tablestable` SET res_id=? WHERE table_number = ?";
		return executeWriteQuery(sql, String.valueOf(res_id), table_number);
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
		executeWriteQuery(sql, subscriber.getUsername(), subscriber.getFirstName(), subscriber.getLastName(),
				subscriber.getEmail(), subscriber.getPhone(), subscriber.getPasswordHash());
	}

	/**
	 * Checks subscriber credentials against the DB.
	 *
	 * @param username subscriber username(it's unique)
	 * @param rawPassword  subscriber raw password
	 * @return subscriber id if credentials match, 0 otherwise
	 */
	public int subscriberLogin(String username, String rawPassword) {
		String sql = "SELECT sub_id, password_hash FROM subscriber WHERE username = ?";
		List<List<Object>> rows = executeReadQuery(sql, username);
		if (rows.isEmpty())
			return 0;
		List<Object> row = rows.get(0);
		if (row.size() < 2)
			return 0;
		Object idObj = row.get(0);
		Object hashObj = row.get(1);
		if (!(idObj instanceof Number) || !(hashObj instanceof String))
			return 0;
		int subscriberId = ((Number) idObj).intValue();
		String passwordHash = (String) hashObj;
		return BCrypt.checkpw(rawPassword, passwordHash) ? subscriberId : 0;
	}

	/**
	 * Retrieves all reservations made by a subscriber, filtering out rows that
	 * lack required data or a valid {@link Status}.
	 *
	 * @param subscriberId database id of the subscriber whose history is needed
	 * @return list of reservations created by the subscriber, or {@code null} if
	 *         no rows were found
	 */
	public List<Reservation> getSubscriberHistory(int subscriberId) {
		String sql = "SELECT confirmation_code, phone, start_time, finish_time, order_date, order_status, "
				+ "num_diners, date_of_placing_order, email " + "FROM reservations WHERE sub_id = ?";
		List<List<Object>> rows = executeReadQuery(sql, subscriberId);
		if (rows.isEmpty())
			return null;
		List<Reservation> reservations = new ArrayList<>();
		for (List<Object> row : rows) {
			if (row.size() < 9)
				continue;

			Object confirmationObj = row.get(0);
			Object phoneObj = row.get(1);
			Object startObj = row.get(2);
			Object finishObj = row.get(3);
			Object orderDateObj = row.get(4);
			Object statusObj = row.get(5);
			Object numDinersObj = row.get(6);
			Object placingDateObj = row.get(7);
			Object emailObj = row.get(8);

			if (!(confirmationObj instanceof String) || !(phoneObj instanceof String)
					|| !(startObj instanceof java.sql.Time) || !(finishObj instanceof java.sql.Time)
					|| !(orderDateObj instanceof java.sql.Date) || !(numDinersObj instanceof Integer)
					|| !(placingDateObj instanceof java.sql.Date))
				continue;

			Status status = null;
			if (statusObj instanceof String) {
				String text = ((String) statusObj).trim();
				if (!text.isEmpty()) {
					try {
						status = Status.valueOf(text);
					} catch (IllegalArgumentException e) {
						status = null;
					}
				}
			}
			if (status == null)
				continue;

			String confirmationCode = toStr(confirmationObj);
			String phoneNumber = (String) phoneObj;
			LocalTime startTime = ((java.sql.Time) startObj).toLocalTime();
			LocalTime finishTime = ((java.sql.Time) finishObj).toLocalTime();
			LocalDate orderDate = ((java.sql.Date) orderDateObj).toLocalDate();
			Integer numDiners = (Integer) numDinersObj;
			LocalDate placingDate = ((java.sql.Date) placingDateObj).toLocalDate();
			String email = (emailObj instanceof String) ? (String) emailObj : null;

			reservations.add(new Reservation(orderDate, numDiners, confirmationCode, subscriberId, placingDate,
					startTime, finishTime, phoneNumber, status, email));
		}
		return reservations;
	}

	/**
	 * Updates subscriber details based on the provided subscriber fields. Only
	 * non-null/non-blank values are updated.
	 *
	 * @param subscriber subscriber data to update
	 * @return number of rows affected
	 */
	public int updateSubscriberInfo(Subscriber subscriber) {
		if (subscriber == null || subscriber.getSubscriberId() == null) {
			return 0;
		}

		List<String> assignments = new ArrayList<>();
		List<Object> params = new ArrayList<>();

		addIfPresent(subscriber.getPhone(), "phone = ?", assignments, params);
		addIfPresent(subscriber.getEmail(), "email = ?", assignments, params);
		addIfPresent(subscriber.getUsername(), "username = ?", assignments, params);
		addIfPresent(subscriber.getPasswordHash(), "password_hash = ?", assignments, params);
		addIfPresent(subscriber.getFirstName(), "first_name = ?", assignments, params);
		addIfPresent(subscriber.getLastName(), "last_name = ?", assignments, params);

		if (assignments.isEmpty()) {
			return 0;
		}

		params.add(subscriber.getSubscriberId());
		String sql = "UPDATE subscriber SET " + String.join(", ", assignments) + " WHERE sub_id = ?";
		return executeWriteQuery(sql, params.toArray());
	}

	/**
	 * Adds an assignment clause and parameter when the provided value contains
	 * text.
	 *
	 * @param value   candidate value to set
	 * @param clause  SQL assignment fragment (e.g., "email = ?")
	 * @param clauses target list for SQL fragments
	 * @param params  target list for prepared statement parameters
	 */
	private static void addIfPresent(String value, String clause, List<String> clauses, List<Object> params) {
		if (value != null && !value.trim().isEmpty()) {
			clauses.add(clause);
			params.add(value);
		}
	}

	/**
	 * Retrieves confirmation codes for all confirmed reservations of a subscriber.
	 *
	 * @param subscriberId subscriber identifier
	 * @return list of confirmation codes as strings (empty if none found)
	 */
	public ArrayList<String> getConfirmedReservationCodesBySubscriber(int subscriberId) {
		String sql = "SELECT confirmation_code FROM reservations " + "WHERE sub_id = ? AND order_status = 'CONFIRMED' "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) " + "AND NOW() <= TIMESTAMP(order_date, finish_time)";
		List<List<Object>> rows = executeReadQuery(sql, subscriberId);
		ArrayList<String> codes = new ArrayList<>();
		for (List<Object> row : rows) {
			if (row.isEmpty())
				continue;
			Object codeObj = row.get(0);
			if (codeObj != null) {
				codes.add(codeObj.toString());
			}
		}
		return codes;
	}

	public ArrayList<String> getAcceptedReservationCodeBySubscriber(int subscriberId) {
		String sql = "SELECT confirmation_code FROM reservations " + "WHERE sub_id = ? AND order_status = 'ACCEPTED' "
				+ "AND NOW() >= TIMESTAMP(order_date, start_time) "
				+ "AND NOW() <= TIMESTAMPADD(MINUTE, 15, TIMESTAMP(order_date, start_time))";
		List<List<Object>> rows = executeReadQuery(sql, subscriberId);
		ArrayList<String> codes = new ArrayList<>();
		for (List<Object> row : rows) {
			if (row.isEmpty())
				continue;
			Object codeObj = row.get(0);
			if (codeObj != null) {
				codes.add(codeObj.toString());
			}
		}
		return codes;
	}

	/**
	 * Retrieves a Subscriber object from the database using the subscriber ID. This
	 * method executes a SQL query to fetch all subscriber fields.
	 *
	 * @param sub_id The ID of the subscriber to search for.
	 * @return A fully populated Subscriber object if found, otherwise null.
	 */
	public Subscriber SearchSubscriberById(int sub_id) {
		// SQL query to retrieve all subscriber fields
		String sql = "SELECT sub_id, username, first_name, last_name, email, phone FROM subscriber WHERE sub_id = ?";
		// Execute the query
		List<List<Object>> rows = executeReadQuery(sql, sub_id);
		// If no results found, return null
		if (rows.isEmpty())
			return null;
		List<Object> row = rows.get(0);
		if (row.isEmpty())
			return null;
		// Extract fields from the row
		Integer subscriberId = (Integer) row.get(0);
		String username = (String) row.get(1);
		String firstName = (String) row.get(2);
		String lastName = (String) row.get(3);
		String email = (String) row.get(4);
		String phone = (String) row.get(5);
		String passwordHash = "";
		// Create and return a Subscriber object
		return new Subscriber(subscriberId, username, firstName, lastName, email, phone, passwordHash);
	}

	/**
	 * Retrieves a Subscriber object from the database using the subscriber's phone
	 * number.
	 *
	 * @param phone The phone number of the subscriber to search for.
	 * @return A fully populated Subscriber object if found, otherwise null.
	 */
	public Subscriber SearchSubscriberByPhone(String phone) {
		String sql = "SELECT sub_id, username, first_name, last_name, email, phone FROM subscriber WHERE phone = ?";
		List<List<Object>> rows = executeReadQuery(sql, phone);
		if (rows.isEmpty())
			return null;
		List<Object> row = rows.get(0);
		if (row.isEmpty())
			return null;
		Integer subscriberId = (Integer) row.get(0);
		String username = (String) row.get(1);
		String firstName = (String) row.get(2);
		String lastName = (String) row.get(3);
		String email = (String) row.get(4);
		String phoneNum = (String) row.get(5);
		String passwordHash = "";
		return new Subscriber(subscriberId, username, firstName, lastName, email, phoneNum, passwordHash);
	}

	/**
	 * Retrieves a Subscriber object from the database using the subscriber's email.
	 *
	 * @param email The email of the subscriber to search for.
	 * @return A fully populated Subscriber object if found, otherwise null.
	 */
	public Subscriber SearchSubscriberByEmail(String email) {
		String sql = "SELECT sub_id, username, first_name, last_name, email, phone FROM subscriber WHERE email = ?";
		List<List<Object>> rows = executeReadQuery(sql, email);
		if (rows.isEmpty())
			return null;
		List<Object> row = rows.get(0);
		if (row.isEmpty())
			return null;
		Integer subscriberId = (Integer) row.get(0);
		String username = (String) row.get(1);
		String firstName = (String) row.get(2);
		String lastName = (String) row.get(3);
		String emailAddr = (String) row.get(4);
		String phone = (String) row.get(5);
		String passwordHash = "";
		return new Subscriber(subscriberId, username, firstName, lastName, emailAddr, phone, passwordHash);
	}

	/**
	 * Retrieves a worker object from the database using the worker ID. This method
	 * executes a SQL query to fetch all worker fields.
	 *
	 * @param worker_id The ID of the worker to search for.
	 * @return A fully populated Worker object if found, otherwise null.
	 */
	public Worker SearchWorkerById(int worker_id) {

		// SQL query to retrieve all worker fields
		String sql = "SELECT * FROM workers WHERE worker_id = ?";

		// Execute the query
		List<List<Object>> rows = executeReadQuery(sql, worker_id);

		// If no results found, return null
		if (rows.isEmpty())
			return null;

		List<Object> row = rows.get(0);

		if (row.isEmpty())
			return null;

		// Extract fields from the row
		Integer workerid = (Integer) row.get(0);
		String username = (String) row.get(1);
		// String passwordHash = (String) row.get(2);
		Enum<WorkerType> workerType = WorkerType.valueOf(((String) row.get(3)).trim().toUpperCase());

		// return new Worker(workerid, username, passwordHash, (WorkerType) workerType);
		return new Worker(workerid, username, (WorkerType) workerType);
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
	 * Loads the current diners per table, including reservation details when
	 * present.
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
	 * confirmed reservation tied to the given phone or email, within the last 15
	 * minutes.
	 *
	 * @param identifier phone number or email address to search by.
	 * @return list with confirmation code and start time, or null if none found.
	 */
	public ArrayList<String> getForgotConfirmationCode(String identifier) {
		if (identifier == null || identifier.trim().isEmpty())
			return null;
		String value = identifier.trim();
		boolean isEmail = value.contains("@");
		String field = isEmail ? "email" : "phone";
		String sql = "SELECT confirmation_code, start_time FROM reservations WHERE " + field
				+ " = ? AND order_date = CURDATE() AND order_status = 'CONFIRMED' AND start_time >= DATE_SUB(CURTIME(), INTERVAL 15 MINUTE) ORDER BY start_time ASC LIMIT 1";
		List<List<Object>> rows = executeReadQuery(sql, value);
		if (rows.isEmpty()) {
			return null;
		}
		List<Object> row = rows.get(0);
		Integer code = row.get(0) instanceof String ? toInteger(row.get(0)) : null;
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

	// Notification service related methods
	// ************************************************

	/**
	 * Retrieves upcoming confirmed reservations within the next two hours for
	 * reminders.
	 *
	 * @return list of rows as strings (res_id, phone, email, start_time,
	 *         confirmation_code), or null if none found
	 */
	public List<List<String>> getReservationToSendReminder() {
		String sql = """
				    SELECT res_id, phone, email, start_time, confirmation_code
				    FROM reservations
				    WHERE order_status = 'CONFIRMED'
				      AND TIMESTAMP(order_date, start_time) >= NOW()
				      AND TIMESTAMP(order_date, start_time) <= TIMESTAMPADD(MINUTE, 120, NOW())
				""";
		List<List<Object>> rows = executeReadQuery(sql);
		if (rows.isEmpty())
			return null;
		List<List<String>> out = new ArrayList<>(rows.size());
		for (List<Object> row : rows) {
			List<String> outRow = new ArrayList<>(row.size());
			for (Object cell : row) {
				outRow.add(cell == null ? null : cell.toString());
			}
			out.add(outRow);
		}
		return out;
	}

	/**
	 * Retrieves confirmed reservations that have finished and are past due for
	 * payment reminders.
	 *
	 * @return list of rows as strings (res_id, phone, email), or null if none found
	 */
	public List<List<String>> getReservationToSendPaymentReminder() {
		String sql = """
				    SELECT res_id, phone, email
				    FROM reservations
				    WHERE order_status = 'CONFIRMED'
				      AND finish_time IS NOT NULL
				      AND TIMESTAMP(order_date, finish_time) < NOW()
				""";
		List<List<Object>> rows = executeReadQuery(sql);
		if (rows.isEmpty())
			return null;
		List<List<String>> out = new ArrayList<>(rows.size());
		for (List<Object> row : rows) {
			List<String> outRow = new ArrayList<>(row.size());
			for (Object cell : row) {
				outRow.add(cell == null ? null : cell.toString());
			}
			out.add(outRow);
		}
		return out;
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
					stmt.setDate(idx, java.sql.Date.valueOf((LocalDate) p));
				else if (p instanceof java.sql.Date)
					stmt.setDate(idx, (java.sql.Date) p);
				else if (p instanceof java.sql.Time)
					stmt.setTime(idx, (java.sql.Time) p);
				else if (p instanceof LocalTime)
					stmt.setTime(idx, java.sql.Time.valueOf((LocalTime) p));

				else
					throw new SQLException();
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("SQLException: " + "executeWriteQuery failed.");
			// e.printStackTrace();
		} finally {
			// Crucial: Return connection to the pool here!
			pool.releaseConnection(pConn);
		}
		return 0;
	}

	/**
	 * Executes a read-only SQL query and returns the results as a list of rows.
	 * Each row is represented as a List<Object> in column. rs.getObject(c) is used
	 * for all columns, so callers are responsible for casting each value to the
	 * expected type. Supported parameter types: Integer, String, LocalDate. Example
	 * usage: String sql = "SELECT res_id, phone FROM reservations WHERE phone = ?";
	 * List<List<Object>> rows = executeReadQuery(sql, "0521234567"); for
	 * (List<Object> row : rows) { int resId = (Integer) row.get(0); String phone =
	 * (String) row.get(1); } Note: add safety checks when you casting!
	 * 
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

	public List<StatusCounts> getDailySlotStats(int year, int month) {
		String sql = "SELECT " + "DAY(order_date) AS day_in_month, "
				+ "  SUM(CASE WHEN order_status IN ('ACCEPTED','COMPLETED') "
				+ "           AND MOD(MINUTE(start_time), 30) = 0 THEN 1 ELSE 0 END) AS on_time, "
				+ "  SUM(CASE WHEN order_status IN ('ACCEPTED','COMPLETED') "
				+ "           AND MOD(MINUTE(start_time), 30) <> 0 THEN 1 ELSE 0 END) AS late, "
				+ "  SUM(CASE WHEN order_status = 'CANCELLED' "
				+ "           AND MOD(MINUTE(start_time), 30) > 15 THEN 1 ELSE 0 END) AS cancelled "
				+ "FROM reservations " + "WHERE YEAR(order_date) = ? AND MONTH(order_date) = ? "
				+ "GROUP BY DAY(order_date) " + "ORDER BY DAY(order_date)";

		List<StatusCounts> out = new ArrayList<>();

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return out;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setInt(1, year);
			stmt.setInt(2, month);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int day = rs.getInt("day_in_month");
					out.add(new StatusCounts(year, month, day, rs.getInt("on_time"), rs.getInt("late"),
							rs.getInt("cancelled")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return out;
	}

	public List<AvgStayCounts> getDailyAverageStay(int year, int month) {
		String sql = "SELECT " + "  DAY(order_date) AS day_in_month, " + "  AVG(TIMESTAMPDIFF(MINUTE, "
				+ "      TIMESTAMP(order_date, start_time), " + "      TIMESTAMP(order_date, finish_time) "
				+ "  )) AS avg_stay_minutes " + "FROM reservations "
				+ "WHERE YEAR(order_date) = ? AND MONTH(order_date) = ? " + "  AND order_status = 'COMPLETED' "
				+ "  AND start_time IS NOT NULL " + "  AND finish_time IS NOT NULL " + "GROUP BY DAY(order_date) "
				+ "ORDER BY DAY(order_date)";

		List<AvgStayCounts> out = new ArrayList<>();

		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
		PooledConnection pConn = pool.getConnection();
		if (pConn == null)
			return out;

		try (PreparedStatement stmt = pConn.getConnection().prepareStatement(sql)) {
			stmt.setInt(1, year);
			stmt.setInt(2, month);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int day = rs.getInt("day_in_month");
					out.add(new AvgStayCounts(year, month, day, rs.getDouble("avg_stay_minutes")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			pool.releaseConnection(pConn);
		}

		return out;
	}

	public List<WeeklySchedule> loadRegularTimes() {
		String sql = """
				SELECT day, opening_time, closing_time
				FROM regulartimes
				ORDER BY FIELD(day, 'Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday')
				""";

		List<List<Object>> rows = executeReadQuery(sql);
		List<WeeklySchedule> out = new ArrayList<>();

		for (List<Object> row : rows) {
			if (row.size() < 3)
				continue;

			String dayStr = row.get(0) == null ? null : row.get(0).toString();
			DayOfWeek day = toDayOfWeek(dayStr);

			LocalTime open = toLocalTime(row.get(1));
			LocalTime close = toLocalTime(row.get(2));

			if (day != null) {
				out.add(new WeeklySchedule(day, open, close));
			}
		}

		return out;
	}

	private static DayOfWeek toDayOfWeek(String dbDay) {
		if (dbDay == null)
			return null;

		return switch (dbDay.trim()) {
		case "Sunday" -> DayOfWeek.SUNDAY;
		case "Monday" -> DayOfWeek.MONDAY;
		case "Tuesday" -> DayOfWeek.TUESDAY;
		case "Wednesday" -> DayOfWeek.WEDNESDAY;
		case "Thursday" -> DayOfWeek.THURSDAY;
		case "Friday" -> DayOfWeek.FRIDAY;
		case "Saturday" -> DayOfWeek.SATURDAY;
		default -> null;
		};
	}

	public int updateRegularDayTimes(String day, LocalTime opening, LocalTime closing) {
		String sql = "UPDATE regulartimes SET opening_time = ?, closing_time = ? WHERE day = ?";
		return executeWriteQuery(sql, opening, closing, day);
	}

	public int updateSpecialDay(LocalDate day, LocalTime opening, LocalTime closing) {
		String sql = """
				    INSERT INTO specialdates (date, opening_time, closing_time)
				    VALUES (?, ?, ?)
				    ON DUPLICATE KEY UPDATE
				        opening_time = VALUES(opening_time),
				        closing_time = VALUES(closing_time)
				""";

		return executeWriteQuery(sql, day, opening, closing);
	}

	public List<SpecialDay> loadUpcomingSpecialDates(int limit) {
		String sql = """
				SELECT date, opening_time, closing_time
				FROM specialdates
				WHERE date >= CURDATE()
				ORDER BY date ASC
				LIMIT ?
				""";

		List<List<Object>> rows = executeReadQuery(sql, limit);

		List<SpecialDay> out = new ArrayList<>();
		for (List<Object> row : rows) {
			if (row.size() < 3)
				continue;

			LocalDate date = toLocalDate(row.get(0));
			LocalTime open = toLocalTime(row.get(1));
			LocalTime close = toLocalTime(row.get(2));

			if (date == null)
				continue;

			out.add(new SpecialDay(date, open, close));
		}
		return out;
	}

}
