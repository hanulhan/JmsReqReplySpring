<%@ page language="java" contentType="text/html; charset=US-ASCII" pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%-- Using Struts2 Tags in JSP --%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
    <head>

    </head>
    <body>
        <div class="x_panel">

            <div class="x_title">
                <h2>Jms-ReqReply WebClient</h2>
                <div class="clearfix"></div>
            </div>

            <div class="x_content">
                <form action="javascript:doSubmit();" id="inputform"
                      class="form-horizontal form-label-left" data-parsley-validate>

                    <div class="row">
                        <div class="col-xs-6">
                            <div class="form-group">
                                <label class="control-label col-md-3 col-sm-3 col-xs-12" for="idRequest">
                                    Request:
                                </label>
                                <div class="col-md-9 col-sm-9 col-xs-12">
                                    <input class="form-control" id="idRequest" name="idRequest" value="Request-1" autocomplete="off" type="text">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-3 col-sm-3 col-xs-12" for="ident">
                                    Ident: 
                                </label>
                                <div class="col-md-9 col-sm-9 col-xs-12">
                                    <select id="idIdent" name="ident" class="input-sm" data-style="btn-primary" required="" >
                                        <option selected >VIDEOSYS</option>
                                        <option >ACSIP</option>
                                        <option >AAA</option>
                                    </select>
                                </div>
                            </div>
                            <a></a>

                            <div class="form-group">
                                <label class="control-label col-md-3 col-sm-3 col-xs-12" for="response">
                                    Response                                   
                                </label>
                                <div class="col-md-9 col-sm-9 col-xs-12">
                                    <div class="form-control" id="idResponse" name="response"></div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xs-2">    
                            <div class="modal-footer">
                                <button id="btn_submit" type="button" class="btn btn-success">
                                    Send
                                </button>
                            </div>
                        </div>
                        <div class="col-xs-2">    
                            <div class="modal-footer">
                                <button id="btn_clear" type="button" class="btn btn-default">
                                    Clear
                                </button>
                            </div>
                        </div>
                    </div>
                </form>
                <content tag="footlines">
                    <!-- // yatdcf - yet another tabledata column filter // -->
                    <script src="<s:url value='/js/'/>yadcf/jquery.dataTables.yadcf.js"></script>

                    <!-- // JQuery locationpicker // -->
                    <script src="<s:url value='/js/'/>vendors/locationpicker/locationpicker.jquery.min.js"></script>

                    <script type="text/javascript">
                        var sDateFormat = "<s:text name='global.dateformat.long'/>";
                        var viewLink = "<a href='javascript:void(0);' onclick='doViewLocationData(\"#id#\");'><i class=\"fa fa-table\" /></a>";
                        var editLink = "<a href='javascript:void(0);' onclick='doGetLocation(\"#id#\");'><i class=\"fa fa-edit\" /></a>";
                        var removeLink = "<a href='javascript:void(0);' onclick='removeLocation(\"#id#\");'><i class=\"fa fa-trash-o\" /></a>";
                        $(document).ready(function () {
                            $("#btn_submit").bind("click", function (event, data) {
                                doSubmit();
                            });

                            $("#btn_clear").bind("click", function (event, data) {
                                doClear();
                            });


                            $("#btn_submit").prop('disabled', false);
                            $("#btn_submit").attr('class', 'btn btn-success');
                            
                            $("#btn_clear").prop('disabled', true);
                            $("#btn_clear").attr('class', 'btn btn-default');
                        });

                    </script>

                    <script type="text/javascript">

                        function doClear() {
                            $("#idResponse").text("");
                            $("#btn_submit").prop('disabled', false);
                            $("#btn_submit").attr('class', 'btn btn-success');
                            $("#btn_clear").prop('disabled', true);
                            $("#btn_clear").attr('class', 'btn btn-default');

                        }

                        function doSubmit() {
                            // validate user form
                            var valid = $("#inputform").parsley().validate();
                            if (!valid)
                                return;

                            var request = $("#idRequest").val();
                            var ident = $("#idIdent").val();
                            var surl = "<s:url action='doSendMessage.action' namespace='/jmsReqReply'/>?ident=" + ident + "&request=" + request;

                            var ret = null;

                            $.ajax({
                                url: surl,
                                type: "GET",
                                cache: false,
                                async: false,
                                success: function (html) {
                                    if (html == null || html.jsonStatus == null || html.jsonStatus.status != "OK") {
                                        console.log("unable to send request" + textStatus);

                                        ret = false;
                                    } else {
                                        //                        if ($("#locationId").prop("readonly") == true) {
                                        //                            var lastupdate = $('#table_weatherlist').find('tr#' + currObj.id).find('td:eq(3)').html();
                                        //                            $('#table_weatherlist').dataTable().fnUpdate(
                                        //                                    [currObj.id, currObj.ident, currObj.name, lastupdate, "", "", ""],
                                        //                                    $('#table_weatherlist tr[id=' + currObj.id + ']')[0]
                                        //                                    );
                                        //                        } else {
                                        //                            var oTable = $('#table_weatherlist').dataTable();
                                        //                            var addId = oTable.fnAddData(
                                        //                                    [currObj.id, currObj.ident, currObj.name, "", "", "", ""],
                                        //                                    false
                                        //                                    );
                                        //                            var theNode = $('#table_weatherlist').dataTable().fnSettings().aoData[addId[0]].nTr;
                                        //                            theNode.setAttribute('id', currObj.id);
                                        //                        }
                                        //doResetDisplay();
                                        // close modal popup
                                        $("#btn_submit").prop('disabled', true);
                                        $("#btn_submit").attr('class', 'btn btn-default');
                                        
                                        $("#btn_clear").prop('disabled', false);
                                        $("#btn_clear").attr('class', 'btn btn-success');

                                        if (html.response != null) {
                                            $("#idResponse").text(html.response);
                                        } else {
                                            $("#idResponse").text("TIMEOUT");
                                        }
                                        ret = true;
                                    }
                                },
                                error: function (jqXHR, textStatus, errorThrown) {
                                    alert("unable to send request");
                                    console.log("unable to send request" + textStatus);
                                    return;
                                }
                            });
                        }
                    </script>
                </content>

                </body>

                </html>