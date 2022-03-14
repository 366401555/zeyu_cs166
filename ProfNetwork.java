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
import java.util.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
 
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;
//static String current = null;
   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

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

   }//end ProfNetwork

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
       if(rs.next()){
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
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            int currentlevel=0;
        //    int ifnew=0;
            int offset=0;
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
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Your Connection Request");
                System.out.println("5. Change password");
                	System.out.println("6. Search people");
                   System.out.println("7. Connection Request");
                   System.out.println("8. View Messages");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql,authorisedUser); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: NewMessage(esql,authorisedUser); break;
                   case 4: RequestList(esql,authorisedUser); break;
                  case 5: ChangePassword(esql,authorisedUser); break;
                   case 6: Search(esql); break;
                   case 7:Connection_Request(esql,authorisedUser);break;
                   case 8:ViewMessages(esql,authorisedUser);break;
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
public static void UpdateProfile(ProfNetwork esql){
   
}
    public static void ViewMessages(ProfNetwork esql,String currentuser){
          try{
         boolean keepon = true;
         while(keepon)
         {

          System.out.println("1. View message");
            System.out.println("2. delete massage");
           
            System.out.println("3. < EXIT");
              switch(readChoice())
              { case 1:
                
               //   System.out.println("ENTER receiverid:");
               //    String rec=in.readLine();
                  // System.out.println(rec);
                   System.out.println("You sended:");
                  String query2=String.format("SELECT * FROM MESSAGE WHERE MESSAGE.senderId='%s'  AND  (MESSAGE.deleteStatus=0 OR MESSAGE.deleteStatus=2)",currentuser);
                
                           esql.executeQueryAndPrintResult(query2);
                            System.out.println("You received:");
    String query3=String.format("SELECT * FROM MESSAGE WHERE  MESSAGE.receiverId='%s' AND  (MESSAGE.deleteStatus=0 OR MESSAGE.deleteStatus=1)",currentuser);       
                      esql.executeQueryAndPrintResult(query3);
                      break;
////////////////////////////////////////////////////////////////////////////
               case 2:
                  System.out.println("You sended:");
                  String queryq=String.format("SELECT * FROM MESSAGE WHERE MESSAGE.senderId='%s'  AND  (MESSAGE.deleteStatus=0 OR MESSAGE.deleteStatus=2)",currentuser);
                
                           esql.executeQueryAndPrintResult(queryq);
                            System.out.println("You received:");
    String query4=String.format("SELECT * FROM MESSAGE WHERE  MESSAGE.receiverId='%s' AND  (MESSAGE.deleteStatus=0 OR MESSAGE.deleteStatus=1)",currentuser);       
                      esql.executeQueryAndPrintResult(query4);
Scanner sc= new Scanner(System.in);
                      System.out.println("ENTER MGID AND DELETESTATUS TO DELETE MESSAGE(confirm ID)");
                      int a=sc.nextInt();
                      int b=sc.nextInt();
                      System.out.println("is your send?");
                      String check=in.readLine();

                      if(check.equals("yes")&& b==0)
                      {
                           String query5=String.format("UPDATE MESSAGE SET deleteStatus =1 WHERE msgId='%s'",a);
                               esql.executeUpdate(query5);
                      }
                      else if(check.equals("yes")&& b==2)
                      {
                           String query6=String.format("UPDATE MESSAGE SET deleteStatus =3 WHERE msgId='%s'",a);
                               esql.executeUpdate(query6);
                      }
                       else if(check.equals("no")&& b==0)
                      {
                           String query7=String.format("UPDATE MESSAGE SET deleteStatus =2 WHERE msgId='%s'",a);
                               esql.executeUpdate(query7);
                      }
                       else if(check.equals("no")&& b==1)
                      {
                           String query8=String.format("UPDATE MESSAGE SET deleteStatus =3 WHERE msgId='%s'",a);
                               esql.executeUpdate(query8);
                      }
                      else
                      {
                         System.out.println("Wrong input");
                      }
                     break;







                        case 3:
                        keepon=false;
                        break;

              }

             



         }
         






      }catch(Exception e){
         System.err.println (e.getMessage());
      }

        


        
    }
  
    public static void NewMessage(ProfNetwork esql,String currentuser){

 try{
      // System.out.println("Enter message you want to write:");
      // String meg=in.readLine();
       System.out.println("Enter userid you want to send:");
       String rec = in.readLine();
        System.out.println("Enter contents you want to send:");
        String content=in.readLine();
        int delatestatus=0;
        String a="sent";
        
        //DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Timestamp time = new Timestamp(System.currentTimeMillis());//https://mkyong.com/java/how-to-get-current-timestamps-in-java/
        String query=String.format("INSERT INTO MESSAGE (senderId,receiverId,contents,sendTime,deleteStatus,status)  VALUES ('%s','%s','%s','%s','%s','%s')",currentuser,rec,content,time,delatestatus,a);
   esql.executeUpdate(query);

    System.out.println("send !!");
    String query2=String.format("SELECT * FROM MESSAGE WHERE MESSAGE.senderId='%s' AND MESSAGE.receiverId='%s' AND MESSAGE.contents='%s'",currentuser,rec,content);
      System.out.println("detail:");
    esql.executeQueryAndPrintResult(query2);

   
       //System.out.println("Receiver not exist");
    





      }catch(Exception e){
         System.err.println (e.getMessage());
      }








        
    }
  public static void ChangePassword(ProfNetwork esql,String currentuser){
try{
         String a=currentuser;
         System.out.print("\tEnter your new password: ");
            String newpassword = in.readLine();
         
         
         String query = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", newpassword, a);
     

        esql.executeQuery(query);
//System.out.println("success!!!!");
         
      }catch(Exception e){
         System.err.println ("success!!!!");
      }



        
    }
   public static void Search(ProfNetwork esql){
        try{
 System.out.print("\tSearch people by name: ");
 String input=in.readLine();
 String query = String.format("SELECT USR.userId, USR.email, USR.name, USR.dateOfBirth FROM USR  WHERE USR.name = '%s' ", input);
int rowCount = esql.executeQueryAndPrintResult(query);
if(rowCount==0)
{
   System.out.println ("No found!");
}
         System.out.println ("total row(s): " + rowCount);



         
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
        
    }






    public static void FriendList(ProfNetwork esql,String currentuser)
{
 try{
 boolean keepon = true;
 String current=currentuser;
          while(keepon)
          {
             System.out.println("View Friends");
            System.out.println("---------");
            System.out.println("1. list current level's friend(by name)");
            System.out.println("2. view friend's profile");
              System.out.println("3.sent request");
            System.out.println("4. < EXIT");
             System.out.println("---------");
 switch(readChoice())
 {
      case 1:

      String query=String.format("SELECT B.name FROM CONNECTION_USR A,USR B WHERE A.userId='%s' AND A.connectionId=B.userId AND A.status='Accept' ",currentuser);
      String query7=String.format("SELECT B.name FROM CONNECTION_USR A,USR B WHERE A.connectionId='%s' AND A.userId=B.userId AND A.status='Accept' ",currentuser);
      int row  =   esql.executeQueryAndPrintResult(query);
       System.out.println("and accpeted friend(accept friend request)(test for insertion):");
      int row6 = esql.executeQueryAndPrintResult(query7);
     // esql.executeQueryAndReturnResult(query);
      if(row==0 && row6==0)
      {
         System.out.println("you don't have any friend");
      }
     break;

     case 2:
     System.out.println("Select the friend by name");
     String input2=in.readLine();
     String query2=String.format("SELECT B.userId FROM USR B WHERE B.name='%s'",input2);

     List<List<String>> checker= esql.executeQueryAndReturnResult(query2);
     
                for (List<String> innerlist2 : checker) {
           
            for (String m : innerlist2) {

                   currentuser=m;
            }
                }
       String query3=String.format("SELECT instituitionName,major,degree,startdate,enddate FROM EDUCATIONAL_DETAILS  WHERE EDUCATIONAL_DETAILS.userId='%s'",currentuser);
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("EDUCATIONAL_DETAILS:");
   int row2=  esql.executeQueryAndPrintResult(query3);
             if(row2==0)
             {
                System.out.println(currentuser+"don't have EDUCATIONAL_DETAILS info");
             } 
              System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
              System.out.println("WORK_EXPR:");
             String query4=String.format("SELECT company,role,location,startDate,endDate FROM WORK_EXPR WHERE WORK_EXPR.userId='%s'",currentuser);
              int row3=  esql.executeQueryAndPrintResult(query4);
             if(row3==0)
             {
                System.out.println(currentuser+"don't have WORK_EXPR info");
             } 
              System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
              System.out.println("You are currently viewing " + input2 + "'s profile");
              
              break;
   case 3:
           System.out.println("you request userid is "+currentuser);
          Connection_Request(esql,current);
          break;

     case 4:
     keepon = false;
     break;


 }







          }

      }catch(Exception e){
         System.err.println (e.getMessage());
      }


}




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
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
          System.out.print("\tEnter user name: ");
            String name = in.readLine();
            System.out.print("\tEnter user date of birth (MM/DD/YYYY): ");
            String day = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email, name, dateOfBirth) VALUES ('%s','%s','%s','%s','%s')", login, password, email,name,day);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
        // return 1;
      }catch(Exception e){
         System.err.println (e.getMessage ());
      //   return 0;
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
      System.out.println("\tinvalid input!!! ");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

public static void Connection_Request(ProfNetwork esql,String currentuser)
{
//   try{
//         System.out.println("\tenter request userID: ");
// String request=in.readLine();
// String leveluser;
// int newlevel=5;
// int oldlevel=3;
// int note=0;
// int counter=0;
// boolean newuser=false;
// //A
//          String query = String.format("SELECT CONNECTION_USR.connectionId  FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Accept'",currentuser);
//          int rowCount = esql.executeQuery(query);
//           List<List<String>> checker=esql.executeQueryAndReturnResult(query);//b
// if(rowCount==0)
// {
// newuser=true;
// System.out.println("\tnew user ");
// } 
// while(counter<2 || note==0 || newuser==false )
// {
//  if(counter==0)
//  {
// note=level1checker(esql,currentuser,request,checker);
//  }

 

// counter=counter+1;
// }




//       }catch(Exception e){
//          System.err.println (e.getMessage());
//       }
  try{
    
     

     
String leveluser;
int newlevel=5;
int oldlevel=3;
int note=0;
int special=0;
String queryend=null;
boolean newuser=false;
//A

         String query = String.format("SELECT CONNECTION_USR.connectionId  FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Accept'",currentuser);
         int rowCount = esql.executeQuery(query);
          List<List<String>> checker=esql.executeQueryAndReturnResult(query);
if(rowCount==0)
{
newuser=true;
System.out.println("\tNew user !");
}

if(newuser==false)
{//System.out.println("\t1"); 
System.out.println("\tenter request userID: ");
String request=in.readLine();     
     for (List<String> innerlist : checker) {//1    //https://javahungry.blogspot.com/2020/01/list-of-lists-in-java.html reference
            for (String i : innerlist) {//2
               // System.out.print(aList.get(i).get(j) + " ");
              if(request.equals(i)) 
             {//System.out.println("\t33333333");
            // System.out.println(i);
             if(note==0)
             {
                 String add1 =String.format("INSERT INTO CONNECTION_USR (userId,connectionId,status) VALUES('%s','%s','Request')",currentuser,request);
               //  System.out.println("\t '%s'");
               System.out.println("Already in the list, don't need to request again!");
               
                special=1;
                 break;
             }
              }
              else
             {//else
         
            //  System.out.println("\t2");
               String levelB=i;
              //    System.out.println("\t2");
                String query2 = String.format("SELECT CONNECTION_USR.connectionId  FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Accept' UNION SELECT CONNECTION_USR.userId  FROM CONNECTION_USR WHERE CONNECTION_USR.connectionId='%s' AND CONNECTION_USR.status='Accept'",levelB,levelB);
              //  System.out.println(i);
               //   System.out.println("\t2");
                List<List<String>> checker2=esql.executeQueryAndReturnResult(query2);
                 int rowCoun2 = esql.executeQuery(query2);
              //    System.out.println(rowCoun2);
             //     System.out.println("\t2");
              if(rowCoun2>0)
             {//row2
                
                for (List<String> innerlist2 : checker2) {//3
            //    System.out.println("\tloop 1");
            for (String m : innerlist2) {//4
          //  System.out.println("\tloop 2");
                  if(request.equals(m)) 
              {//if
           //   System.out.println("\t444444");
           //      System.out.println(m);
             if(note==0)
             {
                 String add1 =String.format("INSERT INTO CONNECTION_USR (userId,connectionId,status) VALUES('%s','%s','Request')",currentuser,request);
               //  System.out.println("\tadded");
               // queryend=String.format("INSERT INTO CONNECTION_USR (userId,connectionId, status) VALUES ('%s','%s','%s')", currentuser, request);
                esql.executeUpdate(add1);
                note=1;
                 break;
             }
              }//if
              else
                 {//else
                 //    System.out.println("\t5");
                String levelC=m;
            //      System.out.println("\t5");
                       String query3 = String.format("SELECT CONNECTION_USR.connectionId  FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Accept' UNION SELECT CONNECTION_USR.userId  FROM CONNECTION_USR WHERE CONNECTION_USR.connectionId='%s' AND CONNECTION_USR.status='Accept'",levelC,levelC);
                 //      System.out.println(levelB);
                //       System.out.println("\t5");
                List<List<String>> checker3=esql.executeQueryAndReturnResult(query3);
                 int rowCoun3 = esql.executeQuery(query2);
             //     System.out.println(rowCoun3);
             //     System.out.println("\t5");
                  if(rowCoun3>0)
                  {//row 3
                     for (List<String> innerlist3 : checker3) {//6
              //         System.out.println("\tloop 1");
                      for (String V : innerlist3) {//7
                if(request.equals(V)) 
                {//if
              //   System.out.println("\tloop 888");
                      System.out.println(V);
             if(note==0)
             {
                 String add2 =String.format("INSERT INTO CONNECTION_USR (userId,connectionId,status) VALUES('%s','%s','Request')",currentuser,request);
               //  System.out.println("\tadded");
               // queryend=String.format("INSERT INTO CONNECTION_USR (userId,connectionId, status) VALUES ('%s','%s','%s')", currentuser, request);
                esql.executeUpdate(add2);
                note=1;
                 break;
             }
                }//if
               

                      
            }//7
                     }//6
                  }//row3
                  else
             {
               // System.out.println("\tfault to add,no chain exit!");
                break;
             }
                  // System.out.println("\tfault to add,>level 2 or no chain");
                 }//else

            }//4
                }//3
             }//row2
             else
             {
               // System.out.println("\tfault to add,no chain exit!");
                break;
             }
 }//else

            

            }//2
     }//1    
}//if
if(special==1)
{

}
else if(note==1)
{
    System.out.println("\tadded!!!!!!");
   

}
else if(note==0 || newuser==false)
{
   System.out.println("\tfault to add,level>3 or no relation");
}

if(newuser==true )
{ 
newuser(esql,currentuser,newlevel);
}






//         // System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }





}



   public static void newuser(ProfNetwork esql,String currentuser,int new1){

 try{
    int newlevel=5;
int counter=0;
System.out.println("\tWellcome! you now can add up to 5 friends request!!");
System.out.println("\tPlease enter userID you want to request:");

String temp1=in.readLine();
counter++;
while(newlevel>0){
String query5=String.format("SELECT * FROM USR WHERE USR.userId='%s' ",temp1);
//temp1="null";
int rowcounter=esql.executeQueryAndPrintResult(query5);
System.out.println(rowcounter);
if(rowcounter ==1)
{//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
String query = String.format("INSERT INTO CONNECTION_USR (userId,connectionId,status) VALUES('%s','%s','Request')",currentuser,temp1);
esql.executeUpdate(query);
 System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");

      
         System.out.println("checker");
        //if(checker>0)
System.out.println("\tSuccess! and you can add "+ newlevel+ " more friend request Do you want to contine? yes(1)/no(2):");
System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
String temp2=in.readLine();
if(temp2.equals("yes"))
{
newlevel--;

System.out.println("\tPlease enter userID you want to request:");
 temp1=in.readLine();
 counter++;
}
else{
//newlevel=0;
//int temp4=5-newlevel;
newlevel=0;
System.out.println("\tYou created "+ counter +" request!");

}
}
else{
   System.out.println("\tError!");
}
}


      }catch(Exception e){
          System.err.println (e.getMessage());
      }
  

   }


   public static void RequestList(ProfNetwork esql,String currentuser){

      try{
          boolean keepon = true;
          while(keepon)
          {
             System.out.println("Your request List");
            System.out.println("---------");
            System.out.println("1. Check List");
            System.out.println("2. Accept or Reject");
            System.out.println("3. < EXIT");
             System.out.println("---------");
            switch(readChoice())
            {
                case 1:
                String query= String.format("SELECT * FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Request'",currentuser);
                int count=esql.executeQueryAndPrintResult(query);
                  if(count==0)
                {
                   System.out.println("No request right now!");
                }
                break;
                // cehcklist(esql,currentuser);break;
               case 2: 
                System.out.println("Currently you have following friend request(s):");
                String query1= String.format("SELECT * FROM CONNECTION_USR WHERE CONNECTION_USR.userId='%s' AND CONNECTION_USR.status='Request'",currentuser);
                int count1=esql.executeQueryAndPrintResult(query1);
                if(count1==0)
                {
                   System.out.println("No request right now!");
                }
                else{
                System.out.println("Enter request ID to accept or reject connection");
                String input=in.readLine();
               System.out.println("Accept or Reject?");
                String result=in.readLine();
                if(result.equals("Accept"))
                {
                String query2=String.format("UPDATE CONNECTION_USR SET status='%s' WHERE userId='%s' AND connectionId='%s'",result,currentuser,input);

                 esql.executeUpdate(query2);
                  System.out.println("Updated");
                }
                else if(result.equals("Reject"))
                {
                     String query2=String.format("UPDATE CONNECTION_USR SET status='%s' WHERE userId='%s' AND connectionId='%s'",result,currentuser,input);
                      System.out.println("Updated");
                }
                else
                {
                   System.out.println("invalid input!");
                }

                }
                break;
                case 3:
                keepon=false;
                break;

            }


          }





        
      }catch(Exception e){
         System.err.println (e.getMessage());
      }

        
    }



//   try{
//       String query = String.format("SELECT CONNECTION_USR.connectionId FROM USR,CONNECTION_USR WHERE USR.userId='%s' AND CONNECTION_USR.status='Accept' AND USR.userId=CONNECTION_USR.userId ",currentuser);

//        List<List<String>> checker = esql.executeQueryAndReturnResult(query);







//       }catch(Exception e){
//          System.err.println (e.getMessage());
     // }



    

// public static int level1checker(ProfNetwork esql,String currentuser,int finder, List<List<String>> list1)
// {
// //int temp=finder;

// for (int i = 0; i < list1.size(); i++) {//1
//             for (int j = 0; j < list1.get(i).size(); j++) {//2
//                // System.out.print(aList.get(i).get(j) + " ");
//               if(request.equals(list1.get(i).get(j))) 
//               {System.out.println("\t3");
//                  String add1 =String.format("INSERT INTO CONNECTION_USR (userId,connectionId,status) VALUES('%s','%s','Request')",currentuser,finder);
//                //  System.out.println("\tadded");
//               //  temp=1;
//                  return 1;
//               }
//             }
// }
// return 0;


//}


}//end ProfNetwork

