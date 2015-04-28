1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99
100
101
102
103
104
105
106
107
108
109
110
111
112
113
114
115
116
117
118
119
120
121
122
123
124
125
126
127
128
129
130
131
132
133
134
135
136
137
138
139
140
141
142
143
144
145
146
147
148
149
150
151
152
153
154
155
156
157
158
159
160
161
162
163
164
165
166
167
168
169
170
171
172
173
174
175
176
177
178
179
180
181
182
183
184
185
186
187
188
189
190
191
192
193
194
195
196
197
198
199
200
201
202
203
204
205
206
207
208
209
210
211
212
213
214
215
216
217
218
219
220
221
222
223
224
225
226
227
228
229
230
231
232
233
234
235
236
237
238
239
240
241
242
243
244
245
246
247
248
249
250
251
252
253
254
255
256
257
258
259
260
261
262
263
264
265
266
267
268
269
270
271
272
273
274
275
276
277
278
279
280
281
282
283
284
285
286
287
288
289
290
291
292
293
294
295
296
297
298
299
300
301
302
303
304
305
306
307
308
309
310
311
312
313
314
315
316
317
318
319
320
321
322
323
324
325
326
327
328
329
330
331
332
333
334
335
336
337
338
339
340
341
342
343
344
345
346
347
348
349
350
351
352
353
354
355
356
357
358
359
360
361
362
363
364
365
366
367
368
369
370
371
372
373
374
375
376
377
378
379
380
381
382
383
384
385
386
387
388
389
390
391
392
393
394
395
396
397
398
399
400
401
402
403
404
405
406
407
408
409
410
411
412
413
414
415
416
417
418
419
420
421
422
423
424
425
426
427
428
429
430
431
432
433
434
435
436
437
438
439
440
441
442
443
444
445
446
447
448
449
450
451
452
453
454
455
456
457
458
459
460
461
462
463
464
465
466
467
468
469
470
471
472
473
474
475
476
477
478
479
480
481
482
483
484
485
486
487
488
489
490
491
492
493
494
495
496
497
498
499
500
501
502
503
504
505
506
507
508
509
510
511
512
513
514
515
516
517
518
519
520
521
522
523
524
525
526
527
528
529
530
531
532
533
534
535
536
537
538
539
540
541
542
543
544
545
546
547
548
549
550
551
552
553
554
555
556
557
558
559
560
561
562
563
564
565
566
567
568
569
570
571
572
573
574
575
576
577
578
579
580
581
582
583
584
585
586
587
588
589
590
591
592
593
594
595
596
597
598
599
600
601
602
603
604
605
606
607
608
609
610
611
612
613
614
615
616
617
618
619
620
621
622
623
624
625
626
627
628
629
630
631
632
633
634
635
636
637
638
639
640
641
642
643
644
645
646
647
648
649
650
651
652
653
654
655
656
657
658
659
660
661
662
663
664
665
666
667
668
669
670
671
672
673
674
675
676
677
678
679
680
681
682
683
684
685
686
687
688
689
690
691
692
693
694
695
696
697
698
699
700
701
702
703
704
705
706
707
708
709
710
711
712
713
714
715
716
717
718
719
720
721
722
723
724
725
726
727
728
729
730
731
732
733
734
735
736
737
738
739
740
741
742
743
744
745
746
747
748
749
750
751
752
753
754
755
756
757
758
759
760
761
762
763
764
765
766
767
768
769
770
771
772
773
774
775
776
777
778
779
780
781
782
783
784
785
786
787
788
789
790
791
792
793
794
795
796
797
798
799
800
801
802
803
804
805
806
807
808
809
810
811
812
813
814
815
816
817
818
819
820
821
822
823
824
825
826
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
                                          nameSort = function(a,b){
                                              return a.get("username")>b.get("username");
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
                                          nameSort = function(a,b){
                                              return a.get("username")>b.get("username");
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
                                          nameSort = function(a,b){
                                              return a.get("username")>b.get("username");
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
                                          nameSort = function(a,b){
                                              return a.get("username")>b.get("username");
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
                                          nameSort = function(a,b){
                                              return a.get("username")>b.get("username");
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
                                            nameSort = function(a,b){
                                                return a.get("username")>b.get("username");
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
                                        nameSort = function(a,b){
                                            return a.get("username")>b.get("username");
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
                                                        nameSort = function(a,b){
                                                            return a.get("username")>b.get("username");
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
  var emptyArray = [];
 
  var query = new Parse.Query(Parse.User);
  query.notEqualTo("objectId", userId);
  query.equalTo("isLoggedIn", true);
  //query.limit(10);
 
  query.find({
        success: function(results){
            if(results.length != 0){
                nameSort = function(a,b){
                    return a.get("username")>b.get("username");
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