package project3;
import java.math.BigDecimal;
import java.util.Scanner;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class q2 {

	public static void main(String args[]) throws SQLException{//idk where to put this
		
		// Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
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
					System.out.print("Enter inventory item name: ");
			        String item = in.nextLine();
			        
					//get cid input for qty
			        System.out.print("Enter desired quantity: ");
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
					String price = rs.getString (5).substring(0, rs.getString(5).length() - 4);//fourth col
					System.out.println ("the lowest price for " + qty + " units of "+ item + " is $" + price + " from the vendor " + vendor + "\n");
				    }
				    prepStatement1.close();
				    prepStatement2.close();
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
				    System.out.println("\nMenu items ranked by popularity in the last 5 months:");
				    java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
				    int num = 1;
				    while ( rs.next ( ) ) {
				    System.out.println();
				    System.out.println("#" + num++);
					int menuid = rs.getInt ( 1 ) ;//first col
					int orderamt = rs.getInt (2);//second col
					System.out.println ("menu item id:  " + menuid);
//					System.out.println("menu item name: "+ name);//should we output the item name instead?
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
        	
        	
        	//USER PICKS D
        	else if(optionPicked.equalsIgnoreCase("D")) {
        		try {
		//		    String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
					String updateSQL = "UPDATE menuitem mi\n" + 
							"SET price = ( SELECT price FROM  (\n" + 
							"SELECT mi.mi_id as mi_id, mi.name as name, tp.TotalPrice as price, mi.description as description FROM menuitem mi, (\n" + 
							"SELECT menurecipe.mi_id, ROUND(2 * 1.30 * SUM(recipecallsfor.qty * a.price), 0) / 2 as TotalPrice \n" + 
							"FROM menurecipe, recipecallsfor, (\n" + 
							"SELECT poi.item_name, price, date_time \n" + 
							"FROM purchaseorderitem poi INNER JOIN (\n" + 
							"SELECT item_name, order_id, date_time FROM purchaseorder po INNER JOIN (\n" + 
							"SELECT item_name, MAX(date_time) AS newest FROM (\n" + 
							"SELECT poi.rname as rname, poi.item_name as item_name, po.order_id as order_id, poi.price as price, po.date_time as date_time \n" + 
							"FROM purchaseorder po INNER JOIN ( SELECT * FROM purchaseorderitem ) poi \n" + 
							"ON po.order_id = poi.order_id)	\n" + 
							"GROUP BY item_name) pip ON po.date_time = pip.newest) ms \n" + 
							"ON poi.item_name = ms.item_name AND poi.order_id = ms.order_id) as a \n" + 
							"WHERE menurecipe.recipe_id = recipecallsfor.recipe_id AND recipecallsfor.item_name = a.item_name\n" + 
							"GROUP BY menurecipe.mi_id ORDER BY menurecipe.mi_id) tp WHERE mi.mi_id = tp.mi_id) newvals WHERE mi.mi_id = newvals.mi_id)";
		//		   
					statement.executeUpdate(updateSQL);
					
					//find top five most expensive menu items
					String querySQL = "SELECT *FROM menuitem ORDER BY price DESC FETCH FIRST 5 ROWS ONLY";
				    System.out.println("\nFive most expensive menu items/prices after markup:");
				    java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
				    int num = 1;
				    while ( rs.next ( ) ) {
				    System.out.println();
				    System.out.println("#" + num++);
					int menuid = rs.getInt ( 1 ) ;//first col
					String itemName = rs.getString (2);//second col
					String price = rs.getString(4).substring(0, rs.getString(4).length() - 6);
			
					System.out.println ("menu item id:  " + menuid);//should we output the item name instead?
					System.out.println("menu item name: "+ itemName);
					System.out.println ("price:  $" + price);
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
        	}//end of option D else if
        	
        	
        	//USER PICKS E
        	else if(optionPicked.equalsIgnoreCase("E")) {
        		try {
        			PreparedStatement preparedStatement= con.prepareStatement("SELECT *"
        					+ "FROM Reservation "
        					+ "WHERE phone_num = ? AND start_time = ?");
        			
        			Scanner in = new Scanner(System.in);
        			System.out.print("Enter phone number of reservation: ");
        			String phonenum = in.nextLine();
        			System.out.print("Enter date of reservation (YYYY-MM-DD):");
        			String datestr = in.nextLine();
        			System.out.print("Enter time of reservation(HH.MM):");
        			String timestr= in.nextLine();
        			
//        			int year, month, day, hour, minute;
//        			year=Integer.parseInt(datestr.substring(0,4));
//        			month=Integer.parseInt(datestr.substring(5,7));
//        			day=Integer.parseInt(datestr.substring(8));
//        			hour = Integer.parseInt(timestr.substring(0,2));
//        			minute = Integer.parseInt(timestr.substring(4));
        			
        			//parse the starttime input and turn it into a Timestamp
        			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm");
        			java.util.Date date = null;
					try {
						date = dateFormat.parse(datestr + " " + timestr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			long time = date.getTime();
        			Timestamp starttime = new Timestamp(time);
        			
        			preparedStatement.setString(1, phonenum);
        			preparedStatement.setTimestamp(2, starttime);
        			
//        			1 Le Mezz                       20 Sean Roberts                                       5142129998 2020-01-01-07.00.00.000000 2020-01-01-08.47.00.000000
        			
        			//INSERT INTO reservation (customer_id,rname, table_num,name, phone_num, start_time, end_time) 
//        			VALUES ( 168,'Le Mezz',4,'Maria Joe','514525474','2020-02-29-13.30.00.000000', '2020-02-29-14.23.00.000000')
        			
        			//Select * from reservation where phone_num = '514525474' AND start_time='2020-02-29-13.30.00.000000'
        			
        			

				    System.out.println("\nReservation(s) deleted:");
				    java.sql.ResultSet rs = preparedStatement.executeQuery();
				    int num = 1;
				    while ( rs.next ( ) ) {
				    System.out.println();
				    System.out.println("#" + num++);
					int custid = rs.getInt ( 1 ) ;//first col
					String restaurant = rs.getString (2);//second col
					int tablenum = rs.getInt(3);
					String name = rs.getString(4);
			
					System.out.println ("customer id: " + custid);//should we output the item name instead?
					System.out.println("restaurant: "+ restaurant);
					System.out.println ("customer name: " + name);
					System.out.println("phone number: "+ phonenum);
					System.out.println("table number: " + tablenum);
					System.out.println("date and time of reservation: " + datestr +" "+ timestr + "\n");
				    }
				    PreparedStatement update = con.prepareStatement("DELETE FROM reservation "
				    		+ "WHERE phone_num = ? AND start_time = ?");
					
					update.setString(1, phonenum);
        			update.setTimestamp(2, starttime);
				    update.executeUpdate();//delete the rows	
				    
				    update.close();
				    
				} catch (SQLException e)
				    {
					sqlCode = e.getErrorCode(); // Get SQLCODE
					sqlState = e.getSQLState(); // Get SQLSTATE
			                
					// Your code to handle errors comes here;
					// something more meaningful than a print would be good
					System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
				    }
        	}
        	
			//USER PICKS Q
			else if(optionPicked.equalsIgnoreCase("Q")) {
				System.out.println("Session Ended");
				break;
			}
				
			//INVALID INPUT
			else {
				System.out.println("Invalid Input. Please type the letter of one of the options listed below.\n");
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
				+ "C. Rank the most popular items in the past 5 months, and order them by popularity\n"
				+ "D. Recalculate all menu items based on latest purchase costs from vendors, with a markup of 30%,\n   then view the 5 most expensive menu items and their prices\n"
				+ "E. Delete all the reservations on all tables for a given phone number and date\n"
				+ "Q. Quit\n"
				+ "Enter A/B/C/D/E/Q: ");
		Scanner in = new Scanner(System.in);
        return in.nextLine();
	}
}


