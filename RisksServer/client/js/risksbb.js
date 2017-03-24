// defines the namespace
window.BBRisk = {
  Models: {},
  Collections: {},
  Views: {},
  Router: {}
};

//IE10 Cache Fix
$.ajaxSetup({ cache: false });

//Build web service url
var rID = window.location.hash.substr(1);
var url = "../server/request/risks/"+rID

// Default Model for Projects
BBRisk.Models.Project = Backbone.Model.extend({
	defaults: {
		rName: ''
	},
	urlRoot: url,
	idAttribute: "id"
});


// Projects Collection
BBRisk.Collections.Risks=  Backbone.Collection.extend({
	model: BBRisk.Models.Project,
	url: url, // Web Service URL for CRUD operations
	
	
	// Long Polling (Code Reference #10)
	longPolling : false,
	
	initialize : function(){
	    _.bindAll(this, "stopLongPolling", "startLongPolling", "executeLongPolling", "onFetch");
	},
	
	stopLongPolling : function(){
		this.longPolling = false;
	},
	startLongPolling : function(){
	    this.longPolling = true;
	    this.executeLongPolling();
	},
	
	executeLongPolling : function(){
	    this.fetch({
	        error: function(model,collecton, options) {
	        	// Call fetch fail function.
	        	errorFetchFail(options.xhr.status);
	        },
	        success: this.onFetch,
	        wait: true // Do not report status until web service response.
	    });
	},
	onFetch : function () {
		if( this.longPolling ){setTimeout(this.executeLongPolling, 500); // Update view each 0.5 seconds
	    }
	},
	
});


// View for Single Project
BBRisk.Views.RiskView = Backbone.View.extend({
	tagName: 'tr',
	className: 'rItem',
	template:$("#rTemplate").html(),
	
	events: {
        'click .delete':   'deleteRisk',
        'click .saveEdit':   'saveEditRisk'
    },
    
    
    // Function used to update existing projects.
    saveEditRisk: function() {
    	//Get Details entered in form
    	var riskrName = document.getElementById("rNameInput"+this.model.get("id")).value;
    	var riskImpact = document.getElementById("impactInput"+this.model.get("id")).value;
    	var riskProbability = document.getElementById("probabilityInput"+this.model.get("id")).value;
    	var riskDescription = document.getElementById("descriptionInput"+this.model.get("id")).value;
    	var riskMitigation = document.getElementById("mitigationInput"+this.model.get("id")).value;
    	var riskStatus = $("input[name=statusInput"+this.model.get("id")+"]:checked").val();
    	var riskfProject = headerpName;
    	
		//Update project with new details.
		var thisObj= this;
		this.model.save({"rName":riskrName, "impact":riskImpact, "probability":riskProbability,
    		"description":riskDescription, "mitigation":riskMitigation, "status":riskStatus, "fProject":riskfProject}, {
		        error: function(model, response) {
		        	// Get Error response and pass to error function.
		            errorNewProject(response);
		        },
		        success: function(model, response) {
		        	errorHideAll();
		        	thisObj.render();
		        	
		        	//Remove Model
		        	$(".modal .close").click();
		        	
		        	
		        	//Remove UI Backdrop from Model that contained form. 
		            //Required to be called manually due to the above render method.
		            $(document).ready(function(){
		            	$(".modal-backdrop").fadeOut(function() {
		            	    // Remove element after the fadeOut
		            		$(".modal-backdrop").remove()
		            		$("#pageBody").removeClass("modal-open");
		            		$("#pageBody").css("padding", "");
		            	});
		            });
		        },
		        wait: true // Do not report status until web service response.
		});
    },
    
    
    // Function to delete a project.
    deleteRisk:function () {
        //Delete model
        this.model.destroy({
	        error: function(model, response) {
	        	console.log(response);
	        	// Show unsuccessful modification message
	        	$("#modFailed").show();
	        },
	        success: function(model, response) {
	        	errorHideAll();
	        },
	        wait: true // Do not report status until web service response.
        });
    },
    
    // Produce HTML in template for a project.
	render: function() {
		var tmpl = _.template(this.template);
        this.$el.html(tmpl(this.model.toJSON()));
        return this;
	},
});

BBRisk.Collections.risks = new BBRisk.Collections.Risks();

