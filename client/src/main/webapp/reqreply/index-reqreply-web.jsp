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
                        <div class="col-md-9">
                            <div class="form-group">
                                <label class="control-label col-md-2" for="idRequest">
                                    Request:
                                </label>
                                <div class="col-md-3">
                                    <input class="form-control" id="idRequest" name="idRequest" value="EQ|CMGETCHARGES|TYC|FR20100101|TO20180101" autocomplete="off" type="text">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-md-2" for="ident">
                                    Ident: 
                                </label>
                                <div class="col-md-3">
                                    <select id="idIdent" name="ident" class="input-sm" data-style="btn-primary" required="" >
                                        <option selected >VIDEOSYS</option>
                                        <option >00000100</option>
                                        <option >00000101</option>
                                        <option >ACSIP</option>
                                        <option >AAA</option>
                                    </select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2" for="idCommand">
                                    Command:
                                </label>
                                <div class="col-md-3">
                                    <input class="form-control" id="idCommand" name="idCommand" value="SY" autocomplete="off" type="text">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-md-2" for="idPort">
                                    Port:
                                </label>
                                <div class="col-md-3">
                                    <input class="form-control" id="idPort" name="idPort" value="5102" autocomplete="off" type="text">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="control-label col-md-2" for="idTimeout">
                                    Timeout [s]:
                                </label>
                                <div class="col-md-1">
                                    <input class="form-control" id="idTimeout" name="idTimeout" value="" autocomplete="off" type="text">
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="col-md-9 col-md-offset-3">
                                    <button id="btn_submit" type="button" class="btn btn-success">
                                        Send
                                    </button>
                                    <button id="btn_clear" type="button" class="btn btn-default">
                                        Clear
                                    </button>

                                </div>                                    
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="loader" id="idLoader"></div>
                        </div>
                    </div>
                    <div >
                        <br>
                    </div>

                </form>
                <div class="row">
                    <div class="col-md-9">
                        <label class="control-label col-md-2" for="idResponseLength">
                            ResponseLength:
                        </label>
                        <div class="col-md-1">
                            <div class="form-control" id="idResponseLength" name="responseLength" value="" autocomplete="off" type="text">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-9">
                        <label class="control-label col-md-2" for="idResponseTime">
                            ResponseTime:
                        </label>
                        <div class="col-md-1">
                            <div class="form-control" id="idResponseTime" name="responseTime" value="" autocomplete="off" type="text">
                            </div>
                        </div>
                        <label class="control-label col-md-2" for="idResponseTime">
                            ms
                        </label>
                    </div>                    
                    <div class="row">
                        <div class="col-md-9">
                            <div class="form-group">
                                <label class="control-label" for="idResponse">
                                    Response                                   
                                </label>
                                <textarea id="idResponse" rows="10" readonly="readonly" required="required" class="form-control" name="response" data-parsley-trigger="keyup" data-parsley-minlength="20" data-parsley-maxlength="5000" data-parsley-minlength-message="Come on! You need to enter at least a 20 caracters long comment.." data-parsley-validation-threshold="10"></textarea>
                            </div>
                        </div>
                    </div>

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
                                    $("#btn_submit").prop('disabled', true).delay(500);
                                    $("#btn_submit").attr('class', 'btn btn-default').delay(500);
                                    $("#idLoader").show().delay(500);
                                    doSubmit();
                                });

                                $("#btn_clear").bind("click", function (event, data) {
                                    doClear();
                                });


                                $("#btn_submit").prop('disabled', false);
                                $("#btn_submit").attr('class', 'btn btn-success');

                                $("#btn_clear").prop('disabled', true);
                                $("#btn_clear").attr('class', 'btn btn-default');

                                $("#idLoader").hide();

                                var surl = "<s:url action='doGetTimeout.action' namespace='/jmsReqReply'/>";

                                var ret = null;

                                $.ajax({
                                    url: surl,
                                    type: "GET",
                                    cache: false,
                                    async: true,
                                    success: function (html) {
                                        if (html == null || html.jsonStatus == null || html.jsonStatus.status != "OK") {
                                            console.log("unable to get timeout");
                                            ret = false;
                                        } else {
                                            $("#idTimeout").val(html.timeout);
                                        }
                                    },
                                    error: function (jqXHR, textStatus, errorThrown) {

                                        alert("unable to get timeout");
                                        console.log("unable to get timeout" + textStatus);
                                        return;
                                    }
                                });
                            });

                        </script>

                        <script type="text/javascript">

                            function doClear() {
                                $("#idResponse").text("");
                                $("#idResponseLength").text("");
                                $("#idResponseTime").text("");
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
                                var command = $("#idCommand").val();
                                var port = $("#idPort").val();
                                var timeout = $("#idTimeout").val();
                                var surl = "<s:url action='doSendMessage.action' namespace='/jmsReqReply'/>?ident=" + ident + "&request=" + request + "&timeout=" + timeout + "&command=" + command + "&port=" + port;

                                var ret = null;

                                $.ajax({
                                    url: surl,
                                    type: "GET",
                                    cache: false,
                                    async: true,
                                    success: function (html) {
                                        if (html == null || html.jsonStatus == null || html.jsonStatus.status != "OK") {
                                            console.log("unable to send request");
                                            $("#idLoader").hide();
                                            ret = false;
                                        } else {


                                            $("#idLoader").hide();
                                            $("#btn_submit").prop('disabled', true);
                                            $("#btn_submit").attr('class', 'btn btn-default');

                                            $("#btn_clear").prop('disabled', false);
                                            $("#btn_clear").attr('class', 'btn btn-success');

                                            if (html.response != null) {

                                                $("#idResponseLength").text(html.response.length);
                                                $("#idResponseTime").text(html.duration);
                                                $("#idResponse").text(html.response);

                                            } else {
                                                $("#idResponse").text("TIMEOUT");
                                                $("#idResponseLength").text("0");
                                            }
                                            ret = true;
                                        }
                                    },
                                    error: function (jqXHR, textStatus, errorThrown) {

                                        alert("unable to send request");
                                        console.log("unable to send request" + textStatus);
                                        $("#idLoader").hide();
                                        return;
                                    }
                                });
                            }
                        </script>
                    </content>

                    </body>

                    </html>