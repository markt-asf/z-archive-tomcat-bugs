<%@ page session="false" pageEncoding="UTF-8"%>
<script type="application/javascript">

    var ws = new WebSocket("ws://localhost:8080/tomcat-bugs/ws001");
    
    function start_webserver() {
        ws.send("ac_1");
    }

    ws.onopen = function(evt) {
        console.log("Connection open ...");
    };

    ws.onmessage = function(evt) {
        console.log("Received Message: " + evt.data);
        if (chart.series[0].data.length > 400) {
            chart.series[0].addPoint(parseFloat(evt.data), false, true, false);
        } else {
            chart.series[0].addPoint(parseFloat(evt.data), false, false, false);
        }
    };

    ws.onclose = function(evt) {
        console.log("Connection closed.");
        ws.close();
    };

    ws.onerror = function (evt) {
        console.log("error: ", evt);
    };

    function end_webserver(){
        console.log("end the connection");
        ws.close();
        console.log("over");
    }
   
</script>

// the buttons
<button id="button" onclick="start_webserver()">Start</button>
<button id="button" onclick="end_webserver()">stop</button>
