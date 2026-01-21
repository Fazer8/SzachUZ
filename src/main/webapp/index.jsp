<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="Strona główna">
    <jsp:attribute name="head">
    <script>
        function handlePlayClick() {
            // 1. Sprawdzamy, czy w przeglądarce jest zapisany token
            // Upewnij się, że przy logowaniu zapisujesz go jako "token" (lub zmień tutaj nazwę klucza)
            const token = localStorage.getItem("authToken");

            console.log("Sprawdzam token:", token ? "Jest token" : "Brak tokena");

            if (token) {
                // JEST ZALOGOWANY -> Idziemy do poczekalni (queue.jsp)
                // Używamy contextPath, żeby link był poprawny niezależnie od nazwy pliku .war
                window.location.href = "${pageContext.request.contextPath}/game/queue.jsp";
            } else {
                // NIE JEST ZALOGOWANY -> Idziemy do logowania
                window.location.href = "${pageContext.request.contextPath}/users/login.jsp";
            }
        }
    </script>
    <style>
        main {
            display: grid;
            grid-template-columns: 1fr;
            place-items: center;
            gap: 1em;
        }
        main > div {
            display: flex;
            flex-direction: column;
            gap: 1em;
        }
        main > div > div {
            padding: 20px;
            border-radius: 20px;
        }
        .left-panel * img {
            max-width: 80vw;
        }
        p {
            text-align: center;
        }
        .left-panel button {
            margin: 0 10vw;

            /* Dodałem kursor rączki, żeby było widać, że to przycisk */
            cursor: pointer;
        }
        .right-panel div {
            width: 80vw;
        }

        @media only screen and (min-width: 670px) {
            main {
                grid-template-columns: 1fr 1fr;
            }
            .left-panel * img {
                max-width: 40vw;
            }
            .right-panel > div {
                width: 30vw;
            }
        }
    </style>
    <script src="${pageContext.request.contextPath}/js/i18n.js"></script>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
        <div class="left-panel">
            <div class="secondary-bg">
                <img src="${pageContext.request.contextPath}/assets/chess-image-become-king.png" data-i18n="image.alt.king">
                <p data-i18n="home.becomeKing"></p>
            </div>


        </div>
        <div class="right-panel">
            <div class="secondary-bg">
                <p data-i18n="home.leaderboard"></p>
                <t:leaderboard />
            </div>
            <div class="secondary-bg-1">
                <button type="button" onclick="handlePlayClick()" data-i18n="button.playOnline" style="padding:15px 32px"></button>
                <p data-i18n="common.or"></p>
                <button type="button"
                        onclick="location.href = '${pageContext.request.contextPath}/game/training.jsp'"
                        style="padding:15px 32px"
                        data-i18n="button.train">
                </button>
            </div>
        </div>
    </main>
    <footer style="display: flex; gap: 10px;">
        <p data-i18n="footer.devLinks"></p>
        <a href="hello-servlet">Hello Servlet</a>
        <a href="users/userProfile.jsp"data-i18n="footer.profile"></a>
        <a href="users/login.jsp"data-i18n="footer.login"></a>
        <a href="users/register.jsp"data-i18n="footer.register"></a>

        <button onclick="setLang('pl')">PL</button>
        <button onclick="setLang('en')">EN</button>
    </footer>
    </jsp:attribute>
</t:layout>