// View for collection of Projects
BBRisk.Views.RisksView = Backbone.View.extend({
    el:$("#risks"),

    initialize:function () {
        this.collection = BBRisk.Collections.risks;
        // Get project items from web service.
        this.collection.fetch({
        	success: function(collection, response, options) {
        		// Hide modification elements if user has read only access.
        		var access = options.xhr.getResponseHeader("access");
        		if (access==="client"){
        			readOnly();
        		}
        		//Get project and manager name.
        		headerpName= options.xhr.getResponseHeader("projectName");
        		headerpmName= options.xhr.getResponseHeader("managerName");
        		if (!headerpmName){
        			headerpmName="";
        		}
        		document.getElementById("projectName").textContent = "Project: "+headerpName;
        		document.getElementById("managerName").textContent = "Project Manager: "+headerpmName;
        	},
	        error: function() {
	        	// Call fetch fail function.
	            errorFetchFail();
	        },
	        wait: true // Do not report status until web service response.
	    });
        
        // Call function to produce HTML 
        this.render();
        this.collection.on("add", this.renderRisk, this);
        
        // Connection for external incoming method call to save a new project
        // Allows method calls from outside el("#risks")
        this.bind("newRiskEvent", this.saveNewRisk);
        
        // Begin long polling. Listen for changes within collection and
        // re-render page accordingly.
        this.collection.startLongPolling();
        this.listenTo(this.collection, 'change', this.render);
        this.listenTo(this.collection, 'remove', this.render);  
    },
    
    // Produce HTML for list of risks
    render:function () {
    	this.$el.empty();
        var that = this;
        removeAllPlots();
        _.each(this.collection.models, function (item) {
        	// For each project in list call renderRisk function.
            that.renderRisk(item);
        });
    },
    
    // Produce HTML for an individual risk item.
    renderRisk:function (item) {
    	BBRisk.Views.riskView = new BBRisk.Views.RiskView({
            model:item
        });
        this.$el.append(BBRisk.Views.riskView.render().el);
        plotRisk(item);
    },
    
    // Function to save a new project.
    saveNewRisk: function(){
    	//Get Details entered in form
    	var riskrName = document.getElementById("rNameInput").value;
    	var riskImpact = document.getElementById("impactInput").value;
    	var riskProbability = document.getElementById("probabilityInput").value;
    	var riskDescription = document.getElementById("descriptionInput").value;
    	var riskMitigation = document.getElementById("mitigationInput").value;
    	var riskStatus = $('input[name=statusInput]:checked').val();
    	var riskfProject = headerpName;
        
    	// Save a newly created risk and its details into list.
		saveNew = this.collection.create({"rName":riskrName, "impact":riskImpact, "probability":riskProbability,
    		"description":riskDescription, "mitigation":riskMitigation, "status":riskStatus, "fProject":riskfProject}, {
	        error: function(model, response) {
	        	// Get Error response and pass to error function.
	            errorNewProject(response);
	        },
	        success: function(model, response) {
	        	errorHideAll();
	        	//Remove Model
	        	$(".modal-header .close").click();
	        	//Remove UI Backdrop from Model that contained form. 
	            //Required to be called manually due to the above render method.
	            $(document).ready(function(){
	            	$(".modal-backdrop").fadeOut(function() {
	            	    // Remove element after the fadeOut
	            		$(".modal-backdrop").remove()
	            		$('#pageBody').removeClass("modal-open");
	            		$('#pageBody').css("padding", "");
	            	});
	            });
	        },
	        wait: true // Do not report status until web service response.
		});
    }
});


// Create an instance of projects view
BBRisk.Views.risksView = new BBRisk.Views.RisksView();


// A view created in order to receive events outside of el of projectsView. Code Reference #11.
BBRisk.Views.TFooterView = Backbone.View.extend({
    el: '#pageBody',
    
    // Listens for click on button.
    events: {
        "click .saveNew" : "saveNewRiskLink",
        "drop .gridBox" : "dropUpdate"
    },
    
    // Bind functions to view.
    initialize: function(){
        _.bindAll(this, "saveNewRiskLink", "dropUpdate");
    },
    
    // Function which links to trigger on projectsView
    saveNewRiskLink: function() {
    	BBRisk.Views.risksView.trigger("newRiskEvent");
    },
    // Function for handling of a drop event when risks are plotted.
    dropUpdate: function(event) {
    	var plotID = event.originalEvent.dataTransfer.getData("text");// Get element id of risk plot object.
    	var rId = document.getElementById(plotID).getAttribute("data-rid");// Get project specific risk ID of dragged risk
    	var collection= BBRisk.Collections.risks; // Get collection of risks.
    	var model = collection.findWhere({rID : parseInt(rId)}); // Find model of risk with matching risk id.
    	// Get impact and probability values of box risk is dropped within.
    	var riskImpact = event.currentTarget.dataset.impact;
    	var riskProbability = event.currentTarget.dataset.prob;
    	
    	// Update Model with new impact and/or probability
    	model.save({"impact":riskImpact, "probability":riskProbability}, {
		        error: function(model, response) {
		        	// Get Error response and pass to error function.
		        	riskGridFail(response);
		        },
		        success: function(model, response) {
		        	errorHideAll();
		        	// Move risk marker on grid to its new position
		        	event.originalEvent.preventDefault();
		        	$('div[data-impact~='+parseInt(riskImpact)+'][data-prob~='+parseInt(riskProbability)+']').append(document.getElementById(plotID));
		        },
		        wait: true // Do not report status until web service response.
		});
    }
    	
    
});


// Create an instance of tFooterView
BBRisk.Views.tFooterView = new BBRisk.Views.TFooterView();

