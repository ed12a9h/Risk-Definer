/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for the start up of the jetty server which 
 * contains my jersey web service framework as a servlet.
 */

package RiskApplication.RisksServer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.Locker;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;


public class App 
{
    public static void main( String[] args )
    {
    	//Create server port 9999 Local host
    	Server server = new Server(9999);
    	
    	//Servlet Config for web service (/server)
    	ResourceConfig config = new ResourceConfig();
    	config.packages("RiskApplication");
    	ServletHolder servlet = new ServletHolder(new ServletContainer(config));
    	ServletContextHandler sctxHandler = new ServletContextHandler(server, "/*");
    	sctxHandler.addServlet(servlet, "/server/*");    	
    	
    	 
    	// Web Application Hosting Config (/client)
    	ResourceHandler rHandler = new ResourceHandler();
    	rHandler.setResourceBase("client");
    	ContextHandler rctxHandler = new ContextHandler ("/client/*");
    	rctxHandler.setHandler(rHandler);
    	
    	// Combining of both context handlers
    	HandlerList hList = new HandlerList();
    	hList.setHandlers(new Handler[]{rctxHandler, sctxHandler});
    	server.setHandler(hList);

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
