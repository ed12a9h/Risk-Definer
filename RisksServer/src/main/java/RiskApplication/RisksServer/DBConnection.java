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

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit <p>
 * 
 * This class contains code for all communication between Jersey
 * and the database.
 * 
 * @author Adam Hustwit
 */
public class DBConnection {
	
	/**
     * Establish connection to database.
     * 
     * @return A connection to send SQL statements through.
     */
    public static Connection getConnection() throws IOException, SQLException{
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
	
    
    /**
     * Creates tables to hold the data and a trigger if they do not already exist.
     */
    public static void createTable() throws SQLException, IOException, ClassNotFoundException{
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
	
    
    /**
     * Add a new project to project table. 
     * 
     * @param pmName Project manager name.
     * @param pName Name of project user is attempting to add.
     * @param users A list of users authorised to access the project.
     * @return A HTTP response message to client. Returns HTTP message with new project ID
     * if project is successful. Otherwise returns HTTP response code 500.
     */
    public static Response addProject(String pName, String pmName, List<String> users){
        Connection database = null;
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
            PreparedStatement statement2 = database.prepareStatement("SELECT pRecID FROM project WHERE pName=?;");
            statement2.setString(1, pName);
            ResultSet project = statement2.executeQuery();
            project.first();
            Integer pRecID = project.getInt("pRecID");
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
            } 
            catch (Exception e2) {
            	e2.printStackTrace();
            	return Response.status(500).build();
            }
        }	
    }
	
	
    /**
     * Update a project in project table. 
     * 
     * @param pRecID ID of project to be updated.
     * @param pmName Project manager name.
     * @param pName Name of project user is attempting to add.
     * @param users A list of users authorised to access the project.
     * @return A HTTP response message to client. Returns HTTP message with project ID
     * if project is successful. Otherwise returns HTTP response code 500.
     */
    public static Response updateProject(Integer pRecID, String pName, String pmName, List<String> users){
        Connection database = null;
        try {
            DBConnection.createTable();
            database = DBConnection.getConnection();
            
            // Multiple insert transaction so auto commit turned off so both inserts can be turned off together.
            database.setAutoCommit(false);
            
            // Statement used to update existing project in project table
            PreparedStatement statement = database.prepareStatement("UPDATE project SET pName=?, pmName=? WHERE pRecID=?");
            statement.setString(1, pName);
            statement.setString(2, pmName);
            statement.setInt(3, pRecID);
            statement.executeUpdate();
            statement.close();
            
            //Delete all removed users from users table.
            PreparedStatement ouStatement = database.prepareStatement("Select user FROM user WHERE fProject=?;");
            ouStatement.setString(1, pName);
            ResultSet oldUsersList = ouStatement.executeQuery();
            while (oldUsersList.next()){
                if (!users.contains(oldUsersList.getString("user"))){
                    PreparedStatement dStatement = database.prepareStatement("DELETE FROM user WHERE fProject=? AND user=?;");
                    dStatement.setString(1, pName);
                    dStatement.setString(2, oldUsersList.getString("user"));
                    dStatement.executeUpdate();
                    dStatement.close();
                }
            }
            
            // Array to store newly added users in need of email invitation.
            ArrayList<String> newUsers = new ArrayList<String>(); 
            
            //Add Projects users to users DB
            for (String user:users){
            	//Test if user already exists for project
            	Statement cStatement = database.createStatement();
            	ResultSet foundUser = cStatement.executeQuery("SELECT user FROM user WHERE "
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
	
    
    /**
     * Delete a project from project table. 
     * 
     * @param pRecID ID of project to be deleted.
     * @return A HTTP response message to client. Returns HTTP message with project ID
     * if delete is successful. Otherwise returns HTTP response code 500.
     */
    public static Response deleteProject(Integer pRecID){	
        try {
            String pName = projectName(pRecID);// Get project name before delete.
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            
            // Statement used to update existing risks in riskEvent table
            PreparedStatement statement = database.prepareStatement("DELETE FROM project WHERE pRecID=?");
            statement.setInt(1, pRecID);
            statement.executeUpdate();
            statement.close();
            database.close();
            // Send Slack Message
            Slack message = new Slack();
            message.deletedProject(pName);
            
            return Response.ok().entity("{\"id\":"+pRecID+"}").build();
        } 
        catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }	
    }
	
    
    /**
     * List all projects in project table. 
     * 
     * @return Returns a list of projects if successful. Otherwise returns null.
     */
    public static List<Project> listProject(){
        try {
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            Statement statement = database.createStatement();
            PreparedStatement ulStatement = database.prepareStatement("SELECT user FROM user WHERE fProject=?;");
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
                
                ulStatement.setString(1, p.getpName());
                ResultSet users = ulStatement.executeQuery();
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
	
	
    /**
     * Get project name by provided project ID
     * 
     * @param pID Project ID to find corresponding project name for.
     * @return A project name if found, otherwise null.
     */
    public static String projectName(Integer pID){
        try {
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            String pName = null;
            // Find project name by searching for ID.
            PreparedStatement statement = database.prepareStatement("SELECT pName FROM project WHERE pRecID=?");
            statement.setInt(1, pID);
            ResultSet project = statement.executeQuery();
            if(project.next()) {
            	pName = project.getString("pName");
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
	
    
    /**
     * Get project manager name using project ID
     * 
     * @param pID Project ID to find corresponding project name for.
     * @return A project manager name if found, otherwise null.
     */
    public static String managerName(Integer pID){
        try {
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            String pmName = null;
            // Find project name by searching for ID.
            PreparedStatement statement = database.prepareStatement("SELECT pmName FROM project WHERE pRecID=?");
            statement.setInt(1, pID);
            ResultSet project = statement.executeQuery();
            if(project.next()) {
            	pmName = project.getString("pmName");
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
	
	
    /**
     * Add a new risk to risk event table.
     * 
     * @param rName Risk name.
     * @param fProject Project which Risk Event relates to.
     * @param lastUpdate Slack object used to maintain timer of time since last update.
     * @return A HTTP response message to client. Returns HTTP message with new risk ID and 
     * rRecID if new risk is successful. Otherwise returns HTTP response code 500.
     */
    public static Response addRisk(String rName, Integer impact, Integer probability, String description, 
    		String mitigation, String status, String fProject, Slack lastUpdate){
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
            PreparedStatement statement2 = database.prepareStatement("SELECT rRecID, rID FROM riskEvent WHERE rName=? AND fProject=?;");
            statement2.setString(1, rName);
            statement2.setString(2, fProject);
            ResultSet project = statement2.executeQuery();
            project.first();
            Integer rRecID = project.getInt("rRecID");
            Integer rID = project.getInt("rID");
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
	
	
    /**
     * Update a risk in risk event table.
     * 
     * @param rRecID Risk ID unique for all risks.
     * @param rID Risk ID unique for all risks within a project.
     * @param rName Risk name.
     * @param lastUpdate Slack object used to maintain timer of time since last update.
     * @param fProject Project which Risk Event relates to.
     * @param lastUpdate Slack object used to maintain timer of time since last update.
     * @return A HTTP response message to client. Returns HTTP message with rRecID
     * if updated risk is successful. Otherwise returns HTTP response code 500.
     */
	// Update a risk in riskEvent table
	public static Response updateRisk(Integer rRecID, Integer rID, String rName, Integer impact, 
			Integer probability, String description, String mitigation, String status, 
			String fProject, Slack lastUpdate){
        try {
            DBConnection.createTable();
            
            Connection database = DBConnection.getConnection();
            
            // Statement used to update existing risks in riskEvent table
            PreparedStatement statement =
            		database.prepareStatement("UPDATE riskEvent SET rName=?, impact=?, probability=?, "
            		+ "description=?, mitigation=?, status=?, fProject=? WHERE rRecID=?");
            statement.setString(1, rName);
            statement.setInt(2, impact);
            statement.setInt(3, probability);
            statement.setString(4, description);
            statement.setString(5, mitigation);
            statement.setString(6, status);
            statement.setString(7, fProject);
            statement.setInt(8, rRecID);
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
	
    /**
     * Delete a risk from risk event table. 
     * 
     * @param rRecID ID of risk to be deleted.
     * @param lastUpdate Slack object used to maintain timer of time since last update.
     * @return A HTTP response message to client. Returns HTTP message with rRecID ID
     * if delete is successful. Otherwise returns HTTP response code 500.
     */
    public static Response deleteRisk(Integer rRecID, Slack lastUpdate, Integer pID){
        try {
            DBConnection.createTable();
            
            Connection database = DBConnection.getConnection();
            
            // Statement used to update existing risks in riskEvent table
            PreparedStatement statement = database.prepareStatement("DELETE FROM riskEvent WHERE rRecID=?;");
            statement.setInt(1, rRecID);
            statement.executeUpdate();
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
    
    
    /**
     * List all risks relating to a project.
     * 
     * @param pName Project name to return the related risks for.
     * @return Returns a list of risk events if successful. Otherwise returns null.
     */
    public static List<Risk> listRisk(String pName){
        try {
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            
            List<Risk> rList = new ArrayList<Risk>();
            
            PreparedStatement statement = database.prepareStatement("SELECT * FROM riskEvent WHERE fProject=?;");
            statement.setString(1, pName);
            ResultSet risks = statement.executeQuery();
            // Add each risk found in database to a list of risk objects.
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
    
    
    /**
     * Return if a user is authorised view to a project.
     * 
     * @param pName Project name user wishes to access.
     * @param email Email address attempting to obtain access.
     * @return Returns true for authorised users and false for unauthorised users.
     */
    public static Boolean clientAccess(String pName, String email){
        try {
            DBConnection.createTable();
            Connection database = DBConnection.getConnection();
            
            // Check if users email is authorised to view project.
            PreparedStatement statement = database.prepareStatement("SELECT * FROM user WHERE fProject=? AND user=?;");
            statement.setString(1, pName);
            statement.setString(2, email);
            ResultSet users = statement.executeQuery();
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
