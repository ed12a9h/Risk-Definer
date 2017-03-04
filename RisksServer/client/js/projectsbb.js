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
	parse:function (response) {
        console.log(response);
        //response.id = response._id;
        return response;
    }
});


// Projects Collection
BBProj.Collections.Projects=  Backbone.Collection.extend({
	model: BBProj.Models.Project,
	url: 'http://0.0.0.0:9999/server/request/projects/' // Web Service URL for CRUD operations
});


// View for Single Project
BBProj.Views.ProjectView = Backbone.View.extend({
	tagName: 'tr',
	className: 'pItem',
	template:$("#pTemplate").html(),
	
	events: {
        //'click .edit':   'editProject',
        'click .delete':   'deleteProject',
        'click .saveEdit':   'saveEditProject'
    },
    //editProject: function(){
    	
    	//console.log("edit window");
    	//document.getElementById("editProjectTitle").innerHTML = "Edit " + this.model.get("pName");
        //document.getElementById("pNameInput").value = this.model.get("pName");
        //document.getElementById("pmNameInput").value = this.model.get("pmName");
        
        
        
    //},
    
    saveEditProject: function(){
    	//document.getElementById("pName").contentEditable = true;
        //var newProject = prompt("New project name:", this.model.get("pName")); // prompts for new name
        //if (!newProject)return;  // no change if user hits cancel
        //document.getElementById("pName").style.color = 'blue';
    	console.log("save attempted");
    	var newProjectpName = document.getElementById("pNameInput").value;
    	var newProjectpmName =document.getElementById("pmNameInput").value;
        
    	if (!newProjectpName && !newProjectpmName)return;
        this.model.save({"pName":newProjectpName});
        
        this.render();
        
        //Remove Backdrop from Model. 
        //Required to be called manually due to the above render method.
        $(document).ready(function(){
        	$(".modal-backdrop").fadeOut(function() {
        	    // Remove element after the fadeOut
        		$(".modal-backdrop").remove()
        	});
        	
            
        });
        
        
    },
    deleteProject:function () {
        //Delete model
        this.model.destroy();

        //Delete view
        this.remove();
    },
    
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
        this.collection.fetch({
            error:function () {
                console.log(arguments);
            }
        });
        this.render();

        this.collection.on("add", this.renderProject, this);
        this.collection.on("reset", this.render, this);
    },

    render:function () {
        var that = this;
        _.each(this.collection.models, function (item) {
            that.renderProject(item);
        });
    },

    
    renderProject:function (item) {
        var projectView = new BBProj.Views.ProjectView({
            model:item
        });
        this.$el.append(projectView.render().el);
    }
});

BBProj.Views.projectsView = new BBProj.Views.ProjectsView();
