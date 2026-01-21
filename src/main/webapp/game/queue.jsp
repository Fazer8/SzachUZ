<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <title data-i18n="queue.title"></title>
    <meta charset="UTF-8">
    <style>
        body {
            background-color: #f0f5f0;
            color: #1a3c1a;
            font-family: sans-serif;
            text-align: center;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            margin: 0;
        }

        h1 { margin-bottom: 20px; }

        .loader {
            border: 8px solid #cbdacb;
            border-top: 8px solid #2e5c2e;
            border-radius: 50%;
            width: 60px;
            height: 60px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
        }

        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

        #timer { font-size: 2rem; font-weight: bold; margin-top: 10px; }
        .status-msg { margin-top: 10px; font-size: 1.2rem; }

        /* Modal */
        #match-modal {
            display: none;
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.6);
            align-items: center; justify-content: center;
            z-index: 1000;
        }
        .modal-box {
            background: white; padding: 30px; border-radius: 10px;
            text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.2);
            border: 2px solid #2e5c2e;
            min-width: 300px;
        }

        #opponent-name {
            color: #2e5c2e;
            font-size: 1.5rem;
            margin: 10px 0;
            font-weight: bold;
        }

        .btn {
            padding: 10px 25px; font-size: 18px; cursor: pointer; margin: 10px;
            border: none; border-radius: 5px; color: white; font-weight: bold;
        }
        .btn-accept { background-color: #2e5c2e; }
        .btn-accept:hover { background-color: #1a3c1a; }
        .btn-decline { background-color: #a33; }
        #error-msg { color: red; display: none; margin-top: 20px;}
    </style>
</head>
<body>

<div id="queue-container">
    <h1 data-i18n="queue.searching"></h1>
    <div class="loader"></div>
    <div id="timer">00:00</div>
    <div class="status-msg"><span data-i18n="queue.mmr"></span>: <span id="mmr-range">+/- 20</span></div>
    <div class="status-msg" style="font-size: 0.9em; color: #666;"><span data-i18n="queue.noRefresh"></span></div>
    <div id="error-msg"></div>
    <button onclick="leaveQueue()"
            class="btn btn-decline"
            style="margin-top: 30px;"
            data-i18n="queue.cancel">
    </button>
</div>

<div id="match-modal">
    <div class="modal-box">
        <h2 data-i18n="queue.matchFound"></h2>
        <p data-i18n="queue.opponent"></p>

        <div id="opponent-name">???</div>

        <p data-i18n="queue.acceptTime"></p>
        <button onclick="acceptMatch()" class="btn btn-accept" data-i18n="queue.play"></button>
        <button onclick="leaveQueue()" class="btn btn-decline" data-i18n="queue.decline"></button>
    </div>
</div>

<script>
    let socket;
    let seconds = 0;
    let timerInt;
    let currentMatchId = null;

    const token = localStorage.getItem("authToken");

    if (!token) {
        //alert("Musisz być zalogowany!");
        window.location.href = "${pageContext.request.contextPath}/users/login.jsp";
    } else {
        startMatchmaking();
    }

    function startMatchmaking() {
        startTimer();
        const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
        const path = window.location.host + "${pageContext.request.contextPath}/ws/matchmaking?token=" + token;

        socket = new WebSocket(protocol + path);

        socket.onopen = function() { console.log("Połączono z kolejką"); };

        socket.onmessage = function(event) {
            const data = JSON.parse(event.data);
            console.log("Data:", data);

            if (data.type === "MATCH_PROPOSED") {
                // Przekazujemy nick do funkcji modala
                showAcceptModal(data.matchId, data.opponentName);
            }
            else if (data.type === "GAME_START") {
                window.location.href = "${pageContext.request.contextPath}/game/chess.jsp?gameId=" + data.matchId + "&color=" + data.color;
            }
        };

        socket.onclose = function(e) {
            stopTimer();
            if (e.code !== 1000) {
                document.getElementById("error-msg").innerText = translations["queue.disconnected"] || "Rozłączono z serwerem. Spróbuj ponownie.";
                document.getElementById("error-msg").style.display = "block";
            }
        };
    }

    function showAcceptModal(matchId, opponentName) {
        currentMatchId = matchId;

        // Wyświetlamy nick przeciwnika
        const nameElement = document.getElementById("opponent-name");
        nameElement.innerText =
            opponentName ? opponentName :
            (translations["queue.unknown"] || "Nieznany przeciwnik");

        document.getElementById("match-modal").style.display = "flex";
    }

    function acceptMatch() {
        if (socket && currentMatchId) {
            socket.send("ACCEPT:" + currentMatchId);
            document.querySelector(".btn-accept").innerText =
                translations["queue.waiting"] || "Czekanie...";
            document.querySelector(".btn-accept").disabled = true;
        }
    }

    function leaveQueue() {
        if (socket) socket.close();
        window.location.href = "${pageContext.request.contextPath}/";
    }

    function startTimer() {
        timerInt = setInterval(() => {
            seconds++;
            const m = Math.floor(seconds / 60).toString().padStart(2,'0');
            const s = (seconds % 60).toString().padStart(2,'0');
            document.getElementById("timer").innerText = m + ":" + s;

            if (seconds > 5) document.getElementById("mmr-range").innerText = "+/- 40";
            if (seconds > 10) document.getElementById("mmr-range").innerText = "+/- 60";
            if (seconds > 15) document.getElementById("mmr-range").innerText =
                                  translations["queue.mmr.wide"] || "SZEROKI";
        }, 1000);
    }

    function stopTimer() { clearInterval(timerInt); }
</script>
<script src="${pageContext.request.contextPath}/js/i18n.js"></script>
</body>
</html>