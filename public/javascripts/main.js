var fortravelers = {
    
    init : function() {
    	
    	// CUSTOM TOOLTIP
    	jQuery(document).tooltip({ position: { my: "left+30 center", at: "right center" } });
    	
    	jQuery("#ontology-format").change(function() {
    		$("#ontology-download").removeAttr("href");
    		$("#ontology-download").prop('href','/ontologyDL/'+$("#ontology-format option:selected").val());
    	});
    	
    	$.pnotify.defaults.history = false;
    	
    	// CHECK FOR AUTHENTICATION
		if($('#authentication').val() == 0) {
			authentication();
		}
    	
    	// EASY MODAL OVERLAY
    	function easyModal(){
	    	jQuery('.easy-modal').easyModal({
				top: 80,
				autoOpen: true,
				overlayOpacity: 0.3,
				overlayColor: "#333",
				overlayClose: true,
				closeOnEscape: true,
				onClose: function(){
					$(".easy-modal").remove();
				}
			});
		}
		
    	// SIGN IN VALIDATION
    	jQuery('#login-email').attr('required','required').attr('pattern','.+@.+');
    	jQuery('#login-password').attr('required','required').attr('pattern','.{6,}');
    	
    	$('#form-login').submit(function(event) {
    		if($('#login-email').val()!="" && $('#login-password').val()!="")
    		{
				$.ajax({
					type : 'POST',
			        url : '/login',
			        data: { email: $('#login-email').val(), password: $('#login-password').val() },
			        dataType: "json",
			        success : function(data) {
			        	if(data.error == "1") {
			        		$('#form-login').append("<div class=\"easy-modal\">Ooupss, correct your email or password field.</div>");
			        		easyModal(); 
			        	}
			        	else {
				        	var html = "<nav id=\"profile\" class='main-nav'><ul><li class='active'><a href='#'>Settings</a><ul class='dropdown-menu'><li><a href=\"/user/"+data.username+"\">" + data.email + "</a></li><li><a href=\"/logout\">Log Out</a></li></ul></li></ul></nav>"; 
				        	$(".header-buttons").after(html);
				        	$(".header-buttons").remove();
				        	authentication();
				        	$.pnotify({
			            		title: 'Success',
			            		text: 'You\'re now connected with us'
			            	});
			        	}
			        },
			        error : function(data) { 
			        	$('#form-login').append("<div class=\"easy-modal\">Ooups, the server has refuse your request. Maybe you should try later.</div>");
			        	easyModal(); 
			        }
				});
				return false;
			}
			
			$('#form-login').append("<div class=\"easy-modal\">Ooupss, correct your email or password field.</div>") 
			easyModal();			
			
			event.preventDefault();
		});
			
    	// SIGN UP VALIDATION
    	jQuery('#register-name').attr('required','required').attr('pattern','[a-zA-Z]{3,}');
    	jQuery('#register-name').keyup(function(){ userNameValidator(); });
		function userNameValidator() {
			var reg = /[\s]+/;
			if(reg.test($('#register-name').val())) {
				$('#register-name').css('border-color','rgb(233, 50, 45)');
				$('#register-name').css('background-color','rgb(255, 244, 241)');
				$('#register-name').css('color','rgb(246, 100, 66)'); }
			else { $('#register-name').removeAttr('style'); $('#register-name').removeAttr('title');} 
		}    	
    	jQuery('#register-email').attr('required','required').attr('pattern','.+@.+');
    	jQuery('#register-city').attr('required','required');
    	jQuery('#register-password').attr('required','required').attr('pattern','.{6,}');
    	jQuery('#register-password-confirm').attr('required','required').attr('pattern','.{6,}');
    	jQuery('#register-password').keyup(function(){ passwordsValidator(); });
    	jQuery('#register-password-confirm').keyup(function(){ passwordsValidator(); }); 
		function passwordsValidator() {
			if($('#register-password').val() != $('#register-password-confirm').val()) {
				$('#register-password-confirm').css('border-color','rgb(233, 50, 45)');
				$('#register-password-confirm').css('background-color','rgb(255, 244, 241)');
				$('#register-password-confirm').css('color','rgb(246, 100, 66)');						
			}
			else { $('#register-password-confirm').removeAttr('style'); }
		}
		    	
		$('#form-register').submit(function(event) {
			if( $('#register-name').val() !="" && $('#register-email').val() !="" && $('#register-city').val()!="" &&
			    $('#register-password').val()!= "" && $('#register-password-confirm').val()!="") {
				$.ajax({
					type : 'POST',
			        url : '/register',
			        data: {
			        	  username: $('#register-name').val(), 
			        	  email: $('#register-email').val(), 
			        	  city: $('#destination-city').val(), 
			        	  password: $('#register-password').val(), 
			        	  passwordConfirm: $('#register-password-confirm').val() 
			       	},
			       	dataType: "json",
			        success : function(data) { 
			        	if(data.error == "1") {
			        		$('#form-register').append("<div class=\"easy-modal\">Please be sure you have all the filds properly filled and try again.</div>");
			        		easyModal(); 
						}			        		
			        	else if(data.error == "2") {
			        		$('#register-name').attr('title','Username already exists. Type a new one and try again.');
							$('#register-name').focus();
							$('#register-name').css('color','rgb(246, 100, 66)');
						}			        		
			        	else if (data.error == "3") {
			        		$('#register-email').attr('title','Email already exists. Type a new one and try again.');
							$('#register-email').focus();
							$('#register-email').css('color','rgb(246, 100, 66)');
			        	}
			        	else if (data.error == "0") {
			        		var html = "<nav id=\"profile\" class='main-nav'><ul><li class='active'><a href='#'>Settings</a><ul class='dropdown-menu'><li><a href=\"/user/"+data.username+"\">" + data.email + "</a></li><li><a href=\"/logout\">Log Out</a></li></ul></li></ul></nav>"; 
				        	$(".header-buttons").after(html);
				        	$(".header-buttons").remove();
				        	authentication();	
				        	$.pnotify({
			            		title: 'Success',
			            		text: 'You\'re now connected with us'
			            	});
			        	} 
			        },
			        error : function(data) { 
			        	$('#form-register').append("<div class=\"easy-modal\">Ooups, the server has refuse your request.</div>");
			        	easyModal(); 
			        }
				});
				return false;
			}
			$('#form-register').append("<div class=\"easy-modal\">Please be sure you have all the filds properly filled and try again.</div>");
			easyModal();
			        					
			event.preventDefault();
		});
		
    	/*
    	 * Updates how many times a user traveled to a destination and unlocks rating and commenting
    	 */
    	jQuery(document).on("click", "#user-traveled", function () {
		    $.ajax({
		        type: "POST",
		        url: "/submitTimesTraveled/",
		        data: "{ \"cityname\": \"" + $('#city').val() + "\", \"timestraveled\": \"" + $('#times-traveled').val() + "\" }",
		        contentType: "application/json; charset=utf-8",
		        dataType: "json",
		        success: function (msg) {
	                $('#cannotvote').attr('id', 'canvote');
	                if(!$(".write-comment").length) {
	                	$('.userCanComment').append("<a href=\"#comment\" class=\"write-comment\">Write a review</a>")
	                }
	                $("#user-comment").removeAttr("disabled");
	                $("#user-comment").removeAttr("title");
	                $("#comment-text").removeAttr("disabled");
	                $("#comment-text").removeAttr("title");
	                $.pnotify({
	            		title: 'Success',
	            		text: 'Thank you for your feedback. Now you can rate or write a review'
	            	});
		        },
		        error: function (msg) {
		            alert(msg.d);
		        }
		    });
		});
    	
    	/*
    	 * Update rate by user
    	 */
    	var instance = 0;
    	jQuery(document).on("click", "#votestar1, #votestar2, #votestar3, #votestar4, #votestar5", function () {
    		var rating = $(this).attr('data-value');
    		if(instance == 0) {
    			instance = 1;
    			if($("#canvote").length) {
				    $.ajax({
				        type: "POST",
				        url: "/submitRating/",
				        data: "{ \"cityname\": \"" + $('#city').val() + "\", \"rating\": \"" + rating + "\" }",
				        contentType: "application/json; charset=utf-8",
				        dataType: "json",
				        success: function (msg) {
				        	console.log(msg.rating);
				        	$(".rating").css("width", msg.rating);
				        	if($(".notratedyet").length) {
				        		$(".notratedyet").text("1 vote");
				        	}
				        	if($(".nbvotes").length) {
				        		$(".nbvotes").text(msg.numberOfVotes + " votes");
				        	}
				        	$.pnotify({
			            		title: 'Success',
			            		text: 'Thank you for voting.'
			            	});
				        },
				        error: function (msg) {
				            alert(msg.d);
				        }
				    });
    			}
        		else {
        			alert('You can vote or comment only if you have been to this place.');
        		}
    		}
		});
    	
    	/*
    	 * Submit review
    	 */
    	jQuery(document).on("click", "#user-comment", function () {
    		if($('#comment-text').val()=="")
				$('#comment-text').focus();
			else
			{
	    		var review = $('#comment-text').val();
	    		if (review == '') {
	    			if(!$(".comment-missing").length) {
	    				$('#comment').append("<div id=\"error-message comment-missing\">Please enter a text</div>");
	    			}
	    		}
	    		else {
				    $.ajax({
				        type: "POST",
				        url: "/submitReview/",
				        data: "{ \"cityname\": \"" + $('#city').val() + "\", \"review\": \"" + review + "\" }",
				        contentType: "application/json; charset=utf-8",
				        dataType: "json",
				        success: function (msg) {
				        	var reviewHtml = "<div class=\"review\"><em>" + msg.nick + " on " + msg.date + "</em><p>" + review + "</p></div>";
			                $('a.comments-text').after(reviewHtml);
			                $("#comment-text").val("");
			                if($(".no-reviews").length) {
			                	$(".no-reviews").remove();
			                }
			                $.pnotify({
			            		title: 'Success',
			            		text: 'Thank you for your review.'
			            	});
				        },
				        error: function (msg) {
				            alert(msg.d);
				        }
				    });
	    		}
	    	}
		});

		function authentication()
		{
			var $this = $('.header-buttons .profile');
            if($('#hidden-header').hasClass('open')) {
                $this.removeClass('closed');
                $this.attr('data-original-title', 'Log In / Sign Out');
                $('#hidden-header .profile-form').hide();
                $('#main').css('float','');
                fortravelers.closeHiddenHeader();
                $("#body").css('height', '');
            } 
            else {
				$this.addClass('closed');
                $this.attr('data-original-title', 'Close');
                $('#hidden-header .profile-form').show();
                $('#main').css('float','left');
                fortravelers.openHiddenHeader();
                var heightToAdd = $("#main").height() + 500;
                $("#body").css('height', heightToAdd + 'px');
            }			
		}
		
		jQuery('.header-buttons .profile').click(function() {
            authentication();
        });
        		
		jQuery('#email-login').click(function(){
			$('#hidden-header').css('display','');
		});
		
		jQuery('#search-string').attr('required','required');
		
		jQuery("#form-search").submit(function(event) {
			if($('#search-string').val() == "") {
				return false;
			}
			return true;
			event.preventDefault();
		});
		
		jQuery("#search-string" ).keyup(function() {
			$("#destination-city").val($('#search-string').val());
		});
		
		var parentID = "";
		jQuery("#search-string, #register-city").autocomplete({
			 source: function( request, response ) {
			 $.ajax({
				 beforeSend: function() {
						$( "#search-string" ).keyup(function() {
							$('#search-string').last().addClass( "loading-main" );
						});
						$("#register-city").keyup(function() {
							$('#register-city').last().addClass( "loading-register " );
						});
				 },
				 type: "POST",
				 url: "/cityInformationByQuery/",
				 data: "{ \"cityname\": \"" + request.term + "\" }",
			 	 contentType: "application/json; charset=utf-8",
				 dataType: "json",
				 success: function( data ) {
					 if (data == 0) {
						 $('#search-string').removeClass('loading-main');
						 $('#register-city').removeClass('loading-register');
					 }
					 else {
						 response( $.map( data.cities, function( item ) {
							 return {
								 label: item.name,
								 value: item.name,
								 hiddenValue: item.value
							 }
						 },
						 $('#search-string').removeClass('loading-main'),
						 $('#register-city').removeClass('loading-register')
						 ));
					 }
				 },
				 error: function(data){
				 	//alert("Sorry not cities were founds. Maybe due to some unavailable service.");
					 $('#search-string').last().addClass( "loading-main" );
				 }
			 	});
			 },
			 minLength: 2,
			 select: function( event, ui ) {
				 $("#destination").val(ui.item.name);
				 $("#destination-city").val(ui.item.hiddenValue);
			 }
		 });
        
		jQuery('.location-finder .button-slider').click(fortravelers.toggleLocationFinder);
        
        jQuery('.custom-multiple-select').mCustomScrollbar({
            theme : "dark-thick"
        });
        
        jQuery('.location-finder .left-side').mCustomScrollbar({
            theme : "dark-thick"
        });
                
		jQuery('.selectpicker').selectpicker();              
        
        jQuery('.testimonial-box .next').click(function(){
            $('.testimonial-box').flexslider("next");
            return false;
        });
        
        jQuery('.testimonial-box .prev').click(function(){
            $('.testimonial-box').flexslider("prev");
            return false;
        });
        
        jQuery('.testimonial-box').flexslider({
            'controlNav': false,
            'directionNav' : false,
            "touch": true,
            "animation": "fade",
            "animationLoop": true,
            "slideshow" : true
        });
                
        jQuery('.featured-item').hover(function() {
            if(!jQuery(this).hasClass('featured-list')) {
                jQuery(this).find('.bottom').slideDown('fast');
//                jQuery(this).find('.bubble').fadeIn('fast');
                jQuery(this).parent().find('.price-wrapper').slideUp('fast');
                jQuery(this).parent().find('.star-rating').slideUp('fast');
            }
        }, function() {
            if(!jQuery(this).hasClass('featured-list')) {
                jQuery(this).find('.bottom').slideUp('fast');
                jQuery(this).parent().find('.price-wrapper').slideDown('fast');
                jQuery(this).parent().find('.star-rating').slideDown('fast');
            }
        });
        
        jQuery('.featured-items-slider').flexslider({
            'controlNav': false,
            'directionNav' : false,
            "touch": true,
            "animation": "fade",
            "animationLoop": true,
            "slideshow" : false
        });
        
        jQuery('.featured-items .next').click(function(){
            $('.featured-items-slider').flexslider("next");
            return false;
        });
        
        jQuery('.featured-items .prev').click(function(){
            $('.featured-items-slider').flexslider("prev");
            return false;
        });
        
		// Simple image gallery. Uses default settings
		jQuery('.fancybox').fancybox();
	
		// Different effects 
		// Change title type, overlay closing speed
		jQuery(".fancybox-effects-a").fancybox({
			helpers: {
				title : {
					type : 'outside'
				},
				overlay : {
					speedOut : 0
				}
			}
		});
	
		// Disable opening and closing animations, change title type
		jQuery(".fancybox-effects-b").fancybox({
			openEffect  : 'none',
			closeEffect	: 'none',
	
			helpers : {
				title : {
					type : 'over'
				}
			}
		});
	
		// Set custom style, close if clicked, change title type and overlay color
		jQuery(".fancybox-effects-c").fancybox({
			wrapCSS    : 'fancybox-custom',
			closeClick : true,
	
			openEffect : 'none',
	
			helpers : {
				title : {
					type : 'inside'
				},
				overlay : {
					css : {
						'background' : 'rgba(238,238,238,0.85)'
					}
				}
			}
		});
	
		// Remove padding, set opening and closing animations, close if clicked and disable overlay
		jQuery(".fancybox-effects-d").fancybox({
			padding: 0,
	
			openEffect : 'elastic',
			openSpeed  : 150,
	
			closeEffect : 'elastic',
			closeSpeed  : 150,
	
			closeClick : true,
	
			helpers : {
				overlay : null
			}
		});
	
		// Button helper. Disable animations, hide close button, change title type and content
		jQuery('.fancybox-buttons').fancybox({
			openEffect  : 'none',
			closeEffect : 'none',
	
			prevEffect : 'none',
			nextEffect : 'none',
	
			closeBtn  : false,
	
			helpers : {
				title : {
					type : 'inside'
				},
				buttons	: {}
			},
	
			afterLoad : function() {
				this.title = 'Image ' + (this.index + 1) + ' of ' + this.group.length + (this.title ? ' - ' + this.title : '');
			}
		});
	
	
		// Thumbnail helper. Disable animations, hide close button, arrows and slide to next gallery item if clicked
		jQuery('.fancybox-thumbs').fancybox({
			prevEffect : 'none',
			nextEffect : 'none',
	
			closeBtn  : false,
			arrows    : false,
			nextClick : true,
	
			helpers : {
				thumbs : {
					width  : 50,
					height : 50
				}
			}
		});
	
		// Media helper. Group items, disable animations, hide arrows, enable media and button helpers.
		jQuery('.fancybox-media')
			.attr('rel', 'media-gallery')
			.fancybox({
				openEffect : 'none',
				closeEffect : 'none',
				prevEffect : 'none',
				nextEffect : 'none',
	
				arrows : false,
				helpers : {
					media : {},
					buttons : {}
				}
			});      
    },
    toggleLocationFinder : function() {
        var $this = jQuery(this);
        if($this.hasClass('expanded')) {
            jQuery('.location-finder .left-side').animate({
                marginLeft : "-40%"
            }, {
                duration : 500,
                queue : false
            });
            jQuery('.location-finder .right-side').animate({
                width : "100%"
            }, {
                duration : 550,
                queue : false,
                complete : function() {
                    $('#map_canvas').gmap3({trigger:"resize"});
                }
            });
            $this.removeClass('expanded');
        } else {
            jQuery('.location-finder .left-side').animate({
                marginLeft : "0px"
            }, {
                duration : 550,
                queue : false
            });
            jQuery('.location-finder .right-side').animate({
                width : "60%"
            }, {
                duration : 500,
                queue : false,
                complete : function() {
                    $('#map_canvas').gmap3({trigger:"resize"});
                }
            });
            $this.addClass('expanded');
        }
        return false;
    },
    openHiddenHeader : function() {
        $('#hidden-header').animate({
            marginTop : 0
        }, 
        {
            duration : 500,
            queue : false,
            complete : function() {
                $(this).addClass('open');
            }
        });
    },
    closeHiddenHeader : function() {
        $('#hidden-header').animate({
            marginTop : "-409px"
        }, {
            duration : 500,
            queue : false,
            complete : function() {
                $(this).removeClass('open');
            }
        });
    }
}

jQuery(document).ready(function() {
    fortravelers.init();
});
