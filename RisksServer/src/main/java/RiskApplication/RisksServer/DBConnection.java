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
		FileInputStream input = new FileInputStream("jdbc.properties");
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
		String freshProjectDB = new String(Files.readAllBytes(Paths.get("freshProjectDB.txt"))); 
		statement.executeUpdate(freshProjectDB);
		//Create new riskEvent table
		String freshRiskDB = new String(Files.readAllBytes(Paths.get("freshRiskDB.txt"))); 
		statement.executeUpdate(freshRiskDB);
		//Try create a trigger to automatically increment risk number unique to project.
		String freshRiskTrig = new String(Files.readAllBytes(Paths.get("freshRiskTrigger.txt")));
		try{
			statement.executeUpdate(freshRiskTrig);
		}
		catch(Exception e){
			//Do nothing - Trigger already exists.
		}
		
		
		statement.close();
		database.close();
	}
		

	// Add a new project to project table
	public static Response addProject(String pName, String pmName)
	{
		try {
			Connection database = getConnection();
			DBConnection.createTable();
			
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
	        database.close();
		    return Response.ok().entity("{\"id\":"+pRecID+"}").build();
		}
		catch (Exception e){
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	
	// Update a risk in riskEvent table
	public static Response updateProject(Integer pRecID, String pName, String pmName)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
			// Statement used to update existing project in project table
			PreparedStatement statement =
			database.prepareStatement("UPDATE project SET pName=?, pmName=? WHERE pRecID=" +pRecID);
			statement.setString(1, pName);
			statement.setString(2, pmName);
			statement.executeUpdate();
			
		    statement.close();
		    database.close();
		    return Response.ok().build();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
		
	
	public static Response deleteProject(Integer pRecID)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to update existing risks in riskEvent table
			Statement statement = database.createStatement();
			statement.executeUpdate("DELETE FROM project WHERE pRecID="+pRecID);
		    statement.close();
		    database.close();
		    return Response.ok().build();
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
		    
			List<Project> pList = new ArrayList<Project>();
			// Add each project found in database to a list of risk objects.
		    ResultSet project = statement.executeQuery("SELECT * FROM project;");
	        while (project.next()) {
	        	Project p = new Project();
	        	p.setid(project.getInt("pRecID"));
	        	p.setpName(project.getString("pName"));
	        	p.setpmName(project.getString("pmName"));
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
	
	// Add a new risk to risk table
	public static Response addRisk(String rName, Integer impact, Integer probability, String description, 
			String mitigation, String status, String fProject)
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
			String fProject)
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
		    return Response.ok().build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	
	public static Response deleteRisk(Integer rRecID)
	{
		try {
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to update existing risks in riskEvent table
			Statement statement = database.createStatement();
			statement.executeUpdate("DELETE FROM riskEvent WHERE rRecID="+rRecID);
		    statement.close();
		    database.close();
		    return Response.ok().build();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}	
	}
	
	// List all risks relating to a project.
	public static List<Risk> listRisk()
	{
		try {
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
		    
			List<Risk> rList = new ArrayList<Risk>();
			
		    // Add each risk found in database to a list of risk objects.
		    ResultSet risks = statement.executeQuery("SELECT * FROM riskEvent;");
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
}
