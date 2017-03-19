<%@ page language="java" contentType="text/html; charset=US-ASCII"
         pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%-- Using Struts2 Tags in JSP --%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
        <title>Jms-ReqReply WebClient</title>
    </head>
    <body>
        <h3>Send a message</h3>
        <script>
        </script>   
        
        <s:form action="doSendMessage" namespace="/jmsReqReply" method="post" >
            <s:select label="ClientId"
                      name="clientId"
                      headerKey="1" 
                      headerValue="Select"
                      list="#{'1':'1', '2':'2', '3':'3', '4':'4', '5':'5', '6':'6'}"
                      value="1"
                      required="true"
                      />
            <!--
            <s:textfield name="ident" label="System Ident"></s:textfield>
            -->
            
            <s:select label="System Iden"
                      name="ident"
                      headerKey="1" 
                      headerValue="Select"
                      list="#{'AAAA':'AAAA', 'BBBB':'BBBB', 'CCCC':'CCCC', 'DDDD':'DDDD', 'EEEE':'EEEE', 'FFFF':'FFFF'}"
                      value="AAAA"
                      required="true"
                      />
            <s:submit value="Send Message"></s:submit>
        </s:form>
    </body>
</html>