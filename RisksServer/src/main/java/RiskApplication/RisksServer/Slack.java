/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for all interaction with the slack API.
 */

package RiskApplication.RisksServer;

// Imports
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Slack {
	
	// Send slack notification of newly added project.
	public void addProject(String pName, String pmName){
		try {
			CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
		    request.setEntity(new StringEntity("{\"text\":\"A New Project was just added!\nProject Name: "+pName+" \nProject Manager: "+pmName+"\"}"));
		    client.execute(request);
		    client.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
