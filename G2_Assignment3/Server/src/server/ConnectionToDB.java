package server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import logic.Reservation;
public class ConnectionToDB {
	private static String DB_PASSWORD;

	public static String getDbPassword() {
		return DB_PASSWORD;
	}
	
    public static void setPassword(String password) {
        DB_PASSWORD = password;
    }

	/**
	 * This method update existing order by the order number(pk), fields that are going to be update are: order_date, number_of_guests
	 * @param order_number int type
	 * @param order_date LocalDate type, need to use java.sql.Date.valueOf method to get valid value for mySQL table
	 * @param number_of_guests int type
	 * @return number bigger then 1 if succeed or 0 if failed
	 */
	public int updateOrder(int order_number ,LocalDate order_date, int number_of_guests) {
		// the two line bellow are needed to use the pool connection
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		try {
			// get connection from the pull
			pConn = pool.getConnection();
			if (pConn == null) return 0;
			// get the actual connection from the class
            Connection conn = pConn.getConnection();
			PreparedStatement stmt = conn.prepareStatement("UPDATE `Order` SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
			stmt.setDate(1, java.sql.Date.valueOf(order_date));
			stmt.setInt(2, number_of_guests);
			stmt.setInt(3, order_number);
			return stmt.executeUpdate();
		}
		catch(SQLException e){
			System.out.println("SQLException: " + "updateOrder failed.");
			e.printStackTrace();
			return 0;
		}
		finally {
            // Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
	}
	/**
	 * This method Search order by the phone number, and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByPhoneNumber(String phone_number) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		String sql = "SELECT order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE phone_number = ?";
		try {
			pConn = pool.getConnection();
			if (pConn == null) return 0;
            Connection conn = pConn.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, phone_number); 
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				orderDate = LocalDate.parse(rs.getString("order_date"));
				numberOfGuests = rs.getInt("number_of_guests");
				confirmationCode = rs.getInt("confirmation_code");
				subscriberId = rs.getInt("subscriber_id");
				DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
				return new Reservation(orderDate, numberOfGuests, confirmationCode, subscriberId, DateOfPlacingOrder,phone_number);
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
	 * This method Search order by the phone number, and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return ArrayList<Reservation> that hold the values that returned from the DB.
	 */
	public List<Reservation> searchOrdersByPhoneNumberList(String phone_number) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
	    List<Reservation> reservations = new ArrayList<>();
	    LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
	    String sql = "SELECT order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order " +
	                 "FROM `Order` WHERE phone_number = ?";
		try {
			pConn = pool.getConnection();
			if (pConn == null) return null;
            Connection conn = pConn.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, phone_number);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            orderDate = LocalDate.parse(rs.getString("order_date"));
	            numberOfGuests = rs.getInt("number_of_guests");
	            confirmationCode = rs.getInt("confirmation_code");
	            subscriberId = rs.getInt("subscriber_id");
	            DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
	            Reservation reservation = new Reservation(orderDate, numberOfGuests, confirmationCode, subscriberId, DateOfPlacingOrder, phone_number);
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
	 * This method Search order by the order number(pk), and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByOrderNumber(int order_number) {
		MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		String phone_number;
		String sql = "SELECT order_date, number_of_guests,confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE order_number = ?;";
		try {
			pConn = pool.getConnection();
			if (pConn == null) return 0;
			Connection conn = pConn.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1,order_number); 
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				orderDate = LocalDate.parse(rs.getString("order_date"));
				numberOfGuests = rs.getInt("number_of_guests");
				confirmationCode = rs.getInt("confirmation_code");
				subscriberId = rs.getInt("subscriber_id");
				DateOfPlacingOrder = LocalDate.parse(rs.getString("date_of_placing_order"));
				phone_number=rs.getString("phone_number");
				return new Reservation(orderDate, numberOfGuests, confirmationCode, subscriberId, DateOfPlacingOrder,phone_number);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
				finally {
            // Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
		return null;
	}

	//template for read queries from DB table(if we have multiply sql queries from the same table) helper method to avoid repeating code
	/*
    private String executeReadQuery(String sql) {
        MySQLConnectionPool pool = MySQLConnectionPool.getInstance();
        PooledConnection pConn = null;
        StringBuilder result = new StringBuilder();
		
        try {
            pConn = pool.getConnection();
            if (pConn == null) return "Error: Database Down";
			
            Connection conn = pConn.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.append(rs.getString("username"))
				.append(" - ")
				.append(rs.getString("role"))
				.append("\n");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "DB Error: " + e.getMessage();
        } finally {
            // Crucial: Return connection to the pool here!
            pool.releaseConnection(pConn);
        }
        
        return result.toString();
    }
	*/
	
}
