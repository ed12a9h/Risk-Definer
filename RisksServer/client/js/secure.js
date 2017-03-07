var auth2;


function onSignIn(googleUser) {
    // Get ID Token and user name then save in Local Storage
    var id_token = googleUser.getAuthResponse().id_token;
    localStorage.setItem("rd_id_token", id_token);
    var profile = googleUser.getBasicProfile();
    localStorage.setItem("g_user_name", profile.getName());
    
    
    // Hide Login window and reveal web application
    $('.container').show();
    $('#loginWindow').hide();
    //try{
    //	window.BBProj.Views.projectsView.render()
    //}
    //catch(err){
    //	console.log(err)
    //}
    
    
};

function signOut() {
	var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
    	jQuery(document).empty();
    	localStorage.removeItem("rd_id_token");
    	localStorage.removeItem("g_user_name");
    	// remove project details from HTML
    	$('.pItem').remove();
    	// Hide web application and display login window
    	$('.container').hide();
    	$('#loginWindow').show();
    });
};


