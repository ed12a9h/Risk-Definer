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
	var gridElement = '<div class="gridItem" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")
	+'" width="88" height="31" data-rid='+item.get("rID")+'>'+item.get("rID")+'</div>';
    $('div[data-impact~='+item.get("impact")+'][data-prob~='+item.get("probability")+']').append(gridElement);
}

// Remove a risk from grid.
function removeRiskPlot(rID) {
	$('#drag'+rID).remove();
}

// Remove all risks from grid.
function removeAllPlots() {
	$('.gridItem').remove();
}
