/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This Javascript file provides functions for presenting and hiding login
 * screens and other security features.
 */

// Stores login token in local storage and hides login window after successful login.
function onSignIn(googleUser) {
    // Get ID Token and user name then save in Local Storage
    var id_token = googleUser.getAuthResponse().id_token;
    localStorage.setItem("rd_id_token", id_token);
    var profile = googleUser.getBasicProfile();
    localStorage.setItem("g_user_name", profile.getName());
    
    // Google Sign-in id token must be sent in the header of all requests to web service.
    // Web service will verify this token for authentication purposes.
    $.ajaxSetup({
    	headers: {'token':localStorage.getItem("rd_id_token")}
    });
    
    // Load backbone script after login.
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = 'js/projectsbb.js';
    document.getElementById('bbScriptHolder').appendChild(script);
    
    // Hide Login window and reveal web application
    $('.container').show();
    $('#loginWindow').hide();
};


// Sign user out. Remove data from HTML. Hide web app and show login window.
function signOut() {
	var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
    	jQuery(document).empty();
    	localStorage.removeItem("rd_id_token");
    	localStorage.removeItem("g_user_name");
    	// remove project details from HTML
    	$('.pItem').remove();
    	$('#bbScriptHolder').remove();
    	$.ajaxSetup({
    	    headers: {
    	        'token':'none'
    	    }
    	});
    	
    	//Refresh Page
    	window.location.reload()
    	
    	// Hide web application and display login window
    	$('.container').hide();
    	$('#loginWindow').show();
    });
};


