<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="game.title">
    <jsp:attribute name="head">
    <link rel="stylesheet"
          href="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.css"
          integrity="sha384-q94+BZtLrkL1/ohfjR8c6L+A6qzNH9R2hBLwyoAfu3i/WCvQjzL2RQJ3uNHDISdU"
          crossorigin="anonymous">

    <script src="https://code.jquery.com/jquery-3.5.1.min.js"
            integrity="sha384-ZvpUoO/+PpLXR1lu4jmpXWu80pZlYUAfxl5NsBMWOEPSjUn/6Z/hRTt8+pR6L4N2"
            crossorigin="anonymous"></script>
    <script src="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.js"
            integrity="sha384-8Vi8VHwn3vjQ9eUHUxex3JSN/NFqUg3QbPyX8kWyb93+8AC/pPWTzj+nHtbC5bxD"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chess.js/0.10.3/chess.min.js"></script>

    <style>

        main {
            display: flex !important;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 20px;
            width: 100%;
            min-height: 80vh;
        }

        .game-container {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        #board {
            aspect-ratio: 1 / 1;
            height: 70vh;
            width: 70vh;
        }


        .board-b72b1 {
            border: 15px solid var(--accent-color) !important;
            border-radius: 8px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
        }
        .white-1e1d7 { background-color: var(--dominant-color) !important; }
        .black-3c85d { background-color: var(--secondary-color) !important; }


        .turn-indicator {
            margin-bottom: 20px;
            font-weight: bold;
            font-size: 1.5rem;
            padding: 10px 30px;
            border-radius: 8px;
            background: rgba(0,0,0,0.3);
            color: white;
            text-align: center;
            min-width: 300px;
        }


        .overlay {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.85);
            display: flex; justify-content: center; align-items: center;
            z-index: 1000; backdrop-filter: blur(5px);
        }
        .modal-content {
            background: #222; padding: 40px; border-radius: 15px;
            text-align: center; border: 2px solid var(--accent-color); color: white;
            box-shadow: 0 0 20px rgba(0,0,0,0.7);
        }
        .btn-modal {
            margin: 10px; padding: 12px 24px; font-size: 1.1rem; border: none;
            border-radius: 5px; cursor: pointer; transition: transform 0.2s;
            font-weight: bold;
        }
        .btn-download { background-color: #2e7d32; color: white; }
        .btn-exit { background-color: #c62828; color: white; }
        .btn-modal:hover { transform: scale(1.05); filter: brightness(1.1); }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">

        <div class="game-container">
            <div id="game-status" style="display:none" class="overlay">
                <p id="status-text"></p>
                <button onclick="downloadPdf()" data-i18n="game.history"></button>
                <button onclick="exitGame()" data-i18n="game.exit"></button>
            </div>
            <div id="turn-indicator" class="turn-indicator"></div>
            <div>
              <span data-i18n="game.whiteTime"></span>: <span id="whiteClock"></span>
              <span data-i18n="game.blackTime"></span>: <span id="blackClock"></span>
            </div>
            <button onclick="forfeit()" data-i18n="game.forfeit"></button>
            <div id="board"></div>
        </div>

        <script>
            const GAME_ID = "${param.gameId}";
            const TOKEN   = localStorage.getItem("authToken");
            const COLOR   = "${param.color}".toLowerCase();
            const CONTEXT_PATH = "${pageContext.request.contextPath}";

            if (!GAME_ID || GAME_ID.trim() === "" || GAME_ID.startsWith("match_")) {
                alert("Błąd ID gry. Powrót do menu.");
                window.location.href = CONTEXT_PATH + "/";
            }


            function downloadPdf() {
                window.location.href = CONTEXT_PATH + "/game/pdf?gameId=" + GAME_ID;
            }

            function exitGame() {
                window.location.href = CONTEXT_PATH + "/";
            }

            function showResult(result, status) {
                const overlay = document.getElementById("game-status");
                const textField = document.getElementById("status-text");

                let text;
                if (result === "DRAW") {
                    text = translations["game.result.draw"] || "Draw";
                } else if (
                    (result === "WHITE_WON" && COLOR === "white") ||
                    (result === "BLACK_WON" && COLOR === "black")
                ) {
                    text = translations["game.result.win"] || "Wygrałeś";
                } else {
                    text = translations["game.result.lose"] || "Przegrałeś";
                }

                if (status === "FORFEIT") {
                    text += " " + (translations["game.result.forfeit"] || "przez walkower");
                }

                text += "!";

                textField.innerText = text;
                overlay.style.display = "flex";
            }

            function updateTurnIndicator(sideToMove) {
                const el = document.getElementById("turn-indicator");
                let sidePl = sideToMove === "WHITE" ? "Białe" : "Czarne";

                if (sideToMove.toLowerCase() === COLOR) {
                    el.innerText =
                        (translations["game.turn.yours"] || "Twój ruch") +
                        " (" + sideToMove + ")";
                    el.style.color = "green";
                    board.draggable = true;
                } else {
                    el.innerText =
                        (translations["game.turn.enemy"] || "Ruch przeciwnika") +
                        " (" + sideToMove + ")";
                    el.style.color = "gray";
                    board.draggable = false;
                }
            }

            function renderClock(ms) {
                let s = Math.max(0, Math.floor(ms / 1000));
                return Math.floor(s / 60) + ":" + (s % 60).toString().padStart(2, "0");
            }

            function updateClock(msg) {
                document.getElementById("whiteClock").innerText = renderClock(msg.timeRemaining.white);
                document.getElementById("blackClock").innerText = renderClock(msg.timeRemaining.black);
            }

            function forfeit() {
                let res = confirm(
                    translations["game.confirm.forfeit"] || "Opuścić grę?"
                );
                if (res) {
                    socket.send(JSON.stringify({type: "FORFEIT"}));
                }
            }

            let socket = null;
            const wsProtocol = location.protocol === "https:" ? "wss://" : "ws://";
            const wsUrl = wsProtocol + location.host + CONTEXT_PATH + "/ws/chess?gameId=" + GAME_ID + "&token=" + TOKEN;

            socket = new WebSocket(wsUrl);

            socket.onopen = function () { console.log("Połączono z grą"); };
            

            socket.onclose = function () {
                //alert("Connection closed");
                window.location.href = "/index.jsp";
            };

            var board = null;
            var game = new Chess();
            let awaitingServer = false;

            socket.onmessage = function (event) { try {
                const msg = JSON.parse(event.data);

                if (msg.type === "ERROR") {
                    console.log(msg.message);
                    awaitingServer = false;
                    return;
                }

                if (msg.type === "TIME_TICK") {
                    updateClock(msg);
                }

                if (msg.fen) {
                    game.load(msg.fen);
                    board.position(msg.fen);

                    if (msg.status === "FINISHED") {
                       showResult(msg.result, msg.status);
                    } else if (msg.status == "FORFEIT") {
                       showResult(msg.result, msg.status);
                    }

                    updateClock(msg);

                    updateTurnIndicator(msg.sideToMove);
                    awaitingServer = false;
                }
            } catch (error) {
                console.log(event);
            }};

            function onDragStart(source, piece) {
                if (awaitingServer || game.game_over()) return false;
                if ((COLOR === 'white' && piece.startsWith('b')) || (COLOR === 'black' && piece.startsWith('w'))) return false;
            }

            function onDrop (source, target) {
                var move = game.move({ from: source, to: target, promotion: 'q' });
                if (move === null) return 'snapback';

                game.undo();
                awaitingServer = true;
                socket.send(JSON.stringify({ from: source, to: target }));
            }

            function onSnapEnd () { board.position(game.fen()) }

            var config = {
                draggable: true,
                position: 'start',
                orientation: COLOR,
                onDragStart: onDragStart,
                onDrop: onDrop,
                onSnapEnd: onSnapEnd,

                pieceTheme: CONTEXT_PATH + '/assets/pieces/{piece}.png'
            }
            board = Chessboard('board', config);

            window.addEventListener('resize', board.resize);
        </script>
        <script src="${pageContext.request.contextPath}/js/i18n.js"></script>
    </main>
    </jsp:attribute>
</t:layout>