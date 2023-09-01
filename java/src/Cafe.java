/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql, authorisedUser); break;
                   case 2: UpdateProfile(esql, authorisedUser); break;
                   case 3: PlaceOrder(esql, authorisedUser); break;
                   case 4: UpdateOrder(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

  public static void Menu(Cafe esql, String authorisedUser)
  {
	try
	{
		int isManager = esql.executeQuery("SELECT U.login FROM Users U WHERE U.login = '" + authorisedUser + "' AND U.type = 'Manager'");
		if(isManager > 0)
		{
			System.out.println("===== MANAGER'S VIEW =====");
			System.out.println("Please choose what you wish to do");
			System.out.println("1. Search by item name");
			System.out.println("2. Search by item type");
			System.out.println("3. Add item to menu");
			System.out.println("4. Delete item from menu");
			System.out.println("5. Update item from menu");
			
			switch(readChoice()){
				case 1:
					System.out.print("Enter the item name: ");
					String itemName_t = in.readLine();
					String query1 = String.format("SELECT * FROM MENU M WHERE M.itemName = '" + itemName_t + "'");
					esql.executeQueryAndPrintResult(query1);
					break;
				case 2:
					System.out.print("Enter the item type: ");
					String itemType_t = in.readLine();
					String query2 = String.format("SELECT * FROM MENU M WHERE M.type = '" + itemType_t + "'");
					esql.executeQueryAndPrintResult(query2);
					break;
				case 3:
					System.out.print("Enter the Item Name: ");
					String itemName = in.readLine();
					System.out.print("Enter the Item Type: ");
					String itemType = in.readLine();
					System.out.print("Enter the Item Price: $");
					String itemPrice = in.readLine();
					System.out.print("Enter the Item Description: ");
					String itemDesc = in.readLine();
					System.out.print("Enter the Image URL: ");
					String imageURL = in.readLine();
					String query3 = String.format("INSERT INTO MENU (itemName, type, price, description, imageURL) VALUES ('%s', '%s', '%s', '%s', '%s')", itemName, itemType, itemPrice, itemDesc, imageURL);
					esql.executeUpdate(query3);
					break;
				case 4: 
					System.out.print("Enter the item name to delete: ");
					String ItemToDelete = in.readLine();
					String query4 = String.format("DELETE FROM MENU WHERE itemName = '" + ItemToDelete + "'");
					esql.executeUpdate(query4);
					break;
				case 5:
					System.out.print("Please enter the item name to update: ");
					String targetItem = in.readLine();
					System.out.print("Enter its updated name: ");
					String new_itemName = in.readLine();
					System.out.print("Enter its updated type: ");
					String new_itemType = in.readLine();
					System.out.print("Enter its updated price: $");
					String new_itemPrice = in.readLine();
					System.out.print("Enter its updated description: ");
					String new_itemDesc = in.readLine();
					System.out.print("Enter its updated image url: ");
					String new_imageURL = in.readLine();
					String query5 = String.format("UPDATE MENU SET itemName = '" + new_itemName + "', type = '" + new_itemType + "', price = '" + new_itemPrice + "', description = '" + new_itemDesc + "', imageURL = '" + new_imageURL + "' WHERE itemName = '" + targetItem + "'");
					esql.executeUpdate(query5);
					break;
					

			}
		}

		else
		{
			System.out.println("Please choose what you wish to do");
			System.out.println("1. Search by item name");
			System.out.println("2. Search by item type");
			
			switch(readChoice()){
				case 1:
					System.out.print("Enter the item name: ");
					String itemName = in.readLine();
					String query1 = String.format("SELECT * FROM MENU M WHERE M.itemName = '" + itemName + "'");
					esql.executeQueryAndPrintResult(query1);
					break;
				case 2:
					System.out.print("Enter the item type: ");
					String itemType = in.readLine();
					String query2 = String.format("SELECT * FROM MENU M WHERE M.type = '" + itemType + "'");
					esql.executeQueryAndPrintResult(query2);
					break;
			}
		}

		return;	
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
		return;
	}
	
  }

  public static void UpdateProfile(Cafe esql, String authorisedUser){
	try{
		int isManager = esql.executeQuery("SELECT U.login FROM Users U WHERE U.login = '" + authorisedUser + "' AND U.type = 'Manager'");
		if(isManager > 0)
		{
			System.out.println("===== MANAGER'S VIEW =====");
			System.out.println("Please choose who to update");
			System.out.println("1. Self");
			System.out.println("2. Other User");
			int choice = readChoice();
			String targetUser = "tmp";
			if(choice == 2)
			{
				System.out.println("Enter the users login");
				targetUser = in.readLine();
			}
			
			System.out.println("Please choose what you wish to update");
			System.out.println("1. Login");
			System.out.println("2. Phone Number");
			System.out.println("3. Password");
			System.out.println("4. Favorite Items");
			System.out.println("5. User Type");
			switch(readChoice()){
				case 1:
					System.out.println("Enter your new login");
					String newLogin = in.readLine();
					String query1;
					if(choice == 2)
						query1 = String.format("UPDATE Users SET login = '" + newLogin + "' WHERE login = '" + targetUser + "'");
					else
						query1 = String.format("UPDATE Users SET login = '" + newLogin + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query1);
					System.out.println("Success!");
					break;
				case 2:
					System.out.println("Enter your new phone number");
					String newPhoneNumber = in.readLine();
					String query2;
					if(choice == 2)
						query2 = String.format("UPDATE Users SET phoneNum = '" + newPhoneNumber + "' WHERE login = '" + targetUser + "'");
					else
						query2 = String.format("UPDATE Users SET phoneNum = '" + newPhoneNumber + "' WHERE login = '" + authorisedUser + "'");					
					esql.executeUpdate(query2);
					System.out.println("Success!");
					break;
				case 3:
					System.out.println("Enter your password");
					String newPassword = in.readLine();
					String query3;
					if(choice == 2)
						query3 = String.format("UPDATE Users SET password = '" + newPassword + "' WHERE login = '" + targetUser + "'");
					else
						query3 = String.format("UPDATE Users SET password = '" + newPassword + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query3);
					System.out.println("Success!");
					break;
				case 4:
					System.out.println("Enter your new favorite items");
					String newFavItems = in.readLine();
					String query4;
					if(choice == 2)
						query4 = String.format("UPDATE Users SET favItems = '" + newFavItems + "' WHERE login = '" + targetUser + "'");
					else
						query4 = String.format("UPDATE Users SET favItems = '" + newFavItems + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query4);
					System.out.println("Success!");
					break;
				case 5:
					System.out.println("Enter your new user type");
					String newType = in.readLine();
					String query5;
					if(choice == 2)
						query5 = String.format("UPDATE Users SET type = '" + newType + "' WHERE login = '" + targetUser + "'");
					else
						query5 = String.format("UPDATE Users SET type = '" + newType + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query5);
					System.out.println("Success!");
					break;
			}

		}
		
		else
		{
			System.out.println("Please choose what you wish to update");
			System.out.println("1. Login");
			System.out.println("2. Phone Number");
			System.out.println("3. Password");
			System.out.println("4. Favorite Items");
			switch(readChoice()){
				case 1:
					System.out.println("Enter your new login");
					String newLogin = in.readLine();
					String query1 = String.format("UPDATE Users SET login = '" + newLogin + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query1);
					System.out.println("Success!");
					break;
				case 2:
					System.out.println("Enter your new phone number");
					String newPhoneNumber = in.readLine();
					String query2 = String.format("UPDATE Users SET phoneNum = '" + newPhoneNumber + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query2);
					System.out.println("Success!");
					break;
				case 3:
					System.out.println("Enter your password");
					String newPassword = in.readLine();
					String query3 = String.format("UPDATE Users SET password = '" + newPassword + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query3);
					System.out.println("Success!");
					break;
				case 4:
					System.out.println("Enter your new favorite items");
					String newFavItems = in.readLine();
					String query4 = String.format("UPDATE Users SET favItems = '" + newFavItems + "' WHERE login = '" + authorisedUser + "'");
					esql.executeUpdate(query4);
					System.out.println("Success!");
					break;
			}
		}	


		return;
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
		return;
	}	

  }
  static int currnum = 90002;
  public static void PlaceOrder(Cafe esql, String authorisedUser)
  {
	try
	{
		boolean isOrdering = true;
		String item;
		double total = 0.0;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String addOrderQuery = String.format("INSERT INTO Orders (orderid,login, paid, timeStampRecieved, total) VALUES('%d','%s', '%s', '%s', '%s')",currnum,authorisedUser, "false", String.format(timeformat.format(timestamp)), "0");
                esql.executeUpdate(addOrderQuery);	
		System.out.println("Please enter the names of the items you want to add: (Enter 0 to complete order)");
		while(isOrdering)
		{
			System.out.print("Enter the item you want to add: ");
			item = in.readLine();

			if(item.equals("0")) { isOrdering = false; break; }

			String getPriceQuery = String.format("SELECT P.price FROM MENU P WHERE itemName = '" + item + "'");
			List<List<String>> temp = esql.executeQueryAndReturnResult(getPriceQuery);
			total += Double.parseDouble(temp.get(0).get(0));

			String addToItemStatusQuery = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES('%d', '%s', '%s', '%s', '%s')",currnum, item, String.format(timeformat.format(timestamp)), String.format("Hasn''t Started"), "test");
			esql.executeUpdate(addToItemStatusQuery);
			String query2000 = String.format("SELECT * FROM ItemStatus WHERE orderid = '"+currnum+"'");
			esql.executeQueryAndPrintResult(query2000);
		}
		String UpdateOrderQuery = String.format("UPDATE Orders SET total = '" +total+ "' WHERE orderid = '" + currnum+"'");
		esql.executeUpdate(UpdateOrderQuery);
		String PrintOrderQuery = String.format("SELECT * FROM Orders WHERE orderid = '" + currnum + "'");
		esql.executeQueryAndPrintResult(PrintOrderQuery);
		currnum++;
	}
	catch(Exception e)
	{
		System.out.println(e.getMessage());
	}
	return;
  }

  public static void UpdateOrder(Cafe esql, String authorisedUser){
     Scanner sc = new Scanner(System.in);
     double new_price = 0.0;
     double price = 0.0;
     double total_price = 0.0;
     String getpaid;
     Timestamp timestamp_2 = new Timestamp(System.currentTimeMillis());
     SimpleDateFormat timeformat_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     try {
      int isManager_Employee = esql.executeQuery("SELECT U.login FROM Users U WHERE U.login = '" + authorisedUser + "' AND (U.type = 'Manager' OR U.type = 'Employee')");
	if (isManager_Employee > 0 ){
	 String unpaid_24hours_query = String.format("SELECT * FROM Orders WHERE timeStampRecieved >= NOW() - '1 day'::INTERVAL AND paid = 'f'");
	 esql.executeQueryAndPrintResult(unpaid_24hours_query);
         System.out.print("Enter OrderId you wish to update: ");
         int id = sc.nextInt();
         getpaid = String.format("SELECT paid FROM Orders WHERE orderid = " + id);
         List<List<String>> getpaid_1 = esql.executeQueryAndReturnResult(getpaid);
         char c = getpaid_1.get(0).get(0).charAt(0);
         if (c == 'f'){
            System.out.print("Press YES to change an ORDER to paid: ");
            String input = in.readLine().toLowerCase();
            if(input.equals("yes")){
               String query1 = String.format("UPDATE Orders SET paid = 't' WHERE orderid = " + id);
               esql.executeUpdate(query1);
            }
            else{
               System.out.println("It has already been paid.");
            }
         }
      }

      else{
	 String query20000 = String.format("SELECT * FROM orders WHERE login = '"+authorisedUser+"'");
	 esql.executeQueryAndPrintResult(query20000);
         System.out.print("Enter your orderID: ");
         int id = sc.nextInt();
         //String query100 = String.format("SELECT * FROM Orders WHERE orderid = " + id);
         //esql.executeQueryAndPrintResult(query100);
         getpaid = String.format("SELECT paid FROM Orders WHERE orderid = " + id);
         List<List<String>> getpaid_1 = esql.executeQueryAndReturnResult(getpaid);
         char c = getpaid_1.get(0).get(0).charAt(0);
         if (c == 'f'){
            System.out.println("1. Add the item.");
            System.out.println("2. Delete the item.");
            switch(readChoice()){
               case 1:
                  System.out.print("Enter new item: ");
                  String new_order_name = in.readLine();

                  String getNewPriceQuery = String.format("SELECT P.price FROM MENU P WHERE itemName = '" + new_order_name + "'");
                  List<List<String>> temp = esql.executeQueryAndReturnResult(getNewPriceQuery);
                  new_price = Double.parseDouble(temp.get(0).get(0));

                  //String getOldPriceQuery = String.format("SELECT P.price FROM MENU P WHERE itemName = '" + old_order_name + "'");
                  //List<List<String>> temp3 = esql.executeQueryAndReturnResult(getOldPriceQuery);
                  //old_price = Double.parseDouble(temp3.get(0).get(0));

                  String getTotalQuery = String.format("SELECT total FROM Orders WHERE orderid = " + id);
                  List<List<String>> temp2 = esql.executeQueryAndReturnResult(getTotalQuery);
                  total_price = Double.parseDouble(temp2.get(0).get(0));
                  total_price += new_price;
                  String query12 = String.format("SELECT * FROM ItemStatus WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query12);
                  String query13 = String.format("SELECT * FROM Orders WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query13);
		  //System.out.println(timeformat_2.format(timestamp_2));
		  String query2 = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES('%d', '%s', '%s', '%s', '%s')", id, new_order_name, String.format(timeformat_2.format(timestamp_2)), "Hasn''t started", "");
                  esql.executeUpdate(query2);
                  String query3 = String.format("UPDATE Orders SET total = " + total_price + " WHERE orderid = " + id);
                  esql.executeUpdate(query3);

                  String query22 = String.format("SELECT * FROM ItemStatus WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query22);
                  String query23 = String.format("SELECT * FROM Orders WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query23);

                  break;
               case 2:
                  System.out.print("Enter item you want to delete: ");
                  String order_name = in.readLine();

                  String getPriceQuery = String.format("SELECT P.price FROM MENU P WHERE itemName = '" + order_name + "'");
                  List<List<String>> temp4 = esql.executeQueryAndReturnResult(getPriceQuery);
                  price = Double.parseDouble(temp4.get(0).get(0));

                  String TotalQuery = String.format("SELECT total FROM Orders WHERE orderid = " + id);
                  List<List<String>> temp3 = esql.executeQueryAndReturnResult(TotalQuery);
                  total_price = Double.parseDouble(temp3.get(0).get(0));
                  total_price -= price;

                  String query14 = String.format("SELECT * FROM ItemStatus WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query14);
                  String query24 = String.format("SELECT * FROM Orders WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query24);

                  String query4 = String.format("DELETE FROM ItemStatus WHERE itemName = '" + order_name + "' AND orderid = " + id);
                  esql.executeUpdate(query4);
                  String query5 = String.format("UPDATE Orders SET total = " + total_price + " WHERE orderid = " + id);
                  esql.executeUpdate(query5);

                  String query15 = String.format("SELECT * FROM ItemStatus WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query15);
                  String query25 = String.format("SELECT * FROM Orders WHERE orderid = " + id);
                  esql.executeQueryAndPrintResult(query25);
                  break;

            }
         }
         else {
            System.out.println("It has already been paid.");
            
         }      
      }
   } 
     catch (Exception e) {
      System.out.println(e.getMessage());
   }
  }

}//end Cafe

