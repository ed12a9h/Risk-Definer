// Google Sign-in id token must be sent in the header of all requests to web service.
// Web service will verify this token for authentication purposes.
$.ajaxSetup({
    headers: {
        'token':localStorage.getItem("rd_id_token")
    }
});


// defines the namespace
window.BBProj = {
  Models: {},
  Collections: {},
  Views: {},
  Router: {}
};

// Default Model for Projects
BBProj.Models.Project = Backbone.Model.extend({
	defaults: {
		pName: '',
		pmName: ''
	},
	urlRoot: '../server/request/projects/',
	idAttribute: "id"
});


// Projects Collection
BBProj.Collections.Projects=  Backbone.Collection.extend({
	model: BBProj.Models.Project,
	url: '../server/request/projects/', // Web Service URL for CRUD operations
	
	
	//Long Polling:
	longPolling : false,
	intervalMinutes : 0.0001,
	
	initialize : function(){
	    _.bindAll(this, "stopLongPolling", "startLongPolling", "executeLongPolling", "onFetch");
	},
	
	stopLongPolling : function(){
		this.longPolling = false;
	},
	startLongPolling : function(intervalMinutes){
	    this.longPolling = true;
	    if( intervalMinutes ){
	      this.intervalMinutes = intervalMinutes;
	    }
	    this.executeLongPolling();
	},
	
	executeLongPolling : function(){
	    this.fetch({ success : this.onFetch});
	},
	onFetch : function () {
		if( this.longPolling ){
			//this.reset()
			setTimeout(this.executeLongPolling, 1000 * 60 * this.intervalMinutes); // in order to update the view each N minutes
	    }
	},
	
});


// View for Single Project
BBProj.Views.ProjectView = Backbone.View.extend({
	tagName: 'tr',
	className: 'pItem',
	template:$("#pTemplate").html(),
	
	events: {
        'click .delete':   'deleteProject',
        'click .saveEdit':   'saveEditProject'
    },
    
    
    // Function used to update existing projects.
    saveEditProject: function(){
    	// Get contents of edit form.
    	var newProjectpName = document.getElementById("pNameInput"+this.model.get("id")).value;
    	var newProjectpmName =document.getElementById("pmNameInput"+this.model.get("id")).value;
        
    	// Ensure project name is not null.
    	if (!newProjectpName || newProjectpName == "")return;
    	else {
    		//Update project with new details.
    		this.model.save({"pName":newProjectpName, "pmName":newProjectpmName});
            this.render();
    	}
        
        //Remove UI Backdrop from Model that contained form. 
        //Required to be called manually due to the above render method.
        $(document).ready(function(){
        	$(".modal-backdrop").fadeOut(function() {
        	    // Remove element after the fadeOut
        		$(".modal-backdrop").remove()
        	});
        });
    },
    
    // Function to delete a project.
    deleteProject:function () {
        //Delete model
        this.model.destroy();
        //Delete view
        this.remove();
    },
    
    // Produce HTML in template for a project.
	render: function() {
		var tmpl = _.template(this.template);
        this.$el.html(tmpl(this.model.toJSON()));
        return this;
	},
});


// View for collection of Projects
BBProj.Views.ProjectsView = Backbone.View.extend({
    el:$("#projects"),

    initialize:function () {
        this.collection = new BBProj.Collections.Projects();
        // Get project items from web service.
        this.collection.fetch({ update: true });
        
        // Call function to produce HTML 
        this.render();
        this.collection.on("add", this.renderProject, this);
        
        // Connection for external incoming method call to save a new project
        // Allows method calls from outside el("#projects")
        this.bind("newProjectEvent", this.saveNewProject);
        //this.bind("refreshEvent", this.collection.reset);
        
        
        // Begin long polling. Listen for changes within collection and
        // re-render page accordingly.
        this.collection.startLongPolling();
        this.listenTo(this.collection, 'change', this.render);
        this.listenTo(this.collection, 'remove', this.render);  
    },
    
    
	
	
    
    // Produce HTML for list of projects
    render:function () {
    	console.log("Change");
    	this.$el.empty();
        var that = this;
        _.each(this.collection.models, function (item) {
        	// For each project in list call renderProject function.
            that.renderProject(item);
        });
    },
    
    // Produce HTML for an individual project item.
    renderProject:function (item) {
    	BBProj.Views.projectView = new BBProj.Views.ProjectView({
            model:item
        });
        this.$el.append(BBProj.Views.projectView.render().el);
    },
    
    // Function to save a new project.
    saveNewProject: function(){
    	//Get Details entered in form
    	var projectpName = document.getElementById("pNameInput").value;
    	var projectpmName =document.getElementById("pmNameInput").value;
        
    	// Clear Form Ready for re-use
    	document.getElementById("pNameInput").value = "";
    	document.getElementById("pmNameInput").value = localStorage.getItem("g_user_name");
    	
    	// If project name field is blank ignore form submission
    	if (!projectpName || projectpName == "")return;
    	
    	// Save a newly create project and fetch details into list.
    	else {
    		saveNew = this.collection.create({"pName":projectpName, "pmName":projectpmName}, {wait: true});
    	}	
    }
});

// Create an instance of projects view
BBProj.Views.projectsView = new BBProj.Views.ProjectsView();

// A view created in order to receive event from from a button outside of el of projectsView.
BBProj.Views.TFooterView = Backbone.View.extend({
    el: '#pageBody',
    
    // Listens for click on button.
    events: {
        "click .saveNew" : "saveNewProjectLink"
    },
    
    // Bind functions to view.
    initialize: function(){
        _.bindAll(this, "saveNewProjectLink");
    },
    
    // Function which links to trigger on projectsView
    saveNewProjectLink: function() {
    	BBProj.Views.projectsView.trigger("newProjectEvent");
    }
});

// Create an instance of tFooterView
BBProj.Views.tFooterView = new BBProj.Views.TFooterView();
