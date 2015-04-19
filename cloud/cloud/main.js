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

Parse.Cloud.define("commonTags", function(request, response) {
	
  var userId = request.params.userId;
  var clickedId = request.params.clickedId;
  var userArray;
  var clickedArray;
  var commonArray = [];
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			userArray = results[0].get("tags");
			
			var query2 = new Parse.Query(Parse.User);
		    query2.equalTo("objectId", clickedId);
		   
		    query2.find({
				success: function(results){
					clickedArray = results[0].get("tags");

					for(var i = 0; i<clickedArray.length; i++){
						for(var j = 0; j<userArray.length; j++){
							if(userArray[j] == clickedArray[i]){
								commonArray.push(userArray[j]);
							}
						}
					}
					response.success(commonArray);					
				},
				error: function(){
				}
			})
		},
		error: function(){
		}
	})	
});

Parse.Cloud.define("uncommonTags", function(request, response) {
	
  function diffArray(a, b) {
	  var seen = [], diff = [];
	  for ( var i = 0; i < b.length; i++)
		  seen[b[i]] = true;
	  for ( var i = 0; i < a.length; i++)
		  if (!seen[a[i]])
			  diff.push(a[i]);
	  return diff;
	}
	
  var userId = request.params.userId;
  var clickedId = request.params.clickedId;
  var userArray;
  var clickedArray;
  var uncommonArray = [];
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			userArray = results[0].get("tags");
			
			var query2 = new Parse.Query(Parse.User);
		    query2.equalTo("objectId", clickedId);
		   
		    query2.find({
				success: function(results){
					clickedArray = results[0].get("tags");
				
					response.success(diffArray(clickedArray, userArray));					
				},
				error: function(){
				}
			})
		},
		error: function(){
		}
	})	
});

Parse.Cloud.define("isBlocked", function(request, response) {
	
  var userId = request.params.userId;
  var clickedId = request.params.clickedId;
  var userArray;

  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			userArray = results[0].get("blockedUsers");
			
			var done = 0;
			
			for(var i = 0; i<userArray.length; i++){
				if(userArray[i] == clickedId){
					response.success("You blocked this user");
					done = done + 1;
				}
			}
			
			if(done == 0){
				userArray = results[0].get("blockedBy");
				for(var j = 0; j<userArray.length; j++){
					if(userArray[j] == clickedId){
						response.success("You are blocked by this user");
						done = done + 1;
					}
				}
			}
			
			if(done == 0){
				response.success("No block");
			}
		},
		error: function(){
		}
	})	
});

Parse.Cloud.define("unblock", function(request, response) {
	
  var userId = request.params.userId;
  var clickedId = request.params.clickedId;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			var array = results[0].get("blockedUsers");
			if(array.length != 0){
				for(var i = 0; i<array.length; i++){
					if(array[i] == clickedId){
						array.splice(i, 1);
						break;
					}
				}
			}
			
			Parse.Object.saveAll(results, {
				success: function(list) {
					response.success("user unblocked")
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

Parse.Cloud.define("unblockedBy", function(request, response) {
	
  var userId = request.params.userId;
  var clickedId = request.params.clickedId;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);

  query.find({
		success: function(results){
			var array = results[0].get("blockedBy");
			if(array.length != 0){
				for(var i = 0; i<array.length; i++){
					if(array[i] == clickedId){
						array.splice(i, 1);
						break;
					}
				}
			}
			
			Parse.Object.saveAll(results, {
				success: function(list) {
					response.success("user unblocked")
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

