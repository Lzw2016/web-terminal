<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="xterm/xterm.css"/>
    <link rel="stylesheet" href="xterm/addons/fullscreen/fullscreen.css"/>

    <script src="xterm/xterm.js"></script>
    <script src="xterm/addons/fit/fit.js"></script>
    <script src="xterm/addons/fullscreen/fullscreen.js"></script>
    <script src="xterm/addons/search/search.js"></script>
    <script src="xterm/addons/terminado/terminado.js"></script>
    <title>xterm</title>
</head>
<body>
<div class="container">
    <div id="terminal-container" style="height: 500px"></div>
</div>
<script>
    Terminal.applyAddon(fit);
    Terminal.applyAddon(terminado);
    Terminal.applyAddon(fullscreen);

    var term = new Terminal({
        debug: false,
        enableBold: false,
        bellStyle: "sound",
        fontFamily: '"DejaVu Sans Mono", "Everson Mono", FreeMono, Menlo, Terminal, monospace, Consolas',
        fontSize: 15,
        lineHeight: 1.0,
        letterSpacing: 0,
        screenKeys: true,
        useFlowControl: true,
        tabStopWidth: 4,
        cols: 80,
        rows: 20
    });

    var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    var socketURL = protocol + location.hostname + ((location.port) ? (':' + location.port) : '') + "/socket/xterm";
    var sock = new WebSocket(socketURL);
    sock.addEventListener('open', function () {
        // sock.send(JSON.stringify({type: "TERMINAL_RESIZE", columns: 200, rows: 30}));
        term.terminadoAttach(sock);
        term.fit();
        console.log(term);
    });
    term.open(document.getElementById('terminal-container'));
    term.toggleFullScreen();
</script>
</body>
</html>