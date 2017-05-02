/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for all communication between Jersey
 * and the database.
 */

package RiskApplication.RisksServer;

//IO Imports
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//SQL Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
//Other Imports
import java.util.Properties;
import javax.ws.rs.core.Response;

// Class for all database communication
public class DBConnection {
	
	
	//Establish connection to database. 
	public static Connection getConnection() throws IOException, SQLException
	{
		// Load properties from properties file
		FileInputStream input = new FileInputStream("servFiles/jdbc.properties");
		//FileInputStream input = new FileInputStream("src/jdbc.properties");
		Properties props = new Properties();
		props.load(input);
		
		// Define JDBC driver
		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null)
		  System.setProperty("jdbc.drivers", drivers);
		
		// Other parameters used to create connection
		String url = props.getProperty("jdbc.url");
		String user = props.getProperty("jdbc.user");
		String password = props.getProperty("jdbc.password");
		Connection connection = DriverManager.getConnection(url, user, password);
	    return connection;   
	}
	  

	// Creates tables to hold the data and a trigger if they do not already exist. 
	public static void createTable() throws SQLException, IOException, ClassNotFoundException
	{
		Connection database = DBConnection.getConnection();
		Statement statement = database.createStatement();
		
		// Drop existing tables, if present
		// WARNING - deletes all riskEvent and project table data if uncommented!!!
		//statement.executeUpdate("DROP TABLE IF EXISTS riskEvent, project");
		
		
		// Create a new project table
		String freshProjectDB = new String(Files.readAllBytes(Paths.get("servFiles/freshProjectDB.txt"))); 
		statement.executeUpdate(freshProjectDB);
		//Try create a trigger to move risks to archive table when parent project deleted.
		String freshProjDelTrig = new String(Files.readAllBytes(Paths.get("servFiles/freshProjDelTrigger.txt")));
		try{
			statement.executeUpdate(freshProjDelTrig);
		}
		catch(Exception e){
			//Do nothing - Trigger already exists.
		}
		//Create new riskEvent table
		String freshRiskDB = new String(Files.readAllBytes(Paths.get("servFiles/freshRiskDB.txt"))); 
		statement.executeUpdate(freshRiskDB);
		//Create new user table
		String freshUserDB = new String(Files.readAllBytes(Paths.get("servFiles/freshUserDB.txt"))); 
		statement.executeUpdate(freshUserDB);
		//Try create a trigger to automatically increment risk number unique to project.
		String freshRiskTrig = new String(Files.readAllBytes(Paths.get("servFiles/freshRiskTrigger.txt")));
		try{
			statement.executeUpdate(freshRiskTrig);
		}
		catch(Exception e){
			//Do nothing - Trigger already exists.
		}
		//Try create a table for archived risks
		String freshArchiveDB = new String(Files.readAllBytes(Paths.get("servFiles/freshArchiveDB.txt")));
		try{
			statement.executeUpdate(freshArchiveDB);
		}
		catch(Exception e){
			//Do nothing - table already exists.
		}
		//Try create a trigger to move risks to archive table when deleted.
		String freshRiskDelTrig = new String(Files.readAllBytes(Paths.get("servFiles/freshRiskDelTrigger.txt")));
		try{
			statement.executeUpdate(freshRiskDelTrig);
		}
		catch(Exception e){
			//Do nothing - Trigger already exists.
		}
		
		
		statement.close();
		database.close();
	}
		

	// Add a new project to project table
	public static Response addProject(String pName, String pmName, List<String> users)
	{
		Connection database=null;
		try {
			DBConnection.createTable();
			database = getConnection();
			
			// Multiple insert transaction so auto commit turned off so both inserts can be turned off together.
			database.setAutoCommit(false);
			
		    // Statement used to add new project to project table
			PreparedStatement statement =
					database.prepareStatement("INSERT INTO project(pName, pmName) VALUES(?,?)");
			statement.setString(1, pName);
			statement.setString(2, pmName);
			statement.executeUpdate();
	        statement.close();
		    
	        //HTTP Response body should contain ID assigned to new project in database.
		    Statement statement2 = database.createStatement();
		    ResultSet project = statement2.executeQuery("SELECT pRecID FROM project WHERE pName='"+pName+"';");
	        project.first();
	        Integer pRecID= project.getInt("pRecID");
	        statement2.close();
	        
	        //Add Projects users to users DB
	        for (String user:users){
	        	PreparedStatement uStatement = database.prepareStatement("INSERT INTO user(user, fProject) VALUES(?,?)");
	        	uStatement.setString(1, user);
	        	uStatement.setString(2, pName);
	        	uStatement.executeUpdate();
	        	uStatement.close();
	        }
	        
	        // All database transaction successful so commit changes.
	        database.commit();	   
	        database.close();
	        
	        // Send new client users invitation emails.
	        InviteEmail email = new InviteEmail();
	        for (String user:users){
				email.sendEmail(pName, pRecID, user);
	        }
	        
	       
	        // Send message to slack
	        Slack message = new Slack();
	        message.addProject(pName, pmName);
	        
	        
		    return Response.ok().entity("{\"id\":"+pRecID+"}").build();
		}
		catch (Exception e){
			// At least one database change failed so roll back all changes
			try {
				database.rollback();
				return Response.status(500).build();
			} catch (Exception e2) {
				e2.printStackTrace();
				return Response.status(500).build();
			}
		}	
	}
	
	
	// Update a risk in riskEvent table
	public static Response updateProject(Integer pRecID, String pName, String pmName, List<String> users)
	{
		Connection database=null;
		try {
			DBConnection.createTable();
			database = DBConnection.getConnection();
			
			// Multiple insert transaction so auto commit turned off so both inserts can be turned off together.
			database.setAutoCommit(false);
			
			// Statement used to update existing project in project table
			PreparedStatement statement =
			database.prepareStatement("UPDATE project SET pName=?, pmName=? WHERE pRecID=" +pRecID);
			statement.setString(1, pName);
			statement.setString(2, pmName);
			statement.executeUpdate();
		    statement.close();
		    
		    //Delete all removed users from users table.
		    Statement ouStatement = database.createStatement();
		    ResultSet oldUsersList = ouStatement.executeQuery("Select user FROM user WHERE fProject='"+pName+"';");
		    while (oldUsersList.next()){
		    	if (!users.contains(oldUsersList.getString("user"))){
		    		Statement dStatement = database.createStatement();
		 			dStatement.executeUpdate("DELETE FROM user WHERE fProject='"+pName+"' AND user='"+oldUsersList.getString("user")+"';");
		 			dStatement.close();
		    	}
		    }
		    
		    // Array to store newly added users in need of email invitation.
		    ArrayList<String> newUsers = new ArrayList<String>(); 
		    
		    //Add Projects users to users DB
	        for (String user:users){
	        	//Test if user already exists for project
	 			Statement cStatement = database.createStatement();
	 			ResultSet foundUser=cStatement.executeQuery("SELECT user FROM user WHERE "
	 						+ "fProject='"+pName+"' AND user='"+user+"';");
	 			//Add new users to user table.
	 			if (!foundUser.next()){
	 				PreparedStatement uStatement = database.prepareStatement("INSERT INTO user(user, fProject) VALUES(?,?)");
		        	uStatement.setString(1, user);
		        	uStatement.setString(2, pName);
		        	uStatement.executeUpdate();
		        	uStatement.close();
		        	newUsers.add(user);
	 			}
	 			cStatement.close();
	        }
	        
	        // All database transaction successful so commit changes.
	        database.commit();	
		    database.close();
		    
		    // Send new client users invitation emails.
	        InviteEmail email = new InviteEmail();
	        for (String user:newUsers){
				email.sendEmail(pName, pRecID, user);
	        }
		    
		    // Send message to slack
	        Slack message = new Slack();
	        message.updatedProject(pName, pmName);
	        
		    return Response.ok().entity("{\"id\":"+pRecID+"}").build();
		}
		catch (Exception e) {
			// At least one database change failed so roll back all changes
			try {
				database.rollback();
				return Response.status(500).build();
			} 
			catch (Exception e2) {
				e2.printStackTrace();
				return Response.status(500).build();
			}
		}	
	}
		
	// Delete a project from project table.
	public static Response deleteProject(Integer pRecID, Slack lastUpdate)
	{
		try {
			String pName= projectName(pRecID);// Get project name before delete.
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			
		    // Statement used to update existing risks in riskEvent table
			Statement statement = database.createStatement();
			statement.executeUpdate("DELETE FROM project WHERE pRecID="+pRecID);
		    statement.close();
		    database.close();
		    // Send Slack Message
	        lastUpdate.deletedProject(pName);
		    
		    return Response.ok().entity("{\"id\":"+pRecID+"}").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	// List all projects.
	public static List<Project> listProject()
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
			Statement ulStatement = database.createStatement();
			List<Project> pList = new ArrayList<Project>();
			
			// Add each project found in database to a list of risk objects.
		    ResultSet project = statement.executeQuery("SELECT * FROM project;");
	        while (project.next()) {
	        	Project p = new Project();
	        	p.setid(project.getInt("pRecID"));
	        	p.setpName(project.getString("pName"));
	        	p.setpmName(project.getString("pmName"));
	        	// Users attribute is an array of users with access to this project. They are stored in a  separate table which
	        	// is queried below.
	        	ResultSet users = ulStatement.executeQuery("SELECT user FROM user WHERE fProject='"+p.getpName()+"';");
	        	List<String> uList = new ArrayList<String>();
	        	while (users.next()){
	        		String user = users.getString("user");
	        		uList.add(user);
	        	}
	        	p.setUsers(uList);
	        	pList.add(p);
	        }
	        statement.close();
		    database.close();
	        return pList;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
			
		}
	}
	
	
	// Get project name using project ID
	public static String projectName(Integer pID)
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
			String pName = null;
			// Find project name by searching for ID.
		    ResultSet project = statement.executeQuery("SELECT pName FROM project WHERE pRecID="+pID);
		    if(project.next()) {
		    	pName= project.getString("pName");
		    }
	        statement.close();
		    database.close();
	        return pName;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Get project name using project ID
	public static String managerName(Integer pID)
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
			String pmName = null;
			// Find project name by searching for ID.
		    ResultSet project = statement.executeQuery("SELECT pmName FROM project WHERE pRecID="+pID);
		    if(project.next()) {
		    	pmName= project.getString("pmName");
		    }
	        statement.close();
		    database.close();
	        return pmName;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
		
	// Add a new risk to risk table
	public static Response addRisk(String rName, Integer impact, Integer probability, String description, 
			String mitigation, String status, String fProject, Slack lastUpdate)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to add new risks to riskEvent table
			PreparedStatement statement =
					database.prepareStatement("INSERT INTO riskEvent(rName, impact, probability, "
					+ "description, mitigation, status, fProject) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, rName);
			statement.setInt(2, impact);
			statement.setInt(3, probability);
			statement.setString(4, description);
			statement.setString(5, mitigation);
			statement.setString(6, status);
			statement.setString(7, fProject);
			statement.executeUpdate();
		    statement.close();
		    
		    // HTTP Response contains rID and rRecID assigned by database. 
		    Statement statement2 = database.createStatement();
		    ResultSet project = statement2.executeQuery("SELECT rRecID, rID FROM riskEvent WHERE rName='"+rName+
		    		"' AND fProject='"+ fProject +"';");
	        project.first();
	        Integer rRecID= project.getInt("rRecID");
	        Integer rID= project.getInt("rID");
	        statement2.close();
	        database.close();
	        // Send Slack Message
	        lastUpdate.updatedRisk(fProject);
	        
		    return Response.ok().entity("{\"id\":"+rRecID+", \"rID\":"+rID+"}").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	
	// Update a risk in riskEvent table
	public static Response updateRisk(Integer rRecID, Integer rID, String rName, Integer impact, 
			Integer probability, String description, String mitigation, String status, 
			String fProject, Slack lastUpdate)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to update existing risks in riskEvent table
			PreparedStatement statement =
					database.prepareStatement("UPDATE riskEvent SET rName=?, impact=?, probability=?, "
							+ "description=?, mitigation=?, status=?, fProject=? WHERE rRecID=" +rRecID);
			statement.setString(1, rName);
			statement.setInt(2, impact);
			statement.setInt(3, probability);
			statement.setString(4, description);
			statement.setString(5, mitigation);
			statement.setString(6, status);
			statement.setString(7, fProject);
			statement.executeUpdate();
		    statement.close();
		    
		    database.close();
		    // Send Slack Message
	        lastUpdate.updatedRisk(fProject);
	        
		    return Response.ok().entity("{\"id\":"+rRecID+"}").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	
	public static Response deleteRisk(Integer rRecID, Slack lastUpdate, Integer pID)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to update existing risks in riskEvent table
			Statement statement = database.createStatement();
			statement.executeUpdate("DELETE FROM riskEvent WHERE rRecID='"+rRecID+"';");
		    statement.close();
		    database.close();
		    // Send Slack Message
	        lastUpdate.updatedRisk(projectName(pID));
	        
		    return Response.ok().entity("{\"id\":"+rRecID+"}").build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	// List all risks relating to a project.
	public static List<Risk> listRisk(String pName)
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
		    
			List<Risk> rList = new ArrayList<Risk>();
			
		    // Add each risk found in database to a list of risk objects.
		    ResultSet risks = statement.executeQuery("SELECT * FROM riskEvent WHERE fProject='"+pName+"';");
	        while (risks.next()) {
	        	Risk r = new Risk();
	        	r.setid(risks.getInt("rRecID"));
	        	r.setrID(risks.getInt("rID"));
	        	r.setrName(risks.getString("rName"));
	        	r.setImpact(risks.getInt("impact"));
	        	r.setProbability(risks.getInt("probability"));
	        	r.setDescription(risks.getString("description"));
	        	r.setMitigation(risks.getString("mitigation"));
	        	r.setStatus(risks.getString("status"));
	        	r.setfProject(risks.getString("fProject"));
	        	rList.add(r);
	        }
	        statement.close();
		    database.close();
	        return rList;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
			
		}
	}
	
	
	// Return if a user is authorised view to a project.
	public static Boolean clientAccess(String pName, String email)
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
			
		    // Check if users email is authorised to view project.
		    ResultSet users = statement.executeQuery("SELECT * FROM user WHERE fProject='"+pName+"' AND user='"+email+"';");
	        if (users.next()) {
	        	statement.close();
			    database.close();
	        	return true;
	        }
	        else {
	        	statement.close();
			    database.close();
	        	return false;
	        }
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;	
		}
	}
}
