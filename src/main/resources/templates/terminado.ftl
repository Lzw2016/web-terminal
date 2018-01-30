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
        scrollback: 10000,
        screenKeys: true,
        useFlowControl: true,
        tabStopWidth: 4,
        cols: 80,
        rows: 20,
        cursorBlink: true,
        cursorStyle: 'bar' // block underline bar
    });

    var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    var socketURL = protocol + location.hostname + ((location.port) ? (':' + location.port) : '') + "/socket/xterm";
    var sock = new WebSocket(socketURL);
    sock.addEventListener('open', function () {
        term.terminadoAttach(sock);
        term.fit();
    });
    term.open(document.getElementById('terminal-container'));
    // term.toggleFullScreen();

    term.on("title", function (title) {
        if (!title) {
            title = 'xterm';
        } else {
            title = 'xterm' + title;
        }
        document.title = title;
    });

    term.on("resize", function (data) {
        term.fit();
    });

    // term.on("key", function (key, e) {
    //     console.log(key, e);
    // });

    // 右键按下 - 粘贴
    term.element.addEventListener("mousedown", function (e) {
        // 0-左键；1-滚轮；2-右键
        if (e.button === 2) {
            console.log(term.getSelection());
        }
    });

    // 左键抬起 - 复制
    term.element.addEventListener("mouseup", function (e) {
        // 0-左键；1-滚轮；2-右键
        if (e.button === 0) {
            console.log(term.getSelection());
        }
    });

</script>
</body>
</html>