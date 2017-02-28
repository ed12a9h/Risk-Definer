var Project = Backbone.Model.extend({
	defaults: {
    pName: '',
    pmName: '',
	},
	validate: function(attrs, options){
		if (!attrs.name){
			alert('Your project must have a name!');
        }
		if (attrs.name.length < 2){
			alert('Your project\'s name must have more than one letter!');
		}
	},
});
