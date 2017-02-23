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
		
	
	    return DriverManager.getConnection(url, user, password);
	}
	  

	// Creates a table to hold the data. 
	// This code is not called from main program but can be used.
	// WARNING - deletes existing tables
	public static void createTable() throws SQLException, IOException, ClassNotFoundException
	{
		Connection database = DBConnection.getConnection();
		Statement statement = database.createStatement();
		// Drop existing tables, if present
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
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			
		    // Statement used to add new project to project table
			PreparedStatement statement =
					database.prepareStatement("INSERT INTO project(pName, pmName) VALUES(?,?)");
			statement.setString(1, pName);
			statement.setString(2, pmName);
			statement.executeUpdate();
		    
		    // Used purely for testing purposes
		    ResultSet projects = statement.executeQuery("SELECT pName, pmName FROM project");
	        while (projects.next()) {
	        	String pName1 = projects.getString("pName");
	        	String pmName1 = projects.getString("pmName");
	        	System.out.println(pName1 + ":" + pmName1);
	        }
	        
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
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
		    
			List<Project> pList = new ArrayList<Project>();
			
		    // Used purely for testing purposes
		    ResultSet projects = statement.executeQuery("SELECT pRecID, pName, pmName FROM project");
	        while (projects.next()) {
	        	Project p = new Project();
	        	p.setpRecID(projects.getInt("pRecID"));
	        	p.setpName(projects.getString("pName"));
	        	p.setpmName(projects.getString("pmName"));
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
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
			
			// Used purely for testing purposes
		    ResultSet risks = statement.executeQuery("SELECT rName, fProject FROM riskEvent");
	        while (risks.next()) {
	        	String rName1 = risks.getString("rName");
	        	String fProject1 = risks.getString("fProject");
	        	System.out.println(rName1 + ":" + fProject1);
	        }
			
		    statement.close();
		    database.close();
		    return Response.ok().build();
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
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
	
	// List all risks relating to a project.
	public static List<Risk> listRisk()
	{
		try {
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			DBConnection.createTable();
			Connection database = DBConnection.getConnection();
			Statement statement = database.createStatement();
		    
			List<Risk> rList = new ArrayList<Risk>();
			
		    // Add each risk found in database to a list of risk objects.
		    ResultSet risks = statement.executeQuery("SELECT * FROM riskEvent;");
	        while (risks.next()) {
	        	Risk r = new Risk();
	        	r.setrRecID(risks.getInt("rRecID"));
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
