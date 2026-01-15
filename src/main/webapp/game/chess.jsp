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



    </script>
    <style>
        main {
            display: grid;
            place-items: center;
            flex: 1 1 auto !important;
            align-content: stretch;
        }
        #board {
            aspect-ratio: 1 / 1;
            height: 70vh;
            width: 70vh;
            max-height: 70vh;
            max-width: 70vh;
        }
        .board-b72b1 {
            border: 20px solid var(--accent-color) !important;
            border-radius: 10px;
        }
        .white-1e1d7 {
            background-color: var(--dominant-color) !important;
        }

        .black-3c85d {
            background-color: var(--secondary-color) !important;
        }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
            <div id="board"></div>
        <script>
            const GAME_ID = "${param.gameId}";
            console.log(GAME_ID);
            const TOKEN   = localStorage.getItem("authToken");
            const COLOR   = "${param.color}".toLowerCase();
            console.log(COLOR);

            let socket = null;
            const wsUrl =
                (location.protocol === "https:" ? "wss://" : "ws://") +
                location.host +
                "/ws/chess?gameId=" + GAME_ID +
                "&token=" + TOKEN;

            socket = new WebSocket(wsUrl);

            socket.onopen = function () {
                console.log("Chess socket connected");
            };

            socket.onclose = function () {
                alert("Connection closed");
            };

            socket.onerror = function () {
                alert("WebSocket error");
            };

            var board = null
            var game = new Chess()

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

                    if (msg.status === "FINISHED") {
                        alert("Game over");
                    }
                    awaitingServer = false;
                }
            };

            let awaitingServer = false;

            function onDragStart(source, piece) {
                if (awaitingServer) return false;
                if (game.game_over()) return false;

                if ((COLOR === 'white' && piece.startsWith('b')) ||
                    (COLOR === 'black' && piece.startsWith('w'))) {
                    return false;
                }
            }

            function onDrop (source, target) {
                var move = game.move({
                    from: source,
                    to: target,
                    promotion: 'q'
                });

                if (move === null) return 'snapback';

                game.undo();
                awaitingServer = true;

                socket.send(JSON.stringify({
                    from: source,
                    to: target
                }));
            }
            // update the board position after the piece snap
            // for castling, en passant, pawn promotion
            function onSnapEnd () {
                board.position(game.fen())
            }
            var config = {
                draggable: true,
                position: 'start',
                orientation: COLOR,
                onDragStart: onDragStart,
                onDrop: onDrop,
                onSnapEnd: onSnapEnd,
                pieceTheme: '/assets/pieces/{piece}.png'
            }
            board = Chessboard('board', config)
        </script>
    </main>
    </jsp:attribute>
</t:layout>
