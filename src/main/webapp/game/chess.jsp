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
        /* Układ główny - teraz tylko plansza na środku */
        main {
            display: flex !important;
            flex-direction: column; /* Zmiana na column dla wyśrodkowania */
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
            height: 70vh; /* Duża plansza */
            width: 70vh;
        }

        /* Kolory planszy i obramowanie */
        .board-b72b1 {
            border: 15px solid var(--accent-color) !important;
            border-radius: 8px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
        }
        .white-1e1d7 { background-color: var(--dominant-color) !important; }
        .black-3c85d { background-color: var(--secondary-color) !important; }

        /* Wskaźnik tury */
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

        /* Modal (Koniec gry) */
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
        <div id="game-status" style="display:none" class="overlay">
            <div class="modal-content">
                <h2 id="status-text" style="margin-bottom: 20px; font-size: 2rem;">Koniec Gry</h2>
                <button onclick="downloadPdf()" class="btn-modal btn-download">📥 Pobierz Historię (PDF)</button>
                <br><br>
                <button onclick="exitGame()" class="btn-modal btn-exit">🚪 Wyjdź z gry</button>
            </div>
        </div>

        <div class="game-container">
            <div id="turn-indicator" class="turn-indicator">Łączenie z serwerem...</div>
            <div id="board"></div>
        </div>

        <script>
            // Zmienne z JSP
            const GAME_ID = "${param.gameId}";
            const TOKEN   = localStorage.getItem("authToken");
            const COLOR   = "${param.color}".toLowerCase();
            const CONTEXT_PATH = "${pageContext.request.contextPath}";

            // Walidacja ID
            if (!GAME_ID || GAME_ID.trim() === "" || GAME_ID.startsWith("match_")) {
                alert("Błąd ID gry. Powrót do menu.");
                window.location.href = CONTEXT_PATH + "/";
            }

            // --- FUNKCJE OBSŁUGI GRY ---

            function downloadPdf() {
                // To zadziała, bo backend nadal ma historię w bazie
                window.location.href = CONTEXT_PATH + "/game/pdf?gameId=" + GAME_ID;
            }

            function exitGame() {
                // Powrót do strony głównej
                window.location.href = CONTEXT_PATH + "/";
            }

            function showResult(result) {
                const overlay = document.getElementById("game-status");
                const textField = document.getElementById("status-text");
                let text = "Koniec Gry";

                if (result === "DRAW") text = "🤝 Remis!";
                else if ((result === "WHITE_WON" && COLOR === "white") || (result === "BLACK_WON" && COLOR === "black")) {
                    text = "🏆 Zwycięstwo!";
                } else {
                    text = "💀 Porażka";
                }

                textField.innerText = text;
                overlay.style.display = "flex";
            }

            function updateTurnIndicator(sideToMove) {
                const el = document.getElementById("turn-indicator");
                let sidePl = sideToMove === "WHITE" ? "Białe" : "Czarne";

                if (sideToMove.toLowerCase() === COLOR) {
                    el.innerText = "Twój ruch (" + sidePl + ")";
                    el.style.border = "2px solid #4CAF50"; // Zielony
                    el.style.color = "#4CAF50";
                    board.draggable = true;
                } else {
                    el.innerText = "Ruch przeciwnika (" + sidePl + ")";
                    el.style.border = "2px solid #F44336"; // Czerwony
                    el.style.color = "#F44336";
                    board.draggable = false;
                }
            }

            // --- WEBSOCKET ---
            let socket = null;
            // Bezpieczne tworzenie URL socketa
            const wsProtocol = location.protocol === "https:" ? "wss://" : "ws://";
            const wsUrl = wsProtocol + location.host + CONTEXT_PATH + "/ws/chess?gameId=" + GAME_ID + "&token=" + TOKEN;

            socket = new WebSocket(wsUrl);

            socket.onopen = function () { console.log("Połączono z grą"); };
            socket.onclose = function (e) {
                if(e.code !== 1000 && !document.getElementById("game-status").style.display === "flex") {
                    console.log("Rozłączono");
                }
            };

            var board = null;
            var game = new Chess();
            let awaitingServer = false;

            socket.onmessage = function (event) {
                const msg = JSON.parse(event.data);

                if (msg.type === "ERROR") {
                    alert(msg.message);
                    awaitingServer = false;
                    return;
                }

                if (msg.fen) {
                    game.load(msg.fen);
                    board.position(msg.fen);
                    // Tutaj usunęliśmy wywołanie renderHistory(), bo tabela już nie istnieje

                    if (msg.status === "FINISHED") {
                        showResult(msg.result);
                    }

                    updateTurnIndicator(msg.sideToMove);
                    awaitingServer = false;
                }
            };

            // --- LOGIKA SZACHOWNICY ---
            function onDragStart(source, piece) {
                if (awaitingServer || game.game_over()) return false;
                if ((COLOR === 'white' && piece.startsWith('b')) || (COLOR === 'black' && piece.startsWith('w'))) return false;
            }

            function onDrop (source, target) {
                var move = game.move({ from: source, to: target, promotion: 'q' });
                if (move === null) return 'snapback';

                game.undo(); // Cofamy ruch lokalnie, czekamy na potwierdzenie z serwera
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
                // Używamy bezpiecznej ścieżki do obrazków
                pieceTheme: CONTEXT_PATH + '/assets/pieces/{piece}.png'
            }
            board = Chessboard('board', config);

            // Obsługa zmiany rozmiaru okna
            window.addEventListener('resize', board.resize);
        </script>
    </main>
    </jsp:attribute>
</t:layout>