<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="Szachy">
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
            flex-direction: row;
            justify-content: center;
            align-items: flex-start;
            gap: 30px;
            padding: 20px;
            width: 100%;
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
            box-shadow: 0 10px 20px rgba(0,0,0,0.3);
        }
        .white-1e1d7 { background-color: var(--dominant-color) !important; }
        .black-3c85d { background-color: var(--secondary-color) !important; }

        /* --- STYLIZACJA HISTORII --- */
        .history-panel {
            width: 320px;
            height: 70vh;
            background: rgba(255, 255, 255, 0.05);
            border: 2px solid var(--accent-color);
            border-radius: 8px;
            display: flex;
            flex-direction: column;
            padding: 10px;
            color: inherit;
        }

        .players-header {
            display: flex;
            justify-content: space-between;
            padding-bottom: 10px;
            margin-bottom: 10px;
            border-bottom: 1px solid var(--accent-color);
            font-size: 0.9rem;
        }
        .player-badge {
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .history-list {
            flex-grow: 1;
            overflow-y: auto;
            padding-right: 5px;
        }
        #history-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.9rem;
        }
        #history-table th, #history-table td {
            padding: 6px;
            text-align: center;
        }
        #history-table tr:nth-child(even) {
            background-color: rgba(255, 255, 255, 0.1);
        }

        /* Ikony w historii */
        .mini-piece {
            width: 18px;
            height: 18px;
            vertical-align: middle;
            margin-right: 4px;
        }
        .move-content {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        /* Inne elementy */
        .turn-indicator {
            margin-bottom: 10px;
            font-weight: bold;
            font-size: 1.2rem;
            padding: 10px 20px;
            border-radius: 5px;
            background: rgba(0,0,0,0.2);
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
        }
        .btn-modal {
            margin: 10px; padding: 10px 20px; font-size: 1rem; border: none;
            border-radius: 5px; cursor: pointer; transition: transform 0.2s;
        }
        .btn-download { background-color: #2e7d32; color: white; }
        .btn-exit { background-color: #c62828; color: white; }
        .btn-modal:hover { transform: scale(1.05); }

        .history-list::-webkit-scrollbar { width: 8px; }
        .history-list::-webkit-scrollbar-thumb { background: var(--accent-color); border-radius: 4px; }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
        <div id="game-status" style="display:none" class="overlay">
            <div class="modal-content">
                <h2 id="status-text" style="margin-bottom: 20px;">Koniec Gry</h2>
                <button onclick="downloadPdf()" class="btn-modal btn-download">📥 Pobierz Historię (PDF)</button>
                <br>
                <button onclick="exitGame()" class="btn-modal btn-exit">Wyjdź z gry</button>
            </div>
        </div>

        <div class="game-container">
            <div id="turn-indicator" class="turn-indicator">Łączenie...</div>
            <div id="board"></div>
        </div>

        <div class="history-panel">
            <div class="players-header">
                <div class="player-badge">
                    <img src="${pageContext.request.contextPath}/assets/pieces/wK.png" width="20">
                    <span>Białe</span>
                </div>
                <div class="player-badge">
                    <span>Czarne</span>
                    <img src="${pageContext.request.contextPath}/assets/pieces/bK.png" width="20">
                </div>
            </div>

            <div class="history-list">
                <table id="history-table">
                    <thead>
                    <tr>
                        <th style="width: 15%">#</th>
                        <th style="width: 42%">Białe</th>
                        <th style="width: 42%">Czarne</th>
                    </tr>
                    </thead>
                    <tbody id="history-body"></tbody>
                </table>
            </div>
        </div>

        <script>
            const GAME_ID = "${param.gameId}";
            const TOKEN   = localStorage.getItem("authToken");
            const COLOR   = "${param.color}".toLowerCase();

            // Strażnik ID
            if (!GAME_ID || GAME_ID.trim() === "" || GAME_ID.startsWith("match_")) {
                alert("Błąd ID gry. Powrót do menu.");
                window.location.href = "/";
            }

            function downloadPdf() {
                window.location.href = "${pageContext.request.contextPath}/game/pdf?gameId=" + GAME_ID;
            }

            function showResult(result) {
                const overlay = document.getElementById("game-status");
                const textField = document.getElementById("status-text");
                let text = "Koniec Gry";
                if (result === "DRAW") text = "Remis!";
                else if ((result === "WHITE_WON" && COLOR === "white") || (result === "BLACK_WON" && COLOR === "black")) text = "Wygrana! 🎉";
                else text = "Porażka. 💀";

                textField.innerText = text;
                overlay.style.display = "flex";
            }

            function updateTurnIndicator(sideToMove) {
                const el = document.getElementById("turn-indicator");
                let sidePl = sideToMove === "WHITE" ? "Białe" : "Czarne";
                if (sideToMove.toLowerCase() === COLOR) {
                    el.innerText = "Twój ruch (" + sidePl + ")";
                    el.style.border = "2px solid green";
                    board.draggable = true;
                } else {
                    el.innerText = "Ruch przeciwnika (" + sidePl + ")";
                    el.style.border = "2px solid red";
                    board.draggable = false;
                }
            }

            // --- NOWA FUNKCJA RENDERUJĄCA Z IKONAMI ---
            function getPieceIcon(color, type) {
                // Konwersja: 'n' -> 'N'
                const pieceChar = type.toUpperCase();
                // Ścieżka do obrazka
                return "${pageContext.request.contextPath}/assets/pieces/" + color + pieceChar + ".png";
            }

            function renderHistory() {
                // verbose: true daje nam obiekty { color: 'w', piece: 'n', san: 'Nf3', ... }
                const historyArray = game.history({ verbose: true });
                const tbody = document.getElementById("history-body");
                tbody.innerHTML = "";

                for (let i = 0; i < historyArray.length; i += 2) {
                    const moveNum = (i / 2) + 1;
                    const white = historyArray[i];
                    const black = historyArray[i + 1];

                    const row = document.createElement("tr");

                    let html = "<td>" + moveNum + ".</td>";

                    // Białe
                    html += "<td>";
                    if(white) {
                        html += `<div class="move-content">
                                    <img src="\${getPieceIcon('w', white.piece)}" class="mini-piece">
                                    <span>\${white.from} &#8594; \${white.to}</span>
                                 </div>`;
                    }
                    html += "</td>";

                    // Czarne
                    html += "<td>";
                    if(black) {
                        html += `<div class="move-content">
                                    <img src="\${getPieceIcon('b', black.piece)}" class="mini-piece">
                                    <span>\${black.from} &#8594; \${black.to}</span>
                                 </div>`;
                    }
                    html += "</td>";

                    row.innerHTML = html;
                    tbody.appendChild(row);
                }
                const container = document.querySelector(".history-list");
                container.scrollTop = container.scrollHeight;
            }

            // WebSocket
            let socket = null;
            const wsUrl = (location.protocol === "https:" ? "wss://" : "ws://") + location.host + "/ws/chess?gameId=" + GAME_ID + "&token=" + TOKEN;
            socket = new WebSocket(wsUrl);

            socket.onopen = function () { console.log("Połączono z grą"); };
            socket.onclose = function (e) {
                if(e.code !== 1000) alert("Rozłączono z serwerem. Odśwież stronę lub wróć do menu.");
            };

            var board = null;
            var game = new Chess();
            let awaitingServer = false;

            socket.onmessage = function (event) {
                const msg = JSON.parse(event.data);
                if (msg.type === "ERROR") { alert(msg.message); awaitingServer = false; return; }

                if (msg.fen) {
                    game.load(msg.fen);
                    board.position(msg.fen);

                    // Odświeżamy historię (teraz z ikonami)
                    renderHistory();

                    if (msg.status === "FINISHED") showResult(msg.result);
                    updateTurnIndicator(msg.sideToMove);
                    awaitingServer = false;
                }
            };

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
                pieceTheme: 'https://chessboardjs.com/img/chesspieces/wikipedia/{piece}.png'
            }
            board = Chessboard('board', config)
        </script>
    </main>
    </jsp:attribute>
</t:layout>