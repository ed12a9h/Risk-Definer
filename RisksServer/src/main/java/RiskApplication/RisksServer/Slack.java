/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for all interaction with the slack API.
 */

package RiskApplication.RisksServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// Imports
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Slack {
	private Map<String, Timer> timers = new HashMap<String,Timer>();
	
	
	
	// Send slack notification of newly added project.
	public void addProject(String pName, String pmName){
		try {
			// Slack cannot handle single speech mark. Replace with double.
			pName = pName.replaceAll("\"", "'");
			// Send message to Slack API
			CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
		    request.setEntity(new StringEntity("{\"text\":\"A New Project was just added!\n*Project Name:* "+pName+" \n*Project Manager:* "
		    			+pmName+"\n<https://riskdefiner.me/client/|Click To Launch Risk Definer>\"}"));

		    client.execute(request);
		    client.close();
		} 
		catch (Exception e) {
			System.out.println("Slack Message Failure");
			e.printStackTrace();
		}
	}
	
	// Send slack notification of an updated project.
	public void updatedProject(String pName, String pmName){
		try {
			// Slack cannot handle single speech mark. Replace with double.
			pName = pName.replaceAll("\"", "'");
			// Send message to Slack API
			CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
		    request.setEntity(new StringEntity("{\"text\":\"A project was updated with the following details...\nProject Name: "+pName+
		    		" \nProject Manager: "+pmName+"\n<https://riskdefiner.me/client/|Click To Launch Risk Definer>\"}"));
		    client.execute(request);
		    client.close();
		} 
		catch (Exception e) {
			System.out.println("Slack Message Failure");
			e.printStackTrace();
		}
	}
	
	// Send slack notification of an updated project.
	public void deletedProject(String pName){
		try {
			// Slack cannot handle single speech mark. Replace with double.
			pName = pName.replaceAll("\"", "'");
			// Send message to Slack API
			CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
		    request.setEntity(new StringEntity("{\"text\":\"The Project '"+pName
		    		+"' was just deleted.\n<https://riskdefiner.me/client/|Click To Launch Risk Definer>\"}"));
		    client.execute(request);
		    client.close();
		} 
		catch (Exception e) {
			System.out.println("Slack Message Failure");
			e.printStackTrace();
		}
	}
	
	// Send slack notification when risks are updated on a project.
	// A single risk update starts a 5 minute timer. If another risk on the same project is made the timer is reset.
	// This avoids filling the users slack feed with messages. Below function called on every new or updated risk.
	public void updatedRisk(final String fProject){
		try {
			// Get existing timer and remove.
			Timer oldTimer = (Timer)this.timers.get(fProject);
			if (oldTimer!=null){
				oldTimer.cancel(); //this will cancel the current task. if there is no active task, nothing happens
				System.out.println(oldTimer);
				timers.remove(fProject);
			}
			// Create new timer and function timer executes
			Timer timer= new Timer();
			TimerTask action = new TimerTask() {
		        public void run() {
		        	try {
		        		// Slack cannot handle single speech mark. Replace with double.
		    			String pName = fProject.replaceAll("\"", "'");
		        		// Send message to Slack API
		        		CloseableHttpClient client = HttpClients.createDefault();
		    		    HttpPost request = new HttpPost("https://hooks.slack.com/services/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");
		    		    request.setEntity(new StringEntity("{\"text\":\"Risks have been updated on the project '"+pName
		    		    		+"'.\n<https://riskdefiner.me/client/|Click To Launch Risk Definer>\"}"));
		    		    client.execute(request);
		    		    client.close();
		    		    System.out.println("time run");
		        	}
		        	catch (Exception e) {
		    			e.printStackTrace();
		    		}
		    		finally {
		        		timers.remove(fProject);
		        	}
		        }
		    };
			timer.schedule(action, 300000); // Start timer and assign function
		    this.timers.put(fProject, timer); // Add timer to hash map.
		}
	    catch (Exception e) {
	    	System.out.println("Slack Message Failure");
			e.printStackTrace();
		}
	}
}
