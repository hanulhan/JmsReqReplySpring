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
                        <div class="col-xs-7">
                            <div class="form-group">
                                <label class="control-label col-md-3 col-sm-3 col-xs-12" for="Request">
                                    Request:
                                </label>
                                <div class="col-md-9 col-sm-9 col-xs-12">
                                    <input class="form-control" id="locationSearch" placeholder="Request-1" autocomplete="off" type="text">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-md-3 col-sm-3 col-xs-12" for="ident">
                                    Ident: 
                                </label>
                                <div class="col-md-9 col-sm-9 col-xs-12">
                                    <select id="ident" name="Ident" class="input-sm" data-style="btn-primary" required="" >
                                        <option value="VIDEOSY">VIDEOSYS</option>
                                        <option value="ACSIP">ACSIP</option>
                                        <option value="AAA">AAA</option>
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button id="btn_submit" type="button" class="btn btn-success">
                                    Send
                                </button>
                            </div>
                        </div>
                    </div>
                </form>

            </div>
        </div>
    </div>
</body>

<content tag=footlines">
    <!-- // yatdcf - yet another tabledata column filter // -->
    <script src="<s:url value='/js/'/>yadcf/jquery.dataTables.yadcf.js"></script>

    <!-- // JQuery locationpicker // -->
    <script src="<s:url value='/js/'/>vendors/locationpicker/locationpicker.jquery.min.js"></script>

    <script>
        var sDateFormat = "<s:text name='global.dateformat.long'/>";
        var viewLink = "<a href='javascript:void(0);' onclick='doViewLocationData(\"#id#\");'><i class=\"fa fa-table\" /></a>";
        var editLink = "<a href='javascript:void(0);' onclick='doGetLocation(\"#id#\");'><i class=\"fa fa-edit\" /></a>";
        var removeLink = "<a href='javascript:void(0);' onclick='removeLocation(\"#id#\");'><i class=\"fa fa-trash-o\" /></a>";

        $(document).ready(function () {

//            var dtWeather = $('#table_weatherlist')
//                .DataTable({
//                    "autoWidth": false,
//                    "bStateSave": false,
//                    "bPaginate": true,
//                    "bProcessing": true,
//                    "bFilter": true,
//                    "bInfo": true,
//                    "bServerSide": false,
//                    "aoColumns": [
//                        { "sType": "string" }, //  location id
//                        { "bSortable": false }, //  ident
//                        { "sType": "string" }, //  destription
//                        { "sType": "datetime-euro" }, //  Last update
//                        { "bSortable": false, "sClass": "center" }, //view
//                        { "bSortable": false, "sClass": "center" }, //edit
//                        { "bSortable": false, "sClass": "center" } //remove
//                    ],
//                    "fnRowCallback": function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
//                        $('td:eq(4)', nRow).html(viewLink.replace("#id#", aData[0]));
//                        $('td:eq(5)', nRow).html(editLink.replace("#id#", aData[0]));
//                        $('td:eq(6)', nRow).html(removeLink.replace("#id#", aData[0]));
//                    },
//                    "oLanguage": {
//                        "sProcessing": "<s:text name='global.table.Processing'/>",
//                        "sLengthMenu": "<s:text name='global.table.lengthMenu'/>",
//                        "sZeroRecords": "<s:text name='global.table.ZeroRecords'/>",
//                        "sInfo": "<s:text name='global.table.Info'/>",
//                        "sInfoEmpty": "<s:text name='global.table.InfoEmpty'/>",
//                        "sInfoFiltered": "<s:text name='global.table.InfoFiltered'/>",
//                        "sInfoPostFix": "",
//                        "sSearch": "<s:text name='global.table.Search'/>",
//                        "sUrl": "",
//                        "oPaginate": {
//                            "sFirst": "<s:text name='global.table.Paginate.First'/>",
//                            "sPrevious": "<s:text name='global.table.Paginate.Previous'/>",
//                            "sNext": "<s:text name='global.table.Paginate.Next'> </s:text>",
//                            "sLast": "<s:text name='global.table.Paginate.Last'/>"
//                         }
//                     },
//                    "dom": '<Blf<t>ip>',
//                    "buttons": []
//                });

//            yadcf.init(dtWeather, [
//                {
//                    column_number: 1,
//                    filter_default_label: "Select Country",
//                    filter_container_selector: ".dt-buttons",
//                    style_class: "form-control input-sm"
//                }
//            ]);

//            if ((!loadCountryList()) || (!loadWeatherList())) {
//                alert("unable to initialize weather locations");
//            }

//            doResetDisplay();

//            $("#btn_new").bind("click", function (event, data) {
//                doResetDisplay();
//
//                // init locationpicker
//                $("#locationMap").locationpicker({
//                    location: {
//                        latitude: 52.0277312,
//                        longitude: -0.49507570000002943
//                    },
//                    zoom: 15,
//                    radius: 200,
//                    inputBinding: {
//                        latitudeInput: $("#lat"),
//                        longitudeInput: $("#lon"),
//                        locationNameInput: $("#locationSearch")
//                    },
//                    enableAutocomplete: true,
//                    onchanged: function(currentLocation, radius, isMarkerDropped) {
//                        var mapContext = $(this).locationpicker('map');
//                        mapContext.map.setZoom(15);
//                    }
//                });
//                // call autosize as widget container is initially invisble
//                // see @{link} http://logicify.github.io/jquery-locationpicker-plugin/
//                $(".modal-edit").one("shown.bs.modal", function () {
//                    $("#locationMap").locationpicker("autosize");
//                });
//
//                // show modal popup
//                $(".modal-edit").modal("show");
//            });

            $("#btn_submit").bind("click", function (event, data) {
                doSubmit();
            });

        });

    </script>

    <script>
        function doSubmit() {
            // validate user form
            var valid = $("#inputform").parsley().validate();
            if (!valid)
                return;

            var surl = "<s:url action='doInsertLocation.action' namespace='/weather'/>";
            if ($("#locationId").prop("readonly") == true) {
                surl = "<s:url action='doUpdateLocation.action' namespace='/weather'/>";
            }
            ;
            var currObj = {};
            currObj.id = $("#locationId").val();
            currObj.name = $("#locationName").val();
            currObj.ident = $("#ident").val();
            currObj.lat = $("#lat").val();
            currObj.lon = $("#lon").val();

            var obj = {};
            obj.location = currObj;
            var sdata = JSON.stringify(obj);

            $.ajax({
                type: "POST",
                url: surl,
                dataType: 'json',
                async: false,
                data: sdata,
                contentType: "application/json; charset=utf-8",
                success: function (html) {
                    if (html == null || html.jsonStatus == null || html.jsonStatus.status != "OK") {
                        // nicer error message
                        new PNotify({
                            title: "<s:text name='common.error' />",
                            text: html.jsonStatus.errorMsg,
                            styling: 'bootstrap3',
                            type: "error"
                        });
                        return
                    } else {
                        if ($("#locationId").prop("readonly") == true) {
                            var lastupdate = $('#table_weatherlist').find('tr#' + currObj.id).find('td:eq(3)').html();
                            $('#table_weatherlist').dataTable().fnUpdate(
                                    [currObj.id, currObj.ident, currObj.name, lastupdate, "", "", ""],
                                    $('#table_weatherlist tr[id=' + currObj.id + ']')[0]
                                    );
                        } else {
                            var oTable = $('#table_weatherlist').dataTable();
                            var addId = oTable.fnAddData(
                                    [currObj.id, currObj.ident, currObj.name, "", "", "", ""],
                                    false
                                    );
                            var theNode = $('#table_weatherlist').dataTable().fnSettings().aoData[addId[0]].nTr;
                            theNode.setAttribute('id', currObj.id);
                        }
                        doResetDisplay();
                        // close modal popup
                        $(".modal-edit").modal("hide");
                        return;
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    alert("unable to save location");
                    console.log("unable to save location  status:" + textStatus);
                    return;
                }
            });
        }
    </script>
</content>
</html>