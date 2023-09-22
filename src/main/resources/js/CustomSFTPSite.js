pluginRegister.setPluginComponent(new CustomPluginSite());
function CustomPluginSite() {       
    function load(accountName, siteId) {
               
        // checkboxes

		
        var $downloadExpression = $('#csftpDownloadExpression');
        var $patternExpression = $('#csftpPatternExpression');        
        var $uploadAllowOverwrite = $('#csftpUploadAllowOverwrite');
        var $usePassword_Control = $('#csftp_usePassword_Control');
        var $password = $('#csftpPassword');
        
        if (siteId) {
        	var selectedCertificateURL = null;
            $.ajax({
                url : "/api/v1.4/sites/" + siteId,
                type: 'GET',
                dataType : 'json',
                cache: false,
                success : function(data) {
                    if (data && data['protocol'] === 'CustomSFTP') {
                        $.map( data, function( value, key ) {                            
                            var checkedValue = value == 'true' ? true : false;
                            if (key == 'csftpDownloadExpression') {                          
                                changeChecked($downloadExpression, checkedValue);
                                if(checkedValue) {
                                    $('#csftpDownloadFolder').addClass("template-input");
                                } else {
                                   $('#csftpDownloadFolder').removeClass("template-input"); 
                                }
                            } else if(key == 'csftpPatternExpression'){                           
                                changeChecked($patternExpression, checkedValue);
                                if(checkedValue) {
                                    $('#csftpDownloadPattern').addClass("template-input");
                                   
                                } else {
                                   $('#csftpDownloadPattern').removeClass("template-input"); 
                                   
                                }
                            } else if(key == 'csftpUploadAllowOverwrite'){
                                changeChecked($uploadAllowOverwrite, checkedValue);
                                if(checkedValue) {
                                    $('#csftpUploadFolder').addClass("template-input");
                                } else {
                                   $('#csftpUploadFolder').removeClass("template-input"); 
                                }
                            } else if (key == 'metadata') {
                                if ('links' in value) {
                                    $.map( value['links'], function( linkUrl, link ) {
                                        if (link.match('^clientLocalCertificate')) {
                                            selectedCertificateURL = linkUrl;
                                        }
                                    });
                                }
                            } else if(key == 'csftp_usePassword_Control'){
                                changeChecked($usePassword_Control, checkedValue);
                                if(value == 'false'){
                                    $password.val('');
                                }
                            } else if (key == 'csftpPassword'){
                                $password.val('');
                            }  else if (key == 'csftp_networkZone') {
                                custom_fetchDmzZones('csftp_networkZone', value); // load zones with selected one
                                $('#' + key).val(value);
                            }  else if (key == "csftpPatternType") {
                                if (value == "regex") {
                                    $('#csftpPatternTypeRegex').prop('checked', true);
                                } else {
                                    $('#csftpPatternTypeGlob').prop('checked', true);
                                }
								}else{
                                $('#' + key).val(value);
                            }
                        });  
                        getCertList(accountName, selectedCertificateURL);
                    } else {
                        fetchDefaultValues(accountName);
                       }
                }
            });
        } else {
             fetchDefaultValues(accountName);
        }
    } 

    function fetchDefaultValues(accountName) {
        custom_fetchDmzZones('csftp_networkZone', 'none'); // load zones with default selected
        getCertList(accountName);
        $('#csftpPatternTypeGlob').prop('checked', true);
        $('#csftp_usePassword_Control').prop('checked', false);
    }
    
    function setCheckBox(elementId) {
        var targetElement = $('#' + elementId);
        if (targetElement.prop('checked')) {
            targetElement.val("true");
        } else {
            targetElement.val("false");            
        }
    }

    function save(siteData, callback) {
        setCheckBox("csftpDownloadExpression");
        setCheckBox("csftpPatternExpression");
        setCheckBox("csftpUploadAllowOverwrite");
        setCheckBox("csftp_usePassword_Control");


        if (siteData) {

            var customProperties = {
            		"csftpHost": $('#csftpHost').val(),
                    "csftpPort": $('#csftpPort').val(),
                "csftp_networkZone": $('#csftp_networkZone').val(),
                "csftpDownloadExpression": $('#csftpDownloadExpression').val(),
                "csftpDownloadFolder": $('#csftpDownloadFolder').val(),
                "csftpPatternExpression": $('#csftpPatternExpression').val(),
                "csftpDownloadPattern": $('#csftpDownloadPattern').val(),
                "csftpPatternType": $('input:radio[name=csftpPatternType]:checked').val(),
                "csftpUploadAllowOverwrite": $('#csftpUploadAllowOverwrite').val(),
                "csftpUploadFolder": $('#csftpUploadFolder').val(),
                "csftpValidate": $('#csftpValidate').val(),
                "csftpUserName": $('#csftpUserName').val(),
                "csftp_usePassword_Control": $('#csftp_usePassword_Control').val(),
				"csftpCustomCmdUpd": $('#csftpCustomCmdUpd').val(),
				"csftpCustomCmdDwn": $('#csftpCustomCmdDwn').val(), 
				"csftpShowAdvancedSettingsEnabled": $("[name='csftpShowAdvancedSettingsEnabled']").val(),
                "csftpCipherSuites": $("[name='csftpCipherSuites']").val(),
                "csftpHmacs": $("[name='csftpHmacs']").val(),
                "csftpKex": $("[name='csftpKex']").val()
           
            },
            siteUrl = "/api/v1.4/sites",
            siteId = siteData.id;
            
            selectedCertificateOld = $('#clientCertificateIdOld').val(),
            selectedCertificateNew = $('#clientLocalCertificate').val(),
            
            data = null;
            
            var validationErr = [];
    
             
            if (selectedCertificateOld != selectedCertificateNew) {
                if (selectedCertificateNew == '') {
                    selectedCertificateNew = null;
                }
                // Consider removal of Certificate ID from Custom Properties and handle it as link each time.
                $.extend( customProperties, {"clientLocalCertificate": selectedCertificateNew});
            }
            
            var csftpPassword = $('#csftpPassword').val();
            if(csftpPassword) {
                $.extend( customProperties, {"csftpPassword": csftpPassword});
            }
            

            $.extend( siteData, customProperties );
            
            // check if there are validation errors
            if (validationErr && validationErr.length > 0) {
                var message = '';
                for(var i = 0; i < validationErr.length; i++) {
                    message += "\n" + validationErr[i];
                }
                if (callback) {
                    callback(message);
                    return;
                }
            }
            
            if (siteId) {
                // update site
                siteUrl += '/' + siteId;
                data = siteData;
            } else {
                data = {'sites': [siteData]};
            }

            $.ajax({
                    url : siteUrl,
                    type: 'POST',
               dataType : 'json',
            contentType : 'application/json',
                    data: JSON.stringify(data),
                success : function() {
                                if (callback) {
                                    callback();
                                }
                        }, 
                   error: function(jqXHR, textStatus, errorThrown) {
                        // show 'validationErrors' or 'message' key values
                        var errorMsg = '',
                        jsonDoc = jQuery.parseJSON(jqXHR.responseText),
                        validationErrors = jsonDoc["validationErrors"],
                        message = jsonDoc["message"];

                        if (validationErrors && validationErrors.length > 0) {
                            for(var i = 0; i < validationErrors.length; i++) {
                                errorMsg += "\n" + validationErrors[i];
                            }
                        } else if (message) {
                            errorMsg = message;
                        }

                        if (callback) {
                            callback(errorMsg);
                        }
                    }
            });
        }
    }

    var instance = new PluginComponentInterface();
    instance.load = load;
    instance.save = save;

    return Object.seal(instance);
}

      
    function changeChecked(id, isChecked){
        id.val(isChecked); 
        if(isChecked){
            id.attr('checked', 'checked');
        }else{
            id.removeAttr('checked');
        }
    }
    
    function showHideElementsOld(chObjectId, elementId ) {
        var checked = $('#' + chObjectId).is(":checked")
        if (checked) {
            $("#" + elementId).css("display", dhtmlTableRowParam);;
        } else {
            $("#" + elementId).css("display", "none");
        }
        
    }
    
    function showHideElements(chObjectId, elementId) {
    showHideElementsInternal(chObjectId, elementId, dhtmlTableRowParam);
}

function showHideBlockElements(chObjectId, elementId) {
    showHideElementsInternal(chObjectId, elementId, "block");
}

function showHideElementsInternal(chObjectId, elementId, displayType) {
    var isChecked = $('#' + chObjectId).is(":checked");
    $("#" + elementId).css("display", isChecked ? displayType : "none");
}
    

    
    $("#csftpDownloadExpression").change(function() {
        if (this.checked) {
            $('#csftpDownloadFolder').addClass("template-input");
        } else {
           $('#csftpDownloadFolder').removeClass("template-input"); 
        }
    });
    
    $("#csftpPatternExpression").change(function() {
        if (this.checked) {
            $('#csftpDownloadPattern').addClass("template-input");
        } else {
           $('#csftpDownloadPattern').removeClass("template-input");   
        }
    });
    
    $("#csftpcsftpUploadAllowOverwrite").change(function() {
        if (this.checked) {
            $('#csftpUploadFolder').addClass("template-input");
        } else {
           $('#csftpUploadFolder').removeClass("template-input"); 
        }
    });
        

 /**
     * [BEGIN]  dmzZone functions
     */
    /* Retrieves all DMZ zones from database */
    function custom_fetchDmzZones(selectId, custom_selectedZone) {
        custom_appendDefaultOptions(selectId, custom_selectedZone);
        
        $.ajax({
            url : "/api/v1.4/zones",
            type: 'GET',
            dataType : 'json',
            cache: false,
            success : function(data) {
                var zonesList = data.zones;
                if (zonesList && zonesList.length > 0) {
                    custom_fillDmzZonesSelect(selectId, custom_selectedZone, zonesList);
               }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("Error occurred on retrieving Network Zone.");
            }
        })
    }


    function getCertList(accountName, selectedCertificateURL) {
        var $selectCertificate = $("#clientLocalCertificate");
        var $selectCertificateOld = $("#clientCertificateIdOld");
        var selectedCertificateId = null;
        if (selectedCertificateURL) {
            $.ajax({
                url : selectedCertificateURL,
                type: 'GET',
                dataType : 'json',
                contentType : 'application/json',
                success : function(certificate) {
                    if (certificate) {
                        selectedCertificateId = certificate.id;
                        $selectCertificateOld.val(selectedCertificateId);
                    }
                }
            });
        }
        $selectCertificate.append($("<option>" , {text: " (Select Key) ",value: ""}));
        $.each(['private', 'local'], function() {
         var queryParams = {"type": "x509", "usage": this};
           if (this == 'private') {
               $.extend(queryParams, {"account": accountName});
               
           }
            var queryParamsSSH = {"type": "SSH", "usage": this};
            if (this == 'private') {
                $.extend(queryParamsSSH, {"account": accountName});
              
            }
            $.ajax({
                     url : "/api/v1.4/certificates",
                     type: 'GET',
                     dataType : 'json',
                     contentType : 'application/json',
                     data:  queryParams,
                     success : function(data) {
                        var certificates = data.certificates;
                        if (certificates && certificates.length > 0) {
                           $.each(certificates, function() {
                                var $option = $("<option>" , {text: this.name, value: this.id});
                                if ((selectedCertificateId !== 'undefined' || selectedCertificateId !== null) && (selectedCertificateId == this.id)) {
                                    $option.attr('selected', 'selected');
                                }
                                $selectCertificate.append($option);
                           });
                        }
                     }
            });
            
            $.ajax({
                url : "/api/v1.4/certificates",
                type: 'GET',
                dataType : 'json',
                contentType : 'application/json',
                data:  queryParamsSSH,
                success : function(data) {
                   var certificates = data.certificates;
                   if (certificates && certificates.length > 0) {
                      $.each(certificates, function() {
                           var $option = $("<option>" , {text: this.name, value: this.id});
                           if ((selectedCertificateId !== 'undefined' || selectedCertificateId !== null) && (selectedCertificateId == this.id)) {
                               $option.attr('selected', 'selected');
                           }
                           $selectCertificate.append($option);
                      });
                   }
                }
       });
        });
    }
    
    /* fills specified <select> with list of zones and select specified zone */
    function custom_fillDmzZonesSelect(selectId, selectedZone, zonesList) {
       for (var i = 0; i < zonesList.length; i++) {
           var zone = zonesList[i].zone;
           // if not the default zone, add it into the list.
           if ('Private' != zone.name) {
               custom_appendOption(selectId, selectedZone, zone.name);
           }
       }
    }

    function custom_appendDefaultOptions(selectId, selectedZone) {
        custom_appendOption(selectId, selectedZone, 'none');
        custom_appendOption(selectId, selectedZone, 'any');
        custom_appendOption(selectId, selectedZone, 'Default');
    }

    /* append <option> to specified <select> */
    function custom_appendOption(selectId, selectedElement, value) {
       if (selectedElement == value) {
           $('<option value="' + custom_htmlescape(value) + '" selected="selected">' + value + '</option>').appendTo($('#' + selectId));
       } else {
           $('<option value="' + custom_htmlescape(value) + '">' + value + '</option>').appendTo($('#' + selectId));
       }
    }

    /* Escape script characters as <&"\> */
    function custom_htmlescape(str) {
        var ret = str;
        if (str) {
            var regexp = /&/g;
            ret = ret.replace(regexp, "&amp;");
            regexp = /</g;
            ret = ret.replace(regexp, "&lt;");  
            regexp = />/g;  
            ret = ret.replace(regexp, "&gt;");
            regexp = /\"/g; 
            ret = ret.replace(regexp, "&quot;"); 
            regexp = /\\/g; 
            ret = ret.replace(regexp, "&#092;"); 
        }
        return ret;
    }
    /**
     * [END]  dmzZone functions
     */ 