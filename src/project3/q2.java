package project3;
import java.sql.*;

public class q2 {

	public static void main(String args[]) throws SQLException{//idk where to put this
		
		// Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
		String tableName = "";
	        int sqlCode=0;      // Variable to hold SQLCODE
	        String sqlState="00000";  // Variable to hold SQLSTATE
	        
		System.out.print("Pick an option:\n" +
					"A. Determine which tables in the restaurant are currently free\n"
					+ "B. Determine cost of each menu item based on the prices of the latest purchase order\n"
					+ "C. Determine most popular items in the past 3 months, and order them by popularity\n"
					+ "D. Recalculate all menu items based on cost, with a markup of 30%\n"
					+ "E. Delete all the reservations on all tables for a given phone number and date\n"
					+ "Q. Quit\n"
					+ "Enter A/B/C/D/E/Q: ");
	
		//database code
		try {
			//load the DB2 JDBC driver
		    DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;
		} catch (Exception cnfe){
		    System.out.println("Class not found");
	    }
		
		
		// This is the url you must use for DB2.
		//Note: This url may not valid now !
		String url = "jdbc:db2://comp421.cs.mcgill.ca:50000/cs421";
		Connection con = DriverManager.getConnection (url,"dbSQ2074", "Fshstix1!") ;
		Statement statement = con.createStatement ( ) ;
		
		
		// USER PICKS C.
		try {
//		    String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
			String querySQL = "SELECT co.mi_id, sum(quantity) AS orderamt" + 
					"FROM customerorder co INNER JOIN menuitem mi ON co.mi_id = mi.mi_id" + 
					"WHERE time_placed > current date - 3 months" + 
					"GROUP BY co.mi_id " + 
					"ORDER BY orderamt DESC;";
		    System.out.println (querySQL) ;//todo: delete this line later
		    java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
		    while ( rs.next ( ) ) {
			int menuid = rs.getInt ( 1 ) ;//first col
			int orderamt = rs.getInt (2);//second col
			System.out.println ("menuid:  " + id);//should we output the item name instead?
			System.out.println ("amount ordered:  " + name);
		    }
		    System.out.println ("DONE");
		} catch (SQLException e)
		    {
			sqlCode = e.getErrorCode(); // Get SQLCODE
			sqlState = e.getSQLState(); // Get SQLSTATE
	                
			// Your code to handle errors comes here;
			// something more meaningful than a print would be good
			System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
		    }
		
		
		// Finally but importantly close the statement and connection
		statement.close ( ) ;
		con.close ( ) ;
	}
}
