/**
 * Risk Definer Web Application
 * Produced by Adam Hustwit
 * 
 * This file handles the HTML5 drag and drop functionality and plotting of risks onto grid on page load.
 */

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
	$('.riskItemPop').remove();// Hide pop-over
	$('.popover-content:empty').parent().remove();//Hide unwanted old pop-over
    ev.dataTransfer.setData("text", ev.target.id);
}

// Plot a risk onto grid.
function plotRisk(item) {
	$('.riskItemPop').remove();// Hide pop-over
	$('.popover-content:empty').parent().remove();//Hide unwanted old pop-over
	// Items with closed status given grey background.
	if (item.get("status")==="Closed"){
		var gridElement = '<div class="gridItem gRisk" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")+'" style="background-color:grey;"'
			+' width="88" height="31" data-rid='+item.get("rID")+'" data-toggle="modal" data-target="#editModal'+item.get("id") +'" data-content="<p><b>'
			+'Risk Name: </b>'+item.get("rName")+'<br><b>Status: </b>'+item.get("status")+'<br><b>Impact: </b>'+item.get("impact")+'<br><b>Probability: </b>'
			+item.get("probability")+'<br><strong>Click risk number to view or edit details.</strong></p>">'+item.get("rID")+'</div>';
	}
	else {
		var gridElement = '<div class="gridItem gRisk" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")+'" width="88" height="31"'
			+' data-rid='+item.get("rID")+' data-toggle="modal" data-target="#editModal' +item.get("id")+'" data-content="<p><b>Risk Name: </b>'
			+item.get("rName")+'<br><b>Status: </b>'+item.get("status")+'<br><b>Impact: </b>'+item.get("impact")+'<br><b>Probability: </b>'
			+item.get("probability")+'<br><strong>Click risk number to view or edit details.</strong></p>" >'+ item.get("rID")+'</div>';
	}
	// Risk is placed in grid box with matching impact and probability.
    $('div[data-impact~='+item.get("impact")+'][data-prob~='+item.get("probability")+']').append(gridElement);
    // Add hover over pop-over to risk items
    $('#drag'+item.get("rID")).popover({
		html: true,
		placement: "bottom",
		trigger: "hover"
	}).data('bs.popover').tip().addClass('riskItemPop');
	
}


// Ensure each risk grid box contains a maximum of 8 risks.
// Furthers risks stored in popup modal.
function gridOverflow () {
	// Iterate over each grid box.
	$('.gridBox').each(function(box, obj) {
		var overflowEl=""; //Storage of elements overflowing grid box.
		// Test for risk grid boxes with more than 8 risks.
		if ($(this).find(".gridItem").length > 8){
			// Add  8th and following risks HTML to a string.
			// Then remove 8th and following risks from DOM.
			$(this).find(".gridItem").filter(":gt(6)").each(function(riskEl, obj) {
				var childEl = document.getElementById($(this).attr("id"));
				overflowEl= overflowEl+childEl.outerHTML;
				$(this).hide();
			});
			
			// Display plus icon to allow user to choose to display further risks
			var gridPlusElement = '<div class="gridItem gridPlusItem" width="88" height="31"'
			+' data-placement="bottom" data-toggle="popover" data-container="body">+</div>';
		    $(this).append(gridPlusElement);
		 	// Add hover pop-over functionality to plus icon.
			$(this).children('.gridPlusItem').popover({
				html: true,
				content: function() {return overflowEl;}
			}).on("shown.bs.popover", function (e) {
				  $('.gRisk').popover({html:true, placement: 'bottom', trigger:"hover"});});
		}
	});
	
	// Close pop-over containing additional risks on click on risk or outside pop-over.
	// INSERT CODE REFERENCE   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	$(document).on('click', function (e) {
		if($(event.target).hasClass('gRisk'))
        {
			$('.popover').remove();// Hide pop-over           
        }
		else {
			$('[data-toggle="popover"],[data-original-title]').each(function () {
		        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {                
		            (($(this).popover('hide').data('bs.popover')||{}).inState||{}).click = false
		        }
		    });
		}
	    
	});
	
	
}

// Remove a risk from grid.
function removeRiskPlot(rID) {
	$('#drag'+rID).remove();
}

// Remove all risks from grid.
function removeAllPlots() {
	$('.gridItem').remove();
}
