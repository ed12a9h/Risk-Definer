/**
 * Risk Definer Web Application
 * Produced by Adam Hustwit
 * 
 * This file contains code handling any unexpected errors or validation errors.
 */

// This code runs on recovery from serious error.
// It displays the error message to users.
document.addEventListener("DOMContentLoaded", function(event) { 
    if (localStorage.getItem("sError")==="True"){
        $("#modFailed").show();
        localStorage.setItem("sError", "False");
    }
});


// Function uses unsuccessful HTTP response and presents received error
// message to the user in web application.
function errorNewProject(response) {
    errorHideAll();
    var json = false
    // Check if response is received.
    try {
        var responseObj = $.parseJSON(response.responseText);
        json=true;
    } 
    catch (e) {}
    // Report validation errors next to form.
    if (json==true && responseObj.errorType==="validation"){
        for (i in responseObj.error) {
            $(".vfReason").append("<li>"+ responseObj.error[i] +"</li>");
        }
        $(".saveFailed").show();
    }
    // More serious error - display unknown error message.
    // Informs user change not saved.
    else {
        localStorage.setItem("sError", "True");
        //Refresh Page
        window.location.reload();
    };
};


// Display error message on risk grid update fail.
function riskGridFail(response) {
    errorHideAll();
    $("#modFailedGrid").show();
}


// Hides all previous error messages.
function errorHideAll() {
    // Hide all error messages
    $(".alert").hide();
    $(".vfReason").empty();
};


// Network connection lost.
function errorFetchFail(status) {
    $('.pItem').remove();
    $('.rItem').remove();
    $('#rGrid').remove();
    if (status===401) {
        $("#unauthorisedAccess").show();
        $('.priv').hide();
    }
    else {
        $("#networkLost").show();
    }	
};






