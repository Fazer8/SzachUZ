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
        main div {
            display: flex;
            flex-direction: column;
            gap: 1em;
        }
        main div div {
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
        }
        .right-panel div {
            width: 80vw;
        }
        ol {
             list-style-position: inside;
        }

        @media only screen and (min-width: 670px) {
            main {
                grid-template-columns: 1fr 1fr;
            }
            .left-panel * img {
                max-width: 40vw;
            }
            .right-panel div {
                width: 30vw;
            }
        }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
        <div class="left-panel">
            <div class="secondary-bg">
                <img src="${pageContext.request.contextPath}/assets/chess-image-become-king.png" alt="Zdjęcie pionków w rzędach, z królem pomiędzy nimi.">
                <p>Zostań królem SzachUZ</p>
            </div>
            <button  type="button" onclick="window.location.href='users/login.jsp'">Zagraj teraz</button>
        </div>
        <div class="right-panel">
            <div class="secondary-bg">
                <p>Ranking najlepszych graczy</p>
                <ol>
                    <li>LoremFox</li>
                    <li>ipsum_Byte</li>
                    <li>Dolor-99</li>
                    <li>SitNova_42</li>
                    <li>Amet-Core</li>
                    <li>elitShade</li>
                    <li>Tempus_One</li>
                    <li>Magna-Code7</li>
                    <li>nullAsync</li>
                    <li>Gravida-X</li>
                </ol>
            </div>
            <div class="secondary-bg">
                <p>
                    Rywalizuj ze znajomymi<br>lub<br>Trenuj samemu
                </p>
            </div>
        </div>
    </main>
    <footer style="display: flex; gap: 10px;">
        <p>Development links: </p>
        <a href="hello-servlet">Hello Servlet</a>
        <a href="users/userProfile.jsp">Profil Użytkownika</a>
        <a href="users/login.jsp">Zaloguj Się</a>
        <a href="users/register.jsp">Zarejestruj Się</a>
    </footer>
    </jsp:attribute>
</t:layout>
