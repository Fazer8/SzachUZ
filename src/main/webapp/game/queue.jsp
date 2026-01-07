<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pl">
<head>
    <title>Szukanie Przeciwnika - SzachUZ</title>
    <meta charset="UTF-8">
    <style>
        /* Prosty styl pasujący do motywu ze screena */
        body {
            background-color: #f0f5f0; /* Jasna zieleń */
            color: #1a3c1a; /* Ciemna zieleń */
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
            border-top: 8px solid #2e5c2e; /* Ciemny zielony loading */
            border-radius: 50%;
            width: 60px;
            height: 60px;
            animation: spin 1s linear infinite;
            margin: 20px auto;
        }

        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

        #timer { font-size: 2rem; font-weight: bold; margin-top: 10px; }

        .status-msg { margin-top: 10px; font-size: 1.2rem; }

        /* Modal (Popup) */
        #match-modal {
            display: none;
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background-color: rgba(0,0,0,0.6);
            align-items: center; justify-content: center;
        }
        .modal-box {
            background: white; padding: 30px; border-radius: 10px;
            text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.2);
            border: 2px solid #2e5c2e;
        }
        .btn {
            padding: 10px 25px; font-size: 18px; cursor: pointer; margin: 10px;
            border: none; border-radius: 5px; color: white; font-weight: bold;
        }
        .btn-accept { background-color: #2e5c2e; } /* Zielony */
        .btn-accept:hover { background-color: #1a3c1a; }

        .btn-decline { background-color: #a33; } /* Czerwony */

        #error-msg { color: red; display: none; margin-top: 20px;}
    </style>
</head>
<body>

<div id="queue-container">
    <h1>Szukanie przeciwnika...</h1>
    <div class="loader"></div>
    <div id="timer">00:00</div>
    <div class="status-msg">Zakres MMR: <span id="mmr-range">+/- 30</span></div>
    <div class="status-msg" style="font-size: 0.9em; color: #666;">Proszę nie odświeżać strony.</div>
    <div id="error-msg"></div>
    <button onclick="leaveQueue()" class="btn btn-decline" style="margin-top: 30px;">Anuluj</button>
</div>

<div id="match-modal">
    <div class="modal-box">
        <h2>Mecz Znaleziony!</h2>
        <p>Przeciwnik czeka. Masz 10 sekund.</p>
        <button onclick="acceptMatch()" class="btn btn-accept">GRAJ</button>
        <button onclick="leaveQueue()" class="btn btn-decline">ODRZUĆ</button>
    </div>
</div>

<script>
    let socket;
    let seconds = 0;
    let timerInt;
    let currentMatchId = null;

    // 1. Pobierz token
    const token = localStorage.getItem("authToken");

    // 2. Zabezpieczenie: Jak nie ma tokena, wywal do logowania
    if (!token) {
        alert("Musisz być zalogowany!");
        window.location.href = "${pageContext.request.contextPath}/users/login.jsp";
    } else {
        startMatchmaking();
    }

    function startMatchmaking() {
        startTimer();

        // Budowanie URL do WebSocketa
        const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
        // Jeśli twoja aplikacja nie nazywa się "szachuz" w URL, usuń "/szachuz" poniżej
        const path = window.location.host + "${pageContext.request.contextPath}/ws/matchmaking?token=" + token;

        socket = new WebSocket(protocol + path);

        socket.onopen = function() {
            console.log("Połączono z kolejką");
        };

        socket.onmessage = function(event) {
            const data = JSON.parse(event.data);
            console.log("Data:", data);

            if (data.type === "MATCH_PROPOSED") {
                showAcceptModal(data.matchId);
            }
            else if (data.type === "GAME_START") {
                // Przekierowanie do gry
                window.location.href = "${pageContext.request.contextPath}/game/chess.jsp?gameId=" + data.matchId + "&color=" + data.color;
            }
        };

        socket.onclose = function(e) {
            console.log("Rozłączono", e);
            stopTimer();
            if (e.code !== 1000) { // 1000 to normalne zamknięcie
                document.getElementById("error-msg").innerText = "Rozłączono z serwerem. Spróbuj ponownie.";
                document.getElementById("error-msg").style.display = "block";
            }
        };
    }

    function showAcceptModal(matchId) {
        currentMatchId = matchId;
        document.getElementById("match-modal").style.display = "flex";
    }

    function acceptMatch() {
        if (socket && currentMatchId) {
            socket.send("ACCEPT:" + currentMatchId);
            document.querySelector(".btn-accept").innerText = "Czekanie...";
            document.querySelector(".btn-accept").disabled = true;
        }
    }

    function leaveQueue() {
        if (socket) socket.close();
        window.location.href = "${pageContext.request.contextPath}/"; // Powrót na główną
    }

    function startTimer() {
        timerInt = setInterval(() => {
            seconds++;
            const m = Math.floor(seconds / 60).toString().padStart(2,'0');
            const s = (seconds % 60).toString().padStart(2,'0');
            document.getElementById("timer").innerText = m + ":" + s;

            // Symulacja pokazywania zakresu (tylko wizualnie dla usera, backend robi to naprawdę)
            if (seconds > 5) document.getElementById("mmr-range").innerText = "+/- 40";
            if (seconds > 10) document.getElementById("mmr-range").innerText = "+/- 50";
            if (seconds > 15) document.getElementById("mmr-range").innerText = "SZEROKI";

        }, 1000);
    }

    function stopTimer() { clearInterval(timerInt); }
</script>
</body>
</html>