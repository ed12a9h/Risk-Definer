/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file defines the class used for conversion of 
 * json relating to risks
 */

package RiskApplication.RisksServer;

import java.util.List;

import javax.ws.rs.core.Response;

public class Risk {
	private Integer id;
	private Integer rID;
	private String rName;
	private Integer impact;
	private Integer probability;
	private String description;
	private String mitigation;
	private String status;
	private String fProject;
    private String vErrors = "";
    private Integer veCount = 0;
	
	
	public Integer getid() {
		return id;
	}
	public void setid(Integer id) {
		this.id = id;
	}
	public Integer getrID() {
		return rID;
	}
	public void setrID(Integer rID) {
		this.rID = rID;
	}
	public String getrName() {
		return rName;
	}
	public void setrName(String rName) {
		this.rName = rName;
	}
	public Integer getImpact() {
		return impact;
	}
	public void setImpact(Integer impact) {
		this.impact = impact;
	}
	public Integer getProbability() {
		return probability;
	}
	public void setProbability(Integer probability) {
		this.probability = probability;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMitigation() {
		return mitigation;
	}
	public void setMitigation(String mitigation) {
		this.mitigation = mitigation;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getfProject() {
		return fProject;
	}
	public void setfProject(String fProject) {
		this.fProject = fProject;
	}
	
	
	// Method returns false if a risk name matches with a name which already exists in database.
    private boolean validateUnique() {
    	List<Risk> rList = DBConnection.listRisk(this.fProject);
    	for (Risk risk : rList) {
    		if (risk.id!=this.id){
	    	    if (risk.rName.equalsIgnoreCase(this.rName)){
	    			return false;
	    	    }
    		}
    	}
    	return true;
    }
    
    
    // Validate update to a project
    public Response validateUpdate(Slack lastUpdate){
		if (this.rName.length() <=1){
			vErrors= vErrors + "\"Risk name must be longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.rName.length() >=65){
			vErrors= vErrors + "\"Risk name should be less than 65 characters.\", ";
			veCount = veCount+1;
		}
		if (validateUnique() ==false){
			vErrors= vErrors + "\"Risk name already exists.\", ";
			veCount = veCount+1;
		}
		if (this.impact<1  || this.impact>10){
			vErrors= vErrors + "\"Impact must be between 1 and 10.\", ";
			veCount = veCount+1;
		}
		if (this.probability<1  || this.probability>10){
			vErrors= vErrors + "\"Likelihood must be between 1 and 10.\", ";
			veCount = veCount+1;
		}
		if (!this.status.equals("Open") && !this.status.equals("Closed")){
			vErrors= vErrors + "\"Status Must be open or closed.\", ";
			veCount = veCount+1;
		}
		// No validation errors - submit to database
		if (veCount==0){
			return DBConnection.updateRisk(getid(), getrID(), getrName(), getImpact(), getProbability(),
    	    		getDescription(), getMitigation(), getStatus(), getfProject(), lastUpdate);
		}
		// Validation errors - send errors to client with 500 response 
		else {
			vErrors = vErrors.substring(0, vErrors.length() - 2);
			return Response.status(422).entity("{\"error\": [" + vErrors + "], \"errorType\":\"validation\"}").build();
		}
    }
    
    
    // Validate addition of a new project
    public Response validateAdd(Slack lastUpdate){
		if (this.rName.length() <=1){
			vErrors= vErrors + "\"Risk name must be longer than one character.\", ";
			veCount = veCount+1;
		}
		if (this.rName.length() >=250){
			vErrors= vErrors + "\"Risk name should be less than 250 characters.\", ";
			veCount = veCount+1;
		}
		if (validateUnique() ==false){
			vErrors= vErrors + "\"Risk name already exists.\", ";
			veCount = veCount+1;
		}
		if (this.impact<1  || this.impact>10){
			vErrors= vErrors + "\"Impact must be between 1 and 10.\", ";
			veCount = veCount+1;
		}
		if (this.probability<1  || this.probability>10){
			vErrors= vErrors + "\"Lieklihood must be between 1 and 10.\", ";
			veCount = veCount+1;
		}
		if (!this.status.equals("Open") && !this.status.equals("Closed")){
			vErrors= vErrors + "\"Status Must be open or closed.\", ";
			veCount = veCount+1;
		}
		// No validation errors - submit to database
		if (veCount==0){
			return DBConnection.addRisk(getrName(), getImpact(), getProbability(),
    	    		getDescription(), getMitigation(), getStatus(), getfProject(), lastUpdate);
		}
		// Validation errors - send errors to client with 500 response 
		else {
			vErrors = vErrors.substring(0, vErrors.length() - 2);
			return Response.status(422).entity("{\"error\": [" + vErrors + "], \"errorType\":\"validation\"}").build();
		}
    }
}
	
