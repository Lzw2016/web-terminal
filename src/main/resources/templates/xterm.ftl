<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="xterm/xterm.css"/>
    <script src="xterm/xterm.js"></script>
    <script src="xterm/addons/fit/fit.js"></script>
    <title>xterm</title>
</head>
<body>
<div class="container">
    <div id="terminal-container"></div>
</div>
<script>
    Terminal.applyAddon(fit);


    var term = new Terminal(); // {cols: 200, rows: 30}
    term.open(document.getElementById('terminal-container'));
    term.fit();
    var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    var socketURL = protocol + location.hostname + ((location.port) ? (':' + location.port) : '') + "/terminal";
    var sock = new WebSocket(socketURL);
    sock.onopen = function () {
        term.writeln("连接一打开");
        send({type: "TERMINAL_RESIZE", columns: 200, rows: 30}, 0);

        send({type: "TERMINAL_COMMAND", command: "mvn\r"}, 2);
        send({type: "TERMINAL_COMMAND", command: "exit\r"}, 2);
        setTimeout(function () {
            sock.close();
        }, 10000);
    };
    sock.onmessage = function (evt) {
        var json = JSON.parse(evt.data);
        term.write(json.text);
    };
    sock.onclose = function (evt) {
        term.writeln("关闭连接");
    };
    sock.onerror = function (evt) {
        term.writeln("错误");
    };

    var send = function (data, s) {
        setTimeout(function () {
            sock.send(JSON.stringify(data));
        }, s * 1000);
    }
</script>
</body>
</html>