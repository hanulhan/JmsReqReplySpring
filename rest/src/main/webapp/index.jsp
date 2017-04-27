<%@ page language="java" contentType="text/html; charset=US-ASCII"
         pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%-- Using Struts2 Tags in JSP --%>
<html>
    <script type="text/javascript">
        var xml='' +
                '<ReqReply>' +
                '<ident>VIDEOSYS</ident>' +
                '<messageid>MESSAGE-ID-1</messageid>' +
                '<request>REQUEST-1</request>' +
                '<response>RESPONSE-1</response>' +
                '</ReqReply>';
        
        function NavigateSystemAliveRestCall() {

            var ctx = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
            var s = window.location.origin + ctx + "/rest/systemalive";
            window.open(s);
        }

        function NavigateSystemDataRestCall() {
            var ctx = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
            var s = window.location.origin + ctx + "/rest/systemdata";
            var xmlhttp= new XMLHttpRequest();
            xmlhttp.open("POST", s, true);
            xmlhttp.send(escape(xml));
        }
    </script>   

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
        <title>Jms-ReqReply WebClient</title>
    </head>
    <body>
        <h3>ReqReply REST</h3>

        <div id="inputPanel">
            <br>
            <br>   
            <a id="myLink" title="navigate" href="javascript:void(0)" onclick="NavigateSystemAliveRestCall();return false;">Rest call SystemAlive</a>
            <br>
            <a id="myLink" title="navigate" href="javascript:void(0)" onclick="NavigateSystemDataRestCall();return false;">Rest call SystemData</a>
        </div>

        <form id="TheForm" method="post" target="TheWindow">
            <input type="hidden" name="ident" value="VIDEOSYS" />
            <input type="hidden" name="request" value="REQUEST-1" />
            <input type="hidden" name="response" value="RESPONSE-1" />
            <input type="hidden" name="messageid" value="MESSAGE-ID-1" />
        </form>
    </body>
</html>