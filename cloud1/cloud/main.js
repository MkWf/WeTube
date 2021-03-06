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
 
Parse.Cloud.define("removeFriendPtr", function(request, response) {
     
  var userId = request.params.userId;
  var friend = request.params.friend;
   
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var query2 = new Parse.Query(Parse.User);
            query2.equalTo("objectId", friend);
             
            query2.find({
                success: function(results2){
                    if(array.length > 0){
                        for(var i = 0; i<array.length; i++){
                            if(array[i].id == results2[0].id){
                                array.splice(i,1);
                                 
                                Parse.Object.saveAll(array, {
                                    success: function(list) {
                                        response.success("friend removed");
                                    },
                                    error: function(error){
                                        response.success("friend remove failed");
                                    }
                                })
                                 
                                break;
                            }
                        }
                    }
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
 
Parse.Cloud.define("getFriendsAtoZ", function(request, response) {
     
  var userId = request.params.userId;
  var emptyArray = [];
   
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var nameArray = new Array();
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var name = array[i].get("username");
                    nameArray.push(name);
                     
                    if(nameArray.length == array.length){
                        var sortedArray = nameArray.sort();
                        var friendsList = new Array();
                         
                        for(var j = 0; j<sortedArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                              query2.equalTo("username", sortedArray[j]);
 
                              query2.find({
                                  success: function(results){
                                      friendsList.push(results[0]);
                                       
                                      if(friendsList.length == sortedArray.length){
                                          function nameSort(a, b)
                                          {
                                               var A = a.get("username").toLowerCase();
                                               var B = b.get("username").toLowerCase();
                                               if (A < B){
                                                  return -1;
                                               }else if (A > B){
                                                 return  1;
                                               }else{
                                                 return 0;
                                               }
                                          }
                                           
                                          friendsList = friendsList.sort(nameSort);
                                          response.success(friendsList);
                                      }
                                  },
                                  error: function(){
                                      response.error("user lookup failed")
                                  }
                              })    
                        }
                    }
                }
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getFriendsUnavailable", function(request, response) {
     
  var userId = request.params.userId;
  var emptyArray = [];
  var isEmpty = 1;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var nameArray = new Array();
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var isNotAvailable = array[i].get("isInSession");
                    var isLoggedIn = array[i].get("isLoggedIn");
                    if(isNotAvailable && isLoggedIn){
                        nameArray.push(array[i].get("username"));
                        isEmpty = 0;
                    }
                     
                    if(i == array.length-1 && isEmpty){
                        response.success(emptyArray);
                    }
                     
                    if(i == array.length-1){
                        var sortedArray = nameArray.sort();
                        var friendsList = new Array();
                         
                        for(var j = 0; j<sortedArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                              query2.equalTo("username", sortedArray[j]);
 
                              query2.find({
                                  success: function(results){
                                      friendsList.push(results[0]);
                                       
                                      if(friendsList.length == sortedArray.length){
                                          function nameSort(a, b)
                                          {
                                               var A = a.get("username").toLowerCase();
                                               var B = b.get("username").toLowerCase();
                                               if (A < B){
                                                  return -1;
                                               }else if (A > B){
                                                 return  1;
                                               }else{
                                                 return 0;
                                               }
                                          }
                                           
                                          friendsList = friendsList.sort(nameSort);
                                          response.success(friendsList);
                                      }
                                  },
                                  error: function(){
                                      response.error("user lookup failed")
                                  }
                              })    
                        }
                    }
                }
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getFriendsAvailable", function(request, response) {
     
  var userId = request.params.userId;
  var emptyArray = [];
  var isEmpty = 1;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var nameArray = new Array();
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var isAvailable = array[i].get("isInSession");
                    var isLoggedIn = array[i].get("isLoggedIn");
                    if(!isAvailable && isLoggedIn){
                        nameArray.push(array[i].get("username"));
                        isEmpty = 0;
                    }
                     
                    if(i == array.length-1 && isEmpty){
                        response.success(emptyArray);
                    }
                     
                    if(i == array.length-1 && nameArray.length != 0){
                        var sortedArray = nameArray.sort();
                        var friendsList = new Array();
                         
                        for(var j = 0; j<sortedArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                              query2.equalTo("username", sortedArray[j]);
 
                              query2.find({
                                  success: function(results){
                                      friendsList.push(results[0]);
                                       
                                      if(friendsList.length == sortedArray.length){
                                          function nameSort(a, b)
                                          {
                                               var A = a.get("username").toLowerCase();
                                               var B = b.get("username").toLowerCase();
                                               if (A < B){
                                                  return -1;
                                               }else if (A > B){
                                                 return  1;
                                               }else{
                                                 return 0;
                                               }
                                          }
                                           
                                          friendsList = friendsList.sort(nameSort);
                                          response.success(friendsList);
                                      }
                                  },
                                  error: function(){
                                      response.error("user lookup failed")
                                  }
                              })    
                        }
                    }
                }
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getFriendsOffline", function(request, response) {
     
  var userId = request.params.userId;
  var emptyArray = [];
  var isEmpty = 1;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var nameArray = new Array();
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var isLoggedIn = array[i].get("isLoggedIn");
                    if(!isLoggedIn){
                        nameArray.push(array[i].get("username"));
                        isEmpty = 0;
                    }
                     
                    if(i == array.length-1 && isEmpty){
                        response.success(emptyArray);
                    }
                     
                    if(i == array.length-1 && nameArray.length != 0){
                        var sortedArray = nameArray.sort();
                        var friendsList = new Array();
                         
                        for(var j = 0; j<sortedArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                              query2.equalTo("username", sortedArray[j]);
 
                              query2.find({
                                  success: function(results){
                                      friendsList.push(results[0]);
                                       
                                      if(friendsList.length == sortedArray.length){
                                          function nameSort(a, b)
                                          {
                                               var A = a.get("username").toLowerCase();
                                               var B = b.get("username").toLowerCase();
                                               if (A < B){
                                                  return -1;
                                               }else if (A > B){
                                                 return  1;
                                               }else{
                                                 return 0;
                                               }
                                          }
                                           
                                          friendsList = friendsList.sort(nameSort);
                                          response.success(friendsList);
                                      }
                                  },
                                  error: function(){
                                      response.error("user lookup failed")
                                  }
                              })    
                        }
                    }
                }
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getFriendsOnline", function(request, response) {
     
  var userId = request.params.userId;
  var emptyArray = [];
  var isEmpty = 1;
  
  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", userId);
  query.include("friends");
 
  query.find({
        success: function(results){
            var array = results[0].get("friends");
            var nameArray = new Array();
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var isLoggedIn = array[i].get("isLoggedIn");
                    if(isLoggedIn){
                        nameArray.push(array[i].get("username"));
                        isEmpty = 0;
                    }
                     
                    if(i == array.length-1 && isEmpty){
                        response.success(emptyArray);
                    }
                     
                    if(i == array.length-1 && nameArray.length != 0){
                        var sortedArray = nameArray.sort();
                        var friendsList = new Array();
                         
                        for(var j = 0; j<sortedArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                              query2.equalTo("username", sortedArray[j]);
 
                              query2.find({
                                  success: function(results){
                                      friendsList.push(results[0]);
                                       
                                      if(friendsList.length == sortedArray.length){
                                          function nameSort(a, b)
                                          {
                                               var A = a.get("username").toLowerCase();
                                               var B = b.get("username").toLowerCase();
                                               if (A < B){
                                                  return -1;
                                               }else if (A > B){
                                                 return  1;
                                               }else{
                                                 return 0;
                                               }
                                          }
                                           
                                          friendsList = friendsList.sort(nameSort);
                                          response.success(friendsList);
                                      }
                                  },
                                  error: function(){
                                      response.error("user lookup failed")
                                  }
                              })    
                        }
                    }
                }
            }else{
                var emptyArray = [];
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getFriendsDefault", function(request, response) {
     
    var userId = request.params.userId;
    var emptyArray = [];
     
    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);
    query.include("friends");
 
    query.find({
        success: function(results){
            var array = results[0].get("friends");
            var onlineAvailableArray = new Array();
            var onlineUnavailableArray = new Array();
            var offlineArray = new Array();
            var tempArray = new Array();
            var friendsList = new Array();
             
            if(array.length != 0){
                for(var i = 0; i<array.length; i++){
                    var isLoggedIn = array[i].get("isLoggedIn");
                    var isInSession = array[i].get("isInSession");
                    if(isLoggedIn && !isInSession){
                        onlineAvailableArray.push(array[i].get("username"));
                    }else if(isLoggedIn && isInSession){
                        onlineUnavailableArray.push(array[i].get("username"));
                    }else if(!isLoggedIn){
                        offlineArray.push(array[i].get("username"));
                    }
                     
                    if(i == array.length-1){    
                        if(onlineAvailableArray.length != 0){                       
                            for(var j = 0; j<onlineAvailableArray.length; j++){
                                var query2 = new Parse.Query(Parse.User);
                                query2.equalTo("username", onlineAvailableArray[j]);
 
                                query2.find({
                                    success: function(results){
                                        tempArray.push(results[0]);
                                          
                                        if(tempArray.length == onlineAvailableArray.length){
                                            function nameSort(a, b)
                                            {
                                                 var A = a.get("username").toLowerCase();
                                                 var B = b.get("username").toLowerCase();
                                                 if (A < B){
                                                    return -1;
                                                 }else if (A > B){
                                                   return  1;
                                                 }else{
                                                   return 0;
                                                 }
                                            }
                                               
                                            tempArray = tempArray.sort(nameSort);
                                            friendsList = tempArray;
                                            tempArray = []; 
                                        }
                                    }
                                })
                            }
                        }
                        for(var j = 0; j<onlineAvailableArray.length; j++){
                            var query2 = new Parse.Query(Parse.User);
                            query2.equalTo("username", onlineAvailableArray[j]);
 
                            query2.find({
                                success: function(results){
                                    tempArray.push(results[0]);
                                      
                                    if(tempArray.length == onlineAvailableArray.length){
                                        function nameSort(a, b)
                                        {
                                             var A = a.get("username").toLowerCase();
                                             var B = b.get("username").toLowerCase();
                                             if (A < B){
                                                return -1;
                                             }else if (A > B){
                                               return  1;
                                             }else{
                                               return 0;
                                             }
                                        }
                                           
                                        tempArray = tempArray.sort(nameSort);
                                        friendsList = tempArray;
                                        tempArray = [];
                                         
                                        for(var k = 0; k<onlineUnavailableArray.length; k++){
                                            var query3 = new Parse.Query(Parse.User);
                                            query3.equalTo("username", onlineUnavailableArray[k]);
                                             
                                            query3.find({
                                                success: function(results){
                                                     
                                                    tempArray.push(results[0]);
                                                     
                                                    if(tempArray.length == onlineUnavailableArray.length){
                                                        function nameSort(a, b)
                                                          {
                                                               var A = a.get("username").toLowerCase();
                                                               var B = b.get("username").toLowerCase();
                                                               if (A < B){
                                                                  return -1;
                                                               }else if (A > B){
                                                                 return  1;
                                                               }else{
                                                                 return 0;
                                                               }
                                                          }
                                                         
                                                        tempArray = tempArray.sort(nameSort);
                                                        friendsList = friendsList.concat(tempArray);
                                                        tempArray = [];
                                                         
                                                        for(var l = 0; l<offlineArray.length; l++){
                                                            var query4 = new Parse.Query(Parse.User);
                                                            query4.equalTo("username", offlineArray[l]);
                                                         
                                                            query4.find({
                                                                success: function(results){
                                                                    tempArray.push(results[0]);
                                                                     
                                                                    if(tempArray.length == offlineArray.length){
                                                                        nameSort = function(a,b){
                                                                            return a.get("username")>b.get("username");
                                                                        }
                                                                         
                                                                        tempArray = tempArray.sort(nameSort);
                                                                        friendsList = friendsList.concat(tempArray);
                                                                        response.success(friendsList);
                                                                    }
                                                                }
                                                            })
                                                        }
                                                    }
                                                }
                                            })
                                        }                                       
                                    }
                                }
                            })  
                        }
                    }
                }
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});
 
Parse.Cloud.define("getLoggedInUsers", function(request, response) {
     
  var userId = request.params.userId;
  var descending = request.params.descending;
  var ascending = request.params.ascending;
  var emptyArray = [];
 
  var query = new Parse.Query(Parse.User);
  query.notEqualTo("objectId", userId);
  query.equalTo("isLoggedIn", true);

  if(ascending != null){
    query.ascending(ascending);
  }else{
    query.descending(descending);
  }
  query.limit(100);
  
  query.find({
        success: function(results){
            if(results.length != 0){
                function nameSort(a, b)
                {
                     var A = a.get("username").toLowerCase();
                     var B = b.get("username").toLowerCase();
                     if (A < B){
                        return -1;
                     }else if (A > B){
                       return  1;
                     }else{
                       return 0;
                     }
                }
                 
                var users = results.sort(nameSort);
                response.success(users);
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});

Parse.Cloud.define("getMoreUsers", function(request, response) {
     
  var userId = request.params.userId;
  var skip = request.params.skip;
  var limit = request.params.limit;
  var emptyArray = [];
 
  var query = new Parse.Query(Parse.User);
  query.notEqualTo("objectId", userId);
  query.equalTo("isLoggedIn", true);
  query.skip(skip);
  query.limit(limit);
  
  query.find({
        success: function(results){
            if(results.length != 0){
                function nameSort(a, b)
                    {
                         var A = a.get("username").toLowerCase();
                         var B = b.get("username").toLowerCase();
                         if (A < B){
                            return -1;
                         }else if (A > B){
                           return  1;
                         }else{
                           return 0;
                         }
                    }
                 
                var users = results.sort(nameSort);
                response.success(users);
            }else{
                response.success(emptyArray);
            }
        },
        error: function(){
            response.error("user lookup failed")
        }
    })  
});

Parse.Cloud.define("getFriendsAvailableTwo", function(request, response) {
     
    var userId = request.params.userId;
    var emptyArray = [];
    //var isEmpty = 1;

    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);

    query.find({
        success: function(users){  
           var user = users[0];    //passing myself
      
            var q1 = new Parse.Query("Friend");
            q1.equalTo("friend1", user);

            var q2 = new Parse.Query("Friend");
            q2.equalTo("friend2", user);

            var mainQuery = Parse.Query.or(q1, q2);
            mainQuery.include("friend1");
            mainQuery.include("friend2");

            var User = Parse.Object.extend("User"); 
            var innerQuery = new Parse.Query(User);
            innerQuery.equalTo("isInSession", false);
            innerQuery.equalTo("isLoggedIn", true);

            mainQuery.matchesQuery("friend1", innerQuery);
            mainQuery.matchesQuery("friend2", innerQuery);

            mainQuery.find({         
                success: function(friends){
                    var friendPtrs = [];   
                    var availFriends = [];
                    for(var i = 0; i<friends.length; i++){
                        if(friends[i].get("friend1").id == user.id){  
                            friendPtrs.push(friends[i].get("friend2"));
                        }else{
                            friendPtrs.push(friends[i].get("friend1"));
                        }
                    }

                    function nameSort(a, b)
                    {
                         var A = a.get("username").toLowerCase();
                         var B = b.get("username").toLowerCase();
                         if (A < B){
                            return -1;
                         }else if (A > B){
                           return  1;
                         }else{
                           return 0;
                         }
                    }
                     
                  friendPtrs = friendPtrs.sort(nameSort);
                  response.success(friendPtrs);
           
                }, 
                error: function(err){
                  response.error(err);
                }
            })
        
        }
    })
});

Parse.Cloud.define("getFriendsUnavailableTwo", function(request, response) {
     
    var userId = request.params.userId;
    var emptyArray = [];
    //var isEmpty = 1;

    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);

    query.find({
        success: function(users){  
           var user = users[0];    //passing myself
      
            var q1 = new Parse.Query("Friend");
            q1.equalTo("friend1", user);

            var q2 = new Parse.Query("Friend");
            q2.equalTo("friend2", user);

            var mainQuery = Parse.Query.or(q1, q2);
            mainQuery.include("friend1");
            mainQuery.include("friend2");

            user.set("isInSession", true);
            user.save(null, {
              success: function(gameScore) {
                  var User = Parse.Object.extend("User"); 
                  var innerQuery = new Parse.Query(User);
                  innerQuery.equalTo("isInSession", true);
                  innerQuery.equalTo("isLoggedIn", true);

                  mainQuery.matchesQuery("friend1", innerQuery);
                  mainQuery.matchesQuery("friend2", innerQuery);

                  mainQuery.find({         
                      success: function(friends){
                          user.set("isInSession", false);
                          user.save();
                          var friendPtrs = [];   
                          var availFriends = [];
                          for(var i = 0; i<friends.length; i++){
                              if(friends[i].get("friend1").id == user.id){  
                                  friendPtrs.push(friends[i].get("friend2"));
                              }else{
                                  friendPtrs.push(friends[i].get("friend1"));
                              }
                          }

                   function nameSort(a, b)
                    {
                         var A = a.get("username").toLowerCase();
                         var B = b.get("username").toLowerCase();
                         if (A < B){
                            return -1;
                         }else if (A > B){
                           return  1;
                         }else{
                           return 0;
                         }
                    }
                           
                        friendPtrs = friendPtrs.sort(nameSort);

                        response.success(friendPtrs);
                 
                      }, 
                      error: function(err){
                        response.error(err);
                      }
                  })
              }
            });     
        }
    })
});


Parse.Cloud.define("getFriendsOfflineTwo", function(request, response) {
     
    var userId = request.params.userId;
    var emptyArray = [];
    //var isEmpty = 1;

    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);

    query.find({
        success: function(users){  
           var user = users[0];    //passing myself
      
            var q1 = new Parse.Query("Friend");
            q1.equalTo("friend1", user);

            var q2 = new Parse.Query("Friend");
            q2.equalTo("friend2", user);

            var mainQuery = Parse.Query.or(q1, q2);
            mainQuery.include("friend1");
            mainQuery.include("friend2");


            user.set("isLoggedIn", false);
            user.save(null, {
              success: function(gameScore) {
                  var User = Parse.Object.extend("User"); 
                  var innerQuery = new Parse.Query(User);
                  //innerQuery.equalTo("isInSession", false);
                  innerQuery.equalTo("isLoggedIn", false);

                  mainQuery.matchesQuery("friend1", innerQuery);
                  mainQuery.matchesQuery("friend2", innerQuery);

                  mainQuery.find({         
                      success: function(friends){
                          user.set("isLoggedIn", true);
                          user.save();
                          var friendPtrs = [];   
                          var availFriends = [];
                          for(var i = 0; i<friends.length; i++){
                              if(friends[i].get("friend1").id == user.id){  
                                  friendPtrs.push(friends[i].get("friend2"));
                              }else{
                                  friendPtrs.push(friends[i].get("friend1"));
                              }
                          }

                          function nameSort(a, b)
                          {
                               var A = a.get("username").toLowerCase();
                               var B = b.get("username").toLowerCase();
                               if (A < B){
                                  return -1;
                               }else if (A > B){
                                 return  1;
                               }else{
                                 return 0;
                               }
                          }
                           
                        friendPtrs = friendPtrs.sort(nameSort);

                        response.success(friendPtrs);
                 
                      }, 
                      error: function(err){
                        response.error(err);
                      }
                  })
              }
            });     
        }
    })
});

Parse.Cloud.define("getFriendsAtoZTwo", function(request, response) {
     
    var userId = request.params.userId;
    var emptyArray = [];
    //var isEmpty = 1;

    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);

    query.find({
        success: function(users){  
           var user = users[0];   
      
            var q1 = new Parse.Query("Friend");
            q1.equalTo("friend1", user);

            var q2 = new Parse.Query("Friend");
            q2.equalTo("friend2", user);

            var mainQuery = Parse.Query.or(q1, q2);
            mainQuery.include("friend1");
            mainQuery.include("friend2");
            mainQuery.include("User");
            mainQuery.ascending("username");

            mainQuery.find({         
                success: function(friends){
                    var friendPtrs = [];   
                    var availFriends = [];
                    for(var i = 0; i<friends.length; i++){
                        if(friends[i].get("friend1").id == user.id){  
                            friendPtrs.push(friends[i].get("friend2"));
                        }else{
                            friendPtrs.push(friends[i].get("friend1"));
                        }
                    }

                   function nameSort(a, b)
                    {
                         var A = a.get("username").toLowerCase();
                         var B = b.get("username").toLowerCase();
                         if (A < B){
                            return -1;
                         }else if (A > B){
                           return  1;
                         }else{
                           return 0;
                         }
                    }
                     
                  friendPtrs = friendPtrs.sort(nameSort);
                  response.success(friendPtrs);
           
                }, 
                error: function(err){
                  response.error(err);
                }
            })
        
        }
    })
});

Parse.Cloud.define("updateLastSeen", function(request, response) {
     
    var userId = request.params.userId;
    var ms = request.params.ms;

    var query = new Parse.Query(Parse.User);
    query.equalTo("objectId", userId);

    query.find({
        success: function(users){  
          var user = users[0];   

          user.set("lastSeen", ms);
          user.save(null, {
              success: function(save) {
                response.success("updated");
              }
          });           
        }
    })
});