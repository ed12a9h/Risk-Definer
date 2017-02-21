/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for defining the routing of URLs 
 * to java functions.
 */

package RiskApplication.RisksServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


// Call containing routing for all urls
@Path("/request")
public class RiskServer {
	
	// Method to add new project to project table. Accepts new project details in json
	// and returns success status to client.
    @POST
    @Path("/addproject/")
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addProject(final Project project) throws IOException, SQLException {
	    return DBConnection.addProject(project.getpName(), project.getpmName());
    }
	
    
    // Method to add new risk to riskEvent table. Accepts new risks details in json
 	// and returns success status to client.
    @POST
    @Path("/addrisk/")
    @Produces(MediaType.TEXT_HTML)
    public Response addRisk(final Risk risk) throws IOException, SQLException {
	    return DBConnection.addRisk(risk.getrName(), risk.getImpact(), risk.getProbability(),
	    		risk.getDescription(), risk.getMitigation(), risk.getStatus(), risk.getProject());
    }
    
    
}