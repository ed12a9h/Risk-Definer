/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file defines the class used for conversion of 
 * json relating to risks
 */

package RiskApplication.RisksServer;

public class Risk {
	private String rID;
	private String rName;
	private Integer impact;
	private Integer probability;
	private String description;
	private String mitigation;
	private String status;
	private String project;
	
	
	public String getrID() {
		return rID;
	}
	public void setrID(String rID) {
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
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	
}
