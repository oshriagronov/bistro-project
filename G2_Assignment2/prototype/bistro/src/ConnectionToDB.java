import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
public class ConnectionToDB {
	Connection conn;
	public ConnectionToDB() 
	{
        try 
        {
        	// "password" argument is for the db password.
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "password");
            System.out.println("SQL connection succeed");
     	} catch (SQLException ex) 
     	    {/* handle any errors*/
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            }
   	}
	/**
	 * This method using the Reservation object which hold all the data needed to create new order at the DB
	 * @param reservation of type Reservation 
	 * @return number bigger then 1 if succeed or 0 if failed
	 */
	public int insertNewOrder(Reservation reservation) {
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("INSERT INTO `Order` ( order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order) VALUES( ?, ?, ?, ?, ?)");
			stmt.setDate(1, java.sql.Date.valueOf(reservation.getOrderDate()));
			stmt.setInt(2, reservation.getNumberOfGuests());
			stmt.setInt(3, reservation.getConfirmationCode());
			stmt.setInt(4, reservation.getSubscriberId());
			stmt.setDate(5, java.sql.Date.valueOf(reservation.getDateOfPlacingOrder()));
			return stmt.executeUpdate();
		}
		catch(SQLException e){
			e.printStackTrace();
			return 0;
		}
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
			e.printStackTrace();
			return 0;
		}
	}
}