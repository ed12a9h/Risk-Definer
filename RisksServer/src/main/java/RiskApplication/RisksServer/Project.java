/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file defines the class used for conversion of 
 * json relating to projects
 */

package RiskApplication.RisksServer;

public class Project {
	private Integer pRecID;
	private String pName;
    private String pmName;
    

    public Integer getpRecID() {
        return pRecID;
    }
    public String getpName() {
        return pName;
    }
    public String getpmName() {
        return pmName;
    }
    public void setpRecID(Integer pRecID) {
    	this.pRecID = pRecID;
    }
    public void setpName(String pName) {
    	this.pName = pName;
    }
    public void setpmName(String pmName) {
    	this.pmName = pmName;
    }
}
