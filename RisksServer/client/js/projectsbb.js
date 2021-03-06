/**
 * Risk Definer Web Application
 * Produced by Adam Hustwit
 * 
 * This file contains code used for creating a backbone MVC and syncing project data with the Risk Definer Web service.
 */

// IE10 Cache Fix
$.ajaxSetup({ cache: false });

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
        var projectUsers = getEmails(this.model.get("id"));
        
        //Update project with new details.
        var thisObj= this;
        this.model.save({"pName":newProjectpName, "pmName":newProjectpmName, "users":projectUsers}, {
            error: function(model, response) {
                // Get Error response and pass to error function.
                errorNewProject(response);
            },
            success: function(model, response) {
                errorHideAll();
                thisObj.render();
                
                //Remove Model
                $(".modal-header .close").click();
                
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
    deleteProject:function () {
        // User prompted to confirm delete.
        if (checkDelete()===true) {
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
        }
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
        this.collection.fetch({
            error: function(model,collecton, options) {
                // Call fetch fail function.
                errorFetchFail(options.xhr.status);
            },
            wait: true // Do not report status until web service response.
        });
        
        // Call function to produce HTML 
        this.render();
        this.collection.on("add", this.renderProject, this);
        
        // Connection for external incoming method call to save a new project
        // Allows method calls from outside el("#projects")
        this.bind("newProjectEvent", this.saveNewProject);
        
        // Begin long polling. Listen for changes within collection and
        // re-render page accordingly.
        this.collection.startLongPolling();
        this.listenTo(this.collection, 'change', this.render);
        this.listenTo(this.collection, 'remove', this.render);  
    },
    
    // Produce HTML for list of projects
    render:function () {
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
        var projectUsers = getEmails("new");
        
        // Save a newly create project and a details into list.
        saveNew = this.collection.create({"pName":projectpName, "pmName":projectpmName, "users":projectUsers}, {
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
BBProj.Views.projectsView = new BBProj.Views.ProjectsView();


// A view created in order to receive event from from a button outside of el of projectsView. Code Reference #11.
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
