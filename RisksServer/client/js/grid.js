/**
 * Risk Definer Web Service
 * Produced by Adam Hustwit
 * 
 * This file handles the HTML5 drag and drop functionality and plotting of risks onto grid on page load.
 */

function allowDrop(ev) {
  ev.preventDefault();
}

function drag(ev) {
  ev.dataTransfer.setData("text", ev.target.id);
}

// Plot a risk onto grid.
function plotRisk(item) {
	// Items with closed status given grey background.
	if (item.get("status")==="Closed"){
		var gridElement = '<div class="gridItem" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")
		+'" style="background-color:grey;" width="88" height="31" data-rid='+item.get("rID")+' data-toggle="modal"' 
		+'data-target="#editModal'+item.get("id")+'" onclick="errorHideAll()">'+item.get("rID")+'</div>';
	}
	else {
		var gridElement = '<div class="gridItem" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")
		+'" width="88" height="31" data-rid='+item.get("rID")+' data-toggle="modal"' 
		+' data-target="#editModal'+item.get("id")+'" onclick="errorHideAll()">'+item.get("rID")+'</div>';
	}
    $('div[data-impact~='+item.get("impact")+'][data-prob~='+item.get("probability")+']').append(gridElement);
	
}


// Ensure each risk grid box contains a maximum of 8 risks.
// Furthers risks stored in popup modal.
function gridOverflow () {
	// Iterate over each grid box.
	$('.gridBox').each(function(box, obj) {
		var overflowEl=""; //Storage of elements overflowing grid box.
		// Test for risk grid boxes with more than 8 risks.
		if ($(this).children().length > 8){
			// Add  8th and following risks HTML to a string.
			// Then remove 8th and following risks from DOM.
			$(this).children().filter(":gt(6)").each(function(riskEl, obj) {
				var childEl = document.getElementById($(this).attr("id"));
				overflowEl= overflowEl+childEl.outerHTML;
				$(this).hide();
			});
			
			// Display plus icon to allow user to choose to display further risks
			var gridPlusElement = '<div class="gridItem gridPlusItem" width="88" height="31"'
			+' tabindex="0" data-placement="bottom" data-trigger="hover focus" rel="popover">+</div>';
		    $(this).append(gridPlusElement);
		 	// Add hover pop-over functionality to plus icon.
			$(this).children('.gridPlusItem').popover({
				html: true,
				content: function() {
				      return overflowEl;
				    }
			});
		}
	});
	//$('.gridPlusItem').popover();
	//Hide pop-over on click elsewhere
	$('.popover-dismiss').popover({trigger: 'focus'})
}

// Remove a risk from grid.
function removeRiskPlot(rID) {
	$('#drag'+rID).remove();
}

// Remove all risks from grid.
function removeAllPlots() {
	$('.gridItem').remove();
}
