/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file defines the class used for conversion of 
 * json relating to projects
 */

package RiskApplication.RisksServer;
import java.util.List;

import javax.ws.rs.core.Response;

public class Project {
	private Integer id;
	private String pName;
    private String pmName;
    private String vErrors = "";
    private Integer veCount = 0;
    private List<String> users;
    

    public Integer getid() {
        return id;
    }
    public String getpName() {
        return pName;
    }
    public String getpmName() {
        return pmName;
    }
    public List<String> getUsers() {
    	return users;
    }
    public void setid(Integer id) {
    	this.id = id;
    }
    public void setpName(String pName) {
    	this.pName = pName;
    }
    public void setpmName(String pmName) {
    	this.pmName = pmName;
    }
    public void setUsers(List<String> users) {
    	this.users = users;
    }
    
    
    // Method returns false if a project name matches with a name which already exists in database.
    private boolean validateUnique() {
    	List<Project> pList = DBConnection.listProject();
    	for (Project project : pList) {
    		if (project.id!=this.id){
    			if (project.pName.equalsIgnoreCase(this.pName)){
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    
    // Validate update to a project
    public Response validateUpdate(){
		if (this.pName.length() <=1){
			vErrors= vErrors + "\"Project name must be longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.pmName.length() ==1){
			vErrors= vErrors + "\"Project manager name should be empty or longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.pName.length() >=65){
			vErrors= vErrors + "\"Project name should be less than 65 characters.\", ";
			veCount = veCount+1;
		}
		if (this.pmName.length() >=65){
			vErrors= vErrors + "\"Project manager name should under 65 characters.\", ";
			veCount = veCount+1;
		}
		if (validateUnique() ==false){
			vErrors= vErrors + "\"Project name already exists.\", ";
			veCount = veCount+1;
		}
		// No validation errors - submit to database
		if (veCount==0){
			return DBConnection.updateProject(getid(), getpName(), getpmName(), getUsers());
		}
		// Validation errors - send errors to client with 500 response 
		else {
			vErrors = vErrors.substring(0, vErrors.length() - 2);
			return Response.status(422).entity("{\"error\": [" + vErrors + "], \"errorType\":\"validation\"}").build();
		}
    }
    
    
    // Validate addition of a new project
    public Response validateAdd(){
		if (this.pName.length() <=1){
			vErrors= vErrors + "\"Project name must be longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.pmName.length() ==1){
			vErrors= vErrors + "\"Project manager name should be empty or longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.pName.length() >=250){
			vErrors= vErrors + "\"Project name should be less than 250 characters.\", ";
			veCount = veCount+1;
		}
		if (this.pmName.length() >=100){
			vErrors= vErrors + "\"Project manager name should under 100 characters.\", ";
			veCount = veCount+1;
		}
		if (validateUnique() ==false){
			vErrors= vErrors + "\"Project name already exists.\", ";
			veCount = veCount+1;
		}		
		// No validation errors - submit to database
		if (veCount==0){
			return DBConnection.addProject(getpName(), getpmName(), getUsers());
		}
		// Validation errors - send errors to client with 500 response 
		else {
			vErrors = vErrors.substring(0, vErrors.length() - 2);
			return Response.status(422).entity("{\"error\": [" + vErrors + "], \"errorType\":\"validation\"}").build();
		}
    }
}
