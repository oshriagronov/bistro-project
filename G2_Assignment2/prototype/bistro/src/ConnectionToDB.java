import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class ConnectionToDB {
	Connection conn;
	public ConnectionToDB() 
	{
        try 
        {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "Oshri@Agronov");
            //Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.3.68/test","root","Root");
            System.out.println("SQL connection succeed");
            //createTableCourses(conn);
     	} catch (SQLException ex) 
     	    {/* handle any errors*/
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            }
   	}
	
	public void saveUser(ArrayList<String> usr) {
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("INSERT INTO users ( id, username, department, tel) VALUES(? , ?, ?, ?)");
			stmt.setInt(1, Integer.parseInt(usr.get(1)));
			stmt.setString(2, usr.get(0));
			stmt.setString(3, usr.get(2));
			stmt.setString(4, usr.get(3));
			stmt.executeUpdate();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
}