package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logic.Reservation;
import logic.Status;
import logic.Table;
import logic.TableStatus;

public class ConnectionToDB {
	private static String DB_PASSWORD;

	public static String getDbPassword() {
		return DB_PASSWORD;
	}

	public static void setPassword(String password) {
		DB_PASSWORD = password;
	}

	/**
	 * This method update existing order by the order number(pk), fields that are
	 * going to be update are: order_date, number_of_guests
	 * 
	 * @param order_number     int type
	 * @param order_date       LocalDate type, need to use java.sql.Date.valueOf
	 *                         method to get valid value for mySQL table
	 * @param number_of_guests int type
	 * @return number bigger then 1 if succeed or 0 if failed
	 */
	public int updateOrder(int order_number, LocalDate order_date, int number_of_guests) {
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		try {
			// get connection from the pull
			pConn = pool.getConnection();
			if (pConn == null) return 0;
			// get the actual connection from the class
			PreparedStatement stmt = pConn.getConnection()
					.prepareStatement("UPDATE `Order` SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
			stmt.setDate(1, java.sql.Date.valueOf(order_date));
			stmt.setInt(2, number_of_guests);
			stmt.setInt(3, order_number);
			return stmt.executeUpdate();
		}catch(SQLException e){
			System.out.println("SQLException: " + "updateOrder failed.");
			e.printStackTrace();
		}
		finally {
			// Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
		return 0;
	}
	/**
	 * This method Search order by the phone number, and return the value of :
	 * order_date, number_of_guests
	 * 
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByPhoneNumber(String phone_number) {
		String sql = "SELECT order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE phone_number = ?";
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
	 * This method Search order by the phone number, and return the value of :
	 * order_date, number_of_guests
	 * 
	 * @param order_number int type
	 * @return ArrayList<Reservation> that hold the values that returned from the
	 *         DB.
	 */
	public List<Reservation> searchOrdersByPhoneNumberList(String phone_number) {
		String sql = "SELECT order_number ,order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, order_status "
				+ "FROM `Order` WHERE phone_number = ?";
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

	public List<Table> loadTables() {
	    String sql = "SELECT table_number, table_size, table_status FROM `tables`";
	    List<Table> tables = new ArrayList<>();
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		// get connection from the pull
		pConn = pool.getConnection();
		if (pConn == null) return null;
	    try{
			PreparedStatement stmt = pConn.getConnection().prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            int tableNumber = rs.getInt("table_number");
	            int tableSize = rs.getInt("table_size");
	            TableStatus status = TableStatus.valueOf(rs.getString("table_status"));
	            tables.add(new Table(tableNumber, tableSize, status));
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		finally {
			// Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
	    return tables;
	}

	/**
	 * This method Search order by the order number(pk), and return the value of :
	 * order_date, number_of_guests
	 * 
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByOrderNumber(int order_number) {
		String sql = "SELECT order_date, number_of_guests,confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE order_number = ?;";
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
	 * Deletes an order from the DB by order number (primary key)
	 * 
	 * @param order_number order number to delete
	 * @return number of rows affected (1 = success, 0 = not found)
	 */
	public int deleteOrderByOrderNumber(int order_number) {
		String sql = "DELETE FROM `Order` WHERE order_number = ?";
		return executeWriteQuery(sql, order_number);
	}

	public int changeOrderStatus(int order_number, Status status) {
		String sql = "UPDATE `order` SET order_status = ? WHERE order_number = ?";
		return executeWriteQuery(sql, status.name(), order_number);
	}
	
	public int changeTableStatus(int table_number, TableStatus status) {
		String sql = "UPDATE `tables` SET table_status = ? WHERE table_number = ?";
		return executeWriteQuery(sql, status.name(), table_number);
	}
	
	public int changeTableSize(int table_number, int table_size) {
		String sql = "UPDATE `tables` SET table_size = ? WHERE table_number = ?";
		return executeWriteQuery(sql, table_number, table_size);
	}


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
