/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for defining the routing of URLs 
 * to java functions.
 */

package RiskApplication.RisksServer;

//Other Imports
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//Jersey Imports
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


// Call containing routing for all urls
@Path("/request")
public class RiskServer {
	
	// Method to add new project to project table. Accepts new project details in json
	// and returns success status to client.
    @POST
    @Path("/projects/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addProject(final Project project) throws IOException, SQLException {
    	if (project.validate()==true){
    		return DBConnection.addProject(project.getpName(), project.getpmName());
    	}
    	else return Response.status(500).build();
	    
    }
    
    
    // Method to update an existing project to project table. Accepts updated risks details in json
   	// and returns success status to client.
    @PUT
    @Path("/projects/{pRecID}/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_HTML)
    public Response updateProject(@PathParam("pRecID") Integer pRecID, final Project project) 
    		throws IOException, SQLException {
    	if (project.validate()==true){
    		return DBConnection.updateProject(pRecID, project.getpName(), project.getpmName());
    	}
    	else return Response.status(500).build();
    }
    
    
    // Method to remove an existing risk from the riskEvent table.
    @DELETE
    @Path("/projects/{pRecID}/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_HTML)
    public Response deleteProject(@PathParam("pRecID") Integer pRecID) throws IOException, SQLException {
    	return DBConnection.deleteProject(pRecID);
    }
     
	
    // Method to list all projects from project table.
    @GET
    @Path("/projects/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> listProjects() throws IOException, SQLException {
    	System.out.println("Request");
    	return DBConnection.listProject();
     }
    
    // Method to add new risk to riskEvent table. Accepts new risks details in json
 	// and returns success status to client.
    @POST
    @Path("/risks/")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addRisk(final Risk risk) throws IOException, SQLException {
    	if (risk.validate()==true){
    		return DBConnection.addRisk(risk.getrName(), risk.getImpact(), risk.getProbability(),
    	    		risk.getDescription(), risk.getMitigation(), risk.getStatus(), risk.getfProject());
    	}
    	else return Response.status(500).build();
	    
    }
    
    
    // Method to update an existing risk to riskEvent table. Accepts updated project details in json
  	// and returns success status to client.
    @PUT
    @Path("/risks/{rRecID}/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_HTML)
    public Response updateRisk(@PathParam("rRecID") Integer rRecID, final Risk risk) 
    		throws IOException, SQLException {
    	if (risk.validate()==true){
    		return DBConnection.updateRisk(rRecID, risk.getrID(), risk.getrName(), risk.getImpact(), 
        			risk.getProbability(), risk.getDescription(), risk.getMitigation(), risk.getStatus(), 
        			risk.getfProject());
    	}
    	else return Response.status(500).build();
    	
    }
    
    
    // Method to remove an existing risk from the riskEvent table.
    @DELETE
    @Path("/risks/{rRecID}/")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_HTML)
    public Response deleteRisk(@PathParam("rRecID") Integer rRecID) throws IOException, SQLException {
    	return DBConnection.deleteRisk(rRecID);
    }
    
    
    // Method to list all risks for a specific project from riskEvent table.
    @GET
    @Path("/risks/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Risk> listRisks() throws IOException, SQLException {
    	return DBConnection.listRisk();
    }
    
    
}