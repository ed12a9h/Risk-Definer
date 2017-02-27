/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for the start up of the jetty server which 
 * contains my jersey web service framework as a servlet.
 */

package RiskApplication.RisksServer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;


public class App 
{
    public static void main( String[] args )
    {
    	//Server config
    	ResourceConfig config = new ResourceConfig();
    	 config.packages("RiskApplication");
    	 ServletHolder servlet = new ServletHolder(new ServletContainer(config));

    	//Port 9999 Local host
    	Server server = new Server(80);
    	 ServletContextHandler context = new ServletContextHandler(server, "/*");
    	 context.addServlet(servlet, "/*");

    	// Try to start server - catch and print any exception
    	try {
    	     server.start();
    	     server.join();
    	 } catch (Exception e) {
			e.printStackTrace();
		} 
    	finally {
    	     server.destroy();
    	 }
    }
}
