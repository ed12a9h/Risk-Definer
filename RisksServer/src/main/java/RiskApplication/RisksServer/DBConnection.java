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
		
		//Foreign Key Support
		//properties.setProperty("PRAGMA foreign_keys", "ON");
		
		
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
		//statement.executeUpdate("DROP TABLE IF EXISTS riskEvent");
		
		
		// Create a new project table
		String freshProjectDB = new String(Files.readAllBytes(Paths.get("freshProjectDB.txt"))); 
		statement.executeUpdate(freshProjectDB);
		//Create new riskEvent table
		String freshRiskDB = new String(Files.readAllBytes(Paths.get("freshRiskDB.txt"))); 
		statement.executeUpdate(freshRiskDB);
		String freshRiskTrig = new String(Files.readAllBytes(Paths.get("freshRiskTrigger.txt")));
		//statement.executeUpdate(freshRiskTrig);
		
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
	
	// Add a new risk to risk table --Not yet working
	public static Response addRisk(String rName, Integer impact, Integer probability, String description, 
			String mitigation, String status, String project)
	{
		try {
			// Used for Dev purposes ONLY!!!!!!!!!!!!!!!!!!!!!!!!!!!
			DBConnection.createTable();
			
			Connection database = DBConnection.getConnection();
			//Statement xstatement = database.createStatement();
			//System.out.println(xstatement.executeUpdate("PRAGMA foreign_keys"));
			//xstatement.executeUpdate("PRAGMA foreign_keys= ON");
			
			
		    // Statement used to add new risks to users table
			PreparedStatement statement =
					database.prepareStatement("INSERT INTO riskEvent(rName, impact, probability,"
					+ "description, mitigation, status, project) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, rName);
			statement.setInt(2, impact);
			statement.setInt(3, probability);
			statement.setString(4, description);
			statement.setString(5, mitigation);
			statement.setString(6, status);
			statement.setString(7, project);
			statement.executeUpdate();
			
			// Used purely for testing purposes
		    ResultSet risks = statement.executeQuery("SELECT rName, project FROM riskEvent");
	        while (risks.next()) {
	        	String rName1 = risks.getString("rName");
	        	String project1 = risks.getString("project");
	        	System.out.println(rName1 + ":" + project1);
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
}
