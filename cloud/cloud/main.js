Parse.Cloud.define("startSession", function(request, response) {
	
  var recipientId = request.params.recipientId;
  
  var pushQuery = new Parse.Query(Parse.Installation);
  pushQuery.equalTo("user", recipientId);


  Parse.Push.send({
    where: pushQuery,
    data: {
      alert: request.params.userId
    }
  }).then(function() {
      response.success("Push was sent successfully.")
  }, function(error) {
      response.error("Push failed to send with error: " + error.message);
  });
});

Parse.Cloud.define("addTag", function(request, response) {
	
  var userId = request.params.userId;
  var tag = request.params.tag;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			var array = results[0].get("tags");
			array.push(tag);	
			
			Parse.Object.saveAll(results, {
				success: function(list) {
					response.success("tag added")
				},
				error: function(error){
					
				}
            })
		},
		error: function(){
			response.error("user lookup failed")
		}
	})	
});

Parse.Cloud.define("removeTag", function(request, response) {
	
  var userId = request.params.userId;
  var tag = request.params.tag;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			var array = results[0].get("tags");
			if(array.length != 0){
				for(var i = 0; i<array.length; i++){
					if(array[i] == tag){
						array.splice(i, 1);
						break;
					}
				}
			}
			
			Parse.Object.saveAll(results, {
				success: function(list) {
					response.success("tag added")
				},
				error: function(error){
					
				}
            })
		},
		error: function(){
			response.error("user lookup failed")
		}
	})	
});

Parse.Cloud.define("removeFriend", function(request, response) {
	
  var userId = request.params.userId;
  var friend = request.params.friend;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			var array = results[0].get("friends");
			if(array.length != 0){
				for(var i = 0; i<array.length; i++){
					if(array[i] == friend){
						array.splice(i, 1);
						break;
					}
				}
			}
			
			Parse.Object.saveAll(results, {
				success: function(list) {
					response.success("friend removed")
				},
				error: function(error){
					
				}
            })
		},
		error: function(){
			response.error("user lookup failed")
		}
	})	
});
