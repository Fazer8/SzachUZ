<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<t:layout page_name="training.title" block="guest">

    <jsp:attribute name="head">
        <style>
            .center-content {
                display: grid;
                place-items: center;
                min-height: 60vh;
            }

            .menu {
                display: flex;
                flex-direction: column;
                gap: 1.2em;
                width: 300px;
                text-align: center;
            }

            .section {
                border-bottom: 1px solid #ddd;
                padding-bottom: 1em;
            }

            .side-picker {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 0.5em;
            }

            .menu button {
                padding: 12px;
                font-size: 16px;
                cursor: pointer;
            }

            .side-picker button.selected {
                font-weight: bold;
                border: 2px solid #fff;
            }
        </style>
        <script>
            let selectedSide = "white";

            function pickSide(side) {
                selectedSide = side;
                document.getElementById("whiteBtn").classList.remove("selected");
                document.getElementById("blackBtn").classList.remove("selected");
                document.getElementById(side + "Btn").classList.add("selected");
            }
            async function startTraining(difficulty) {
                const TOKEN = localStorage.getItem("authToken");

                if (!TOKEN) {
                    alert("Not authenticated");
                    return;
                }

                try {
                    const res = await fetch("${pageContext.request.contextPath}/sv/training", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded",
                            "Authorization": "Bearer " + TOKEN
                        },
                        body: new URLSearchParams({
                            difficulty: difficulty,
                            side: selectedSide
                        })
                    });

                    if (!res.ok) {
                        const text = await res.text();
                        throw new Error(text);
                    }

                    const data = await res.json();

                    if (data.type !== "GAME_START") {
                        throw new Error("Unexpected response");
                    }

                    window.location.href =
                        "${pageContext.request.contextPath}/game/chess.jsp"
                        + "?gameId=" + encodeURIComponent(data.gameId)
                        + "&color=" + encodeURIComponent(data.color);

                } catch (e) {
                    console.error(e);
                    alert("Failed to start training game");
                }
            }
        </script>
    </jsp:attribute>

    <jsp:attribute name="body">
        <main class="site-margin border-color border-radius container center-content">
            <div class="menu">

                <div class="section">
                    <strong data-i18n="training.selectSide"></strong>
                    <div class="side-picker">
                        <button id="whiteBtn" class="selected" onclick="pickSide('white')" data-i18n="training.side.white"></button>

                        <button id="blackBtn" onclick="pickSide('black')" data-i18n="training.side.black"></button>
                    </div>
                </div>

                <div class="section">
                    <strong data-i18n="training.difficulty"></strong>
                    <button onclick="startTraining('easy')" data-i18n="training.easy"></button>

                    <button onclick="startTraining('hard')" data-i18n="training.hard"></button>
                </div>

                <button onclick="window.location.href='${pageContext.request.contextPath}/index.jsp'"
                        data-i18n="common.return">
                </button>
            </div>
            <script src="${pageContext.request.contextPath}/js/i18n.js"></script>
        </main>
    </jsp:attribute>
</t:layout>