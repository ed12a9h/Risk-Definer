function allowDrop(ev) {
  ev.preventDefault();
}

function drag(ev) {
  ev.dataTransfer.setData("text", ev.target.id);
}

function drop(ev, el) {
  ev.preventDefault();
  var data = ev.dataTransfer.getData("text");
  el.appendChild(document.getElementById(data));
}

function plotRisk(item) {
	var gridElement = '<div class="gridItem" draggable="true" ondragstart="drag(event)" id="drag'+item.get("rID")+'" width="88" height="31">'+item.get("rID")+'</div>';
    $('div[data-impact~='+item.get("impact")+'][data-prob~='+item.get("probability")+']').append(gridElement);
}

function removeRiskPlot(rID) {
	$('#drag'+rID).remove();
}

function removeAllPlots() {
	$('.gridItem').remove();
}