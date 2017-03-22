/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file maintains the state of the risk and project forms.
 */

// Resets the fields of the new risk form.
function emptyRiskFields(){
	document.getElementById("rNameInput").value= "";
	document.getElementById("impactInput").value= "";
	document.getElementById("probabilityInput").value= "";
	document.getElementById("descriptionInput").value= "";
	document.getElementById("mitigationInput").value= "";
	$("#sDefault").prop('checked',true);
}

function emptyProjectFields(){
	document.getElementById('pmNameInput').value = localStorage.getItem('g_user_name');
	document.getElementById('pNameInput').value = '';
}

function addAnEmail(id) {
	var fContent = '<input type="email" class="form-control usersInput" placeholder="users google email" aria-describedby="sizing-addon2" maxlength="100">';
	var fFieldDiv = document.getElementById("addUsers"+id);
	fFieldDiv.innerHTML = fFieldDiv.innerHTML  + fContent 
}

function getEmails(id) {
	var usersArray = []
	$(".usersInput"+id).each(function() {
		if ($(this).val()){
			usersArray.push($(this).val())
		}
	});
	return usersArray;
}

// Pop up message for user to confirm they wish to delete project and associated risks.
function checkDelete() {
    var conf = confirm("This will delete all project risks.\nPress OK to confirm deletion of project.");
    if (conf == true) {
        return true;
    } 
    else {
    	return false;
    }
}