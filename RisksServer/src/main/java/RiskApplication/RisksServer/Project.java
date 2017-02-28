/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file defines the class used for conversion of 
 * json relating to projects
 */

package RiskApplication.RisksServer;

public class Project {
	private Integer id;
	private String pName;
    private String pmName;
    

    public Integer getid() {
        return id;
    }
    public String getpName() {
        return pName;
    }
    public String getpmName() {
        return pmName;
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
    
    public boolean validate(){
		if (this.pName.length() >=2)
			return true;
		else return false;
    }
}
