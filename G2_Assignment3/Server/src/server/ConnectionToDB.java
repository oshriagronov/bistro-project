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
	private static String DB_PASSWORD = "6911";
	private static ConnectionToDB connectionToDB = null;
	private Connection conn;
	private ConnectionToDB() 
	{
        try 
        {
        	// "password" argument is for the db password.
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys", "root", DB_PASSWORD);
            System.out.println("SQL connection succeed");
     	} catch (SQLException ex) 
     	    {/* handle any errors*/
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            }
   	}

	public static ConnectionToDB getConnInstance() {
		if(connectionToDB == null) {
			connectionToDB = new ConnectionToDB();
		}
		return connectionToDB;
	}
	
	public String getDbPassword() {
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
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("UPDATE `Order` SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
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
	}
	/**
	 * This method Search order by the phone number, and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByPhoneNumber(String phone_number) {
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		// array list of reservation for example
// 		 return new ArrayList<Reservation>(Arrays.asList(
//     new Reservation(
//         LocalDate.of(2024, 5, 10), 5001, 4, 742001, 21111,
//         LocalDate.of(2024, 4, 20), "555-0100"),
//     new Reservation(
//         LocalDate.of(2024, 5, 12), 5002, 2, 742002, 21112,
//         LocalDate.of(2024, 4, 22), "555-0101")
// ));
		String sql = "SELECT order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE phone_number = ?";
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(sql);
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
		return null;
	}
	/**
	 * This method Search order by the phone number, and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return ArrayList<Reservation> that hold the values that returned from the DB.
	 */
	public List<Reservation> searchOrdersByPhoneNumberList(String phone_number) {
	    List<Reservation> reservations = new ArrayList<>();
	    LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
	    String sql = "SELECT order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order " +
	                 "FROM `Order` WHERE phone_number = ?";
	    PreparedStatement stmt;
	    try {
	    	stmt=conn.prepareStatement(sql);
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

	    return reservations;
	}
	/**
	 * This method Search order by the order number(pk), and return the value of : order_date, number_of_guests
	 * @param order_number int type
	 * @return Reservation that hold the values that returned from the DB.
	 */
	public Reservation searchOrderByOrderNumber(int order_number) {
		LocalDate orderDate;
		LocalDate DateOfPlacingOrder;
		int numberOfGuests;
		int confirmationCode;
		int subscriberId;
		String phone_number;
		String sql = "SELECT order_date, number_of_guests,confirmation_code, subscriber_id, date_of_placing_order FROM `Order` WHERE order_number = ?;";
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(sql);
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
		return null;
	}
	
}
