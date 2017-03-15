/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file contains code  for validating tokens provided by the 
 * Google Sign-In for websites framework.
 */

package RiskApplication.RisksServer;

// Jersey Imports
import javax.ws.rs.NotAuthorizedException;

//Google Sign-In Imports 
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;


public class AuthenticationFilter {

    // Method that uses google apis to validate tokens issued by Google
	// after a user logs into web application using their Google Account. 
    public static boolean validateToken(String token) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
            .Builder(new NetHttpTransport(), new GsonFactory())
                .build();
        
        //Verify Token
        GoogleIdToken idToken = null;
		try {
			idToken = verifier.verify(token);
		} 
		catch (Exception e) {
			throw new NotAuthorizedException("Invalid token."); //HTTP 401 response
		}
		
		// Return true if token validates and email is authorised.
        if (idToken != null) {
            Payload payload = idToken.getPayload();
            // The below if statement can be used to check if an email belongs to the Elder Studios Google Suite domain. 
            // It also allows the email address riskdefiner@gmail.com
            if (payload.getHostedDomain() != null && payload.getHostedDomain().equals("elder.co.uk") || payload.getEmail().equals("riskdefiner@gmail.com")){
            	return true;
            }
            // If email does not match return 401 response.
            else { 
            	throw new NotAuthorizedException("Invalid token.");
            }
        }
        // If token is null return 401 response.
        else {
        	throw new NotAuthorizedException("Invalid token.");
        }
    }
    
    
    // Method that uses google apis to validate tokens issued by Google
 	// after a user logs into web application using their Google Account. 
    public static String validateTokenIncClient(String token, String pName ) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
            .Builder(new NetHttpTransport(), new GsonFactory())
                .build();
        
        //Verify Token
        GoogleIdToken idToken = null;
 		try {
 			idToken = verifier.verify(token);
 		} 
 		catch (Exception e) {
 			throw new NotAuthorizedException("Invalid token."); //HTTP 401 response
 		}
 		
 		// Return true if token validates and email is authorised.
         if (idToken != null) {
             Payload payload = idToken.getPayload();
             // The below if statement can be used to check if an email belongs to the Elder Studios Google Suite domain. 
             // It also allows the email address riskdefiner@gmail.com
             if (payload.getHostedDomain() != null && payload.getHostedDomain().equals("elder.co.uk") || payload.getEmail().equals("riskdefiner@gmail.com")){
             	return "priv";
             }
             // This statement authorise clients with read-only access to a risk.
             else if (DBConnection.clientAccess(pName, payload.getEmail())) {
             	return "client";
             }
             // If email does not match return 401 response.
             else { 
             	throw new NotAuthorizedException("Invalid token.");
             }
         }
         // If token is null return 401 response.
         else {
         	throw new NotAuthorizedException("Invalid token.");
         }
     }
}
