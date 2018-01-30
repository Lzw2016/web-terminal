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
<body style="margin: 0;padding: 0;overflow: hidden;cursor: text;user-select: none;background: black;">
<div id="terminal-container" style="margin: 0;padding: 0;position: absolute; width: 100%; height: 100%;"></div>

<script>
    Terminal.applyAddon(fit);
    Terminal.applyAddon(terminado);
    Terminal.applyAddon(fullscreen);

    var terminalContainer = document.getElementById('terminal-container');
    // cols: 80,
    // rows: 24,
    // convertEol: false,
    // termName: 'xterm',
    // cursorBlink: false,
    // cursorStyle: 'block',
    // bellSound: BellSound,
    // bellStyle: 'none',
    // enableBold: true,
    // fontFamily: 'courier-new, courier, monospace',
    // fontSize: 15,
    // lineHeight: 1.0,
    // letterSpacing: 0,
    // scrollback: 1000,
    // screenKeys: false,
    // debug: false,
    // cancelEvents: false,
    // disableStdin: false,
    // useFlowControl: false,
    // tabStopWidth: 8,
    // theme: null
    var term = new Terminal({
        cursorBlink: true,
        cursorStyle: 'underline', // block underline bar
        enableBold: false,
        bellStyle: "sound",
        fontFamily: '"DejaVu Sans Mono", "Everson Mono", FreeMono, Menlo, Terminal, monospace, Consolas',
        tabStopWidth: 4
    });

    var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    var socketURL = protocol + location.hostname + ((location.port) ? (':' + location.port) : '') + "/socket/xterm";
    var sock = new WebSocket(socketURL);
    sock.addEventListener('open', function () {
        term.terminadoAttach(sock);
        term.fit();
    });
    term.open(terminalContainer);
    term.toggleFullScreen();
    term.on("title", function (title) {
        if (!title) {
            title = 'xterm';
        } else {
            title = 'xterm' + title;
        }
        document.title = title;
    });

    window.onresize = function () {
        term.fit();
    };

    // 禁用右键菜单
    term.element.oncontextmenu = function (event) {
        event.returnValue = false;
    };

    // 复制内容
    var clipboard = null;

    // 右键按下 - 粘贴
    term.element.addEventListener("mousedown", function (e) {
        e.preventDefault();
        // 0-左键；1-滚轮；2-右键
        if (e.button === 2 && sock.readyState === WebSocket.OPEN && clipboard) {
            sock.send(JSON.stringify(["stdin", clipboard]));
        }
    });

    // 左键抬起 - 复制
    term.element.addEventListener("mouseup", function (e) {
        // 0-左键；1-滚轮；2-右键
        if (e.button === 0 && term.hasSelection()) {
            clipboard = term.getSelection();
        }
    });
</script>
</body>
</html>