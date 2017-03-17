/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code for the start up of the jetty server which 
 * contains my jersey web service framework as a servlet.
 */

package RiskApplication.RisksServer;

import java.util.Collections;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;


public class App 
{
	//Create server port 9999 Local host
	static Server server = new Server(9999);
	
    public static void main( String[] args )
    {
    	//Security
    	//ConstraintSecurityHandler security = userAuth(server);
    	
    	
    	
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
    	
    	
    	// Combining of both context handlers and security
    	HandlerList hList = new HandlerList();
    	hList.setHandlers(new Handler[]{rctxHandler, sctxHandler}); // Security removed now google auth implemented.
    	server.setHandler(hList);
    	
    	// Config for SSL (Code Reference #1,2)
    	HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(443);
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer()); 
        SslContextFactory sslContextFactory = new SslContextFactory("ssl/keystore");
        sslContextFactory.setKeyStorePassword("chUya7RaQ+s3");
        ServerConnector httpsConnector = new ServerConnector(server, 
        		new SslConnectionFactory(sslContextFactory, "http/1.1"),
        		new HttpConnectionFactory(https_config));
        httpsConnector.setPort(443);
        httpsConnector.setIdleTimeout(50000);
        //server.setConnectors(new Connector[]{ httpsConnector });
        
        

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
    
    public static ConstraintSecurityHandler userAuth(Server server){
    	LoginService loginService = new HashLoginService("MyRealm",
                "client/realm.properties");
        server.addBean(loginService);
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[] { "user", "admin" });
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        security.setHandler(new ResourceHandler());
        return security;
    }
}
