package project3;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class q2 {

	public static void main(String args[]) throws SQLException{//idk where to put this
		
		
		// Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
		String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE
	        
		//database code
		try {
			//load the DB2 JDBC driver
		    DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ;
//		    Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (Exception cnfe){
		    System.out.println("Class not found");
	    }
		// This is the url you must use for DB2.
		//Note: This url may not valid now !
		String url = "jdbc:db2://comp421.cs.mcgill.ca:50000/cs421";
		Connection con = DriverManager.getConnection (url,"cs421g74", "Fshstix1!") ;
		Statement statement = con.createStatement ( ) ;
		
		String optionPicked = "";
        while(!optionPicked.equals("Q")) {
        	optionPicked = promptUser();
        	
        	//USER PICKS A.
        	if(optionPicked.equalsIgnoreCase("A")) {
        		try {
        		String querySQL = "WITH t(entrytime) as (VALUES(CURRENT TIMESTAMP))\n" + 
        				"(SELECT table_num FROM reservation, t WHERE\n" + 
        				"( entrytime + 2 Hour <= start_time and end_time IS NULL) or\n" + 
        				"( entrytime >= end_time)\n" + 
        				"UNION\n" + 
        				"SELECT table_num FROM table WHERE table_num NOT IN (SELECT table_num FROM reservation, t WHERE DATE(entrytime) = DATE(start_time)))\n" + 
        				"INTERSECT\n" + 
        				"(SELECT table_num FROM walkin, t WHERE\n" + 
        				"( entrytime + 2 Hour <= start_time and end_time IS NULL) or\n" + 
        				"( entrytime >= end_time)\n" + 
        				"UNION\n" + 
        				"SELECT table_num FROM table WHERE table_num NOT IN (SELECT table_num FROM walkin, t WHERE DATE(entrytime) = DATE(start_time)))";
        		 	System.out.println("\nCurrently available tables' numbers:");
				    java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
				    while ( rs.next ( ) ) {
				    System.out.println();
					int tablenum = rs.getInt ( 1 ) ;//first col
					System.out.println (tablenum);//should we output the item name instead?
				    }
				    System.out.println ("DONE");
        		}
        		catch(SQLException e) {
        			sqlCode = e.getErrorCode(); // Get SQLCODE
					sqlState = e.getSQLState(); // Get SQLSTATE
			                
					// Your code to handle errors comes here;
					// something more meaningful than a print would be good
					System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
        		}
        	}
        	
        	//USER PICKS B.
        	else if(optionPicked.equalsIgnoreCase("B")) {
        		try {
        			//querySQL1 lists the price for each vendor
					PreparedStatement prepStatement1 = con.prepareStatement("WITH t(dhotel, ditem, dqty) AS (VALUES('Le Mezz', ?, ?))\n" + 
							"SELECT vendor_name, (dqty * pricing_funct) as cost \n" + 
							"FROM InventoryVendor, t \n" + 
							"WHERE item_name = ditem AND minqty <= dqty AND dqty < maxqty AND rname = dhotel");
					//querySQL2 selects the cheapest vendor
					PreparedStatement prepStatement2 = con.prepareStatement("WITH t(dhotel, ditem, dqty) AS (VALUES('Le Mezz', ?, ?))\n" + 
							"SELECT dhotel as rname, ditem as item_name, dqty as qty, vendor_name, cost FROM t, (\n" + 
							"SELECT vendor_name, MIN(dqty * pricing_funct) as cost\n" + 
							"FROM InventoryVendor, t \n" + 
							"WHERE item_name = ditem AND minqty <= dqty AND dqty < maxqty AND rname = dhotel\n" + 
							"GROUP BY vendor_name\n" + 
							"ORDER BY cost ASC\n" + 
							"FETCH FIRST 1 ROWS ONLY)");
					
					//get cid input for item
					Scanner in = new Scanner(System.in);
					System.out.println("Enter inventory item name: ");
			        String item = in.nextLine();
			        
					//get cid input for qty
			        System.out.println("Enter desired quantity:");
			        int qty = in.nextInt();
					
			        //provide values for input parameters
			        prepStatement1.setString(1, item);
			        prepStatement2.setString(1, item);
			        prepStatement1.setInt(2, qty);
			        prepStatement2.setInt(2, qty);
			        
			        //execute the statements
			        prepStatement1.executeQuery();
			        ResultSet rs = prepStatement2.executeQuery();
			        
			        
//				    System.out.println (querySQL) ;//todo: delete this line later
				    while ( rs.next ( ) ) {
				    System.out.println();
					String vendor = rs.getString (4) ;//third col
					String price = rs.getString (5);//fourth col
					System.out.println ("the lowest price for " + qty + " units of "+ item + " is $" + price + " from the vendor " + vendor);
				    }
				    System.out.println ("DONE\n");
				} catch (SQLException e)
				    {
					sqlCode = e.getErrorCode(); // Get SQLCODE
					sqlState = e.getSQLState(); // Get SQLSTATE
			                
					// Your code to handle errors comes here;
					// something more meaningful than a print would be good
					System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
				    }
        	}//end of elseif option B
        	
        	// USER PICKS C.
        	else if(optionPicked.equalsIgnoreCase("C")){
				
				try {
		//		    String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
					String querySQL = "SELECT co.mi_id, sum(quantity) AS orderamt\n" + 
							"FROM customerorder co INNER JOIN menuitem mi ON co.mi_id = mi.mi_id\n" + 
							"WHERE time_placed > current date - 5 months\n" + 
							"GROUP BY co.mi_id\n" + 
							"ORDER BY orderamt DESC";
		//		    System.out.println (querySQL) ;//todo: delete this line later
				    System.out.println("\nDishes ranked by popularity in the last 5 months:");
				    java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
				    int num = 1;
				    while ( rs.next ( ) ) {
				    System.out.println();
				    System.out.println("#" + num++);
					int menuid = rs.getInt ( 1 ) ;//first col
					int orderamt = rs.getInt (2);//second col
					System.out.println ("menuid:  " + menuid);//should we output the item name instead?
					System.out.println ("amount ordered:  " + orderamt);
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
        	}//end of if - user picks C
        	
        	
			//USER PICKS Q
			else if(optionPicked.equalsIgnoreCase("Q")) {
				break;
			}
				
			//INVALID INPUT
			else {
				System.out.println("Invalid Input. Please type the letter of one of the options listed below.");
			}
        }
		
		// Finally but importantly close the statement and connection
		statement.close ( ) ;
		con.close ( ) ;
	}
	
	public static String promptUser() {
		System.out.print("Pick an option:\n" +
				"A. Determine which tables in the restaurant are currently free\n"
				+ "B. Find lowest price of a specific inventory item for a desired quantity\n"
				+ "C. Determine most popular items in the past 5 months, and order them by popularity\n"
				+ "D. Recalculate all menu items based on cost, with a markup of 30%\n"
				+ "E. Delete all the reservations on all tables for a given phone number and date\n"
				+ "Q. Quit\n"
				+ "Enter A/B/C/D/E/Q: ");
		Scanner in = new Scanner(System.in);
        return in.nextLine();
	}
}


