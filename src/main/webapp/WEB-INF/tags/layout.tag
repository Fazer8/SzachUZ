<%@ tag description="Layout template" pageEncoding="UTF-8" %>
<%@ attribute name="page_name" required="true" %>
<%@ attribute name="head" fragment="true" required="false" %>
<%@ attribute name="body" fragment="true" required="true" %>
<%@ attribute name="block" required="false" %>
<%@ taglib prefix="guard" tagdir="/WEB-INF/tags" %>

<guard:block user="${block}"/>

<html>
<head>
    <title>${page_name}</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/logo_tmp.png">

    <style>
        .header-actions {
            display: flex;
            align-items: center;
            gap: 20px;
            margin-right: 15px;
        }

        .hidden {
            display: none !important;
        }

        .dropdown-container {
            position: relative;
        }

        .profile-trigger {
            display: inline-flex;
            align-items: center;
            gap: 0.6em;
            margin: 0;
            padding: 0.5em 0.9em;
            white-space: nowrap;
        }

        .profile-name {
            font-weight: bold;
            line-height: 1;
        }

        .profile-avatar {
            width: 2em;
            height: 2em;
            border-radius: 50%;
            object-fit: cover;
            border: 1px solid rgba(128, 128, 128, 0.3);
            flex-shrink: 0;
            display: block;
        }

        .dropdown-menu {
            position: absolute;
            top: 100%;
            right: 0;
            margin-top: 10px;
            background-color: var(--secondary-bg, #fff);
            border: 1px solid #ccc;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            z-index: 1000;
            min-width: 160px;
            flex-direction: column;
        }

        :root.dark .dropdown-menu {
            background-color: var(--gray-green);
            color: var(--text-color-main);
        }

        .dropdown-item {
            display: block;
            padding: 12px 16px;
            text-decoration: none;
            color: inherit;
            border-bottom: 1px solid rgba(128, 128, 128, 0.1);
            text-align: left;
            width: 100%;
            background: none;
            border: none;
            cursor: pointer;
            font-size: 1em;
            white-space: nowrap;
        }

        :root.dark .dropdown-item {
            color: inherit;
        }

        .dropdown-item:last-child {
            border-bottom: none;
        }

        .dropdown-item:hover {
            background-color: rgba(128, 128, 128, 0.1);
        }
    </style>

    <script>
        async function authFetch(url, options = {}) {
            options.headers = {...(options.headers || {})};
            const token = localStorage.getItem("authToken");
            if (token) options.headers["Authorization"] = "Bearer " + token;
            return fetch(url, options);
        }
    </script>

    <jsp:invoke fragment="head"/>

    <script>
        async function resolveTheme() {
            let theme = null;
            const isLoggedInServerSide = ${isLoggedIn};

            if (isLoggedInServerSide) {
                try {
                    const response = await authFetch("${pageContext.request.contextPath}/api/profile/me", {method: "GET"});
                    if (response.ok) {
                        let profile = await response.json();
                        let themeResponse = (profile.preferences?.darkMode) || (profile.darkMode) || false;
                        theme = themeResponse ? "dark" : "light";
                        fillUserData(profile);
                        renderUserView();
                    } else {
                        console.warn("Sesja wygasła - czyszczenie danych.");
                        clearSessionData();
                        renderGuestView();
                    }
                } catch (e) {
                    console.error("Błąd sieci:", e);
                }
            } else {
                if (localStorage.getItem("authToken")) localStorage.removeItem("authToken");
                renderGuestView();
            }

            if (theme === null) theme = localStorage.getItem("theme");
            if (theme === null) theme = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
            applyTheme(theme || "light");
        }

        function clearSessionData() {
            localStorage.removeItem("authToken");
            document.cookie = "authToken=; Max-Age=0; path=/";
        }

        async function fillUserData(profile) {
            const username = profile.user?.username || profile.username || "Użytkownik";
            const avatarFile = profile.preferences?.userAvatar || profile.userAvatar || "default_avatar.png";

            const nameEl = document.getElementById("nav-username");
            const avEl = document.getElementById("nav-avatar");

            if (nameEl) nameEl.innerText = username;

            if (avEl) {
                const url = "${pageContext.request.contextPath}/api/profile/avatars/" + avatarFile;
                try {
                    const response = await authFetch(url, {method: "GET"});

                    if (response.ok) {
                        const blob = await response.blob();
                        const objectUrl = URL.createObjectURL(blob);
                        avEl.src = objectUrl;
                    } else {
                        avEl.src = "${pageContext.request.contextPath}/assets/default_avatar.png";
                    }
                } catch (e) {
                    console.error(e);
                }
            }
        }

        function renderUserView() {
            document.getElementById("guest-view").classList.add("hidden");
            document.getElementById("user-view").classList.remove("hidden");
        }

        function renderGuestView() {
            document.getElementById("user-view").classList.add("hidden");
            document.getElementById("guest-view").classList.remove("hidden");
        }

        function toggleDropdown() {
            const menu = document.getElementById("user-dropdown");
            if (menu.classList.contains("hidden")) {
                menu.classList.remove("hidden");
                menu.style.display = "flex";
            } else {
                menu.classList.add("hidden");
                menu.style.display = "none";
            }
        }

        function doLogout() {
            clearSessionData();
            window.location.href = "${pageContext.request.contextPath}/users/login.jsp";
        }

        window.onclick = function (event) {
            if (!event.target.closest('#user-view')) {
                const menu = document.getElementById("user-dropdown");
                if (menu && !menu.classList.contains("hidden")) {
                    menu.classList.add("hidden");
                    menu.style.display = "none";
                }
            }
        }
        document.addEventListener("DOMContentLoaded", resolveTheme);

        function applyTheme(theme) {
            document.documentElement.classList.toggle("dark", theme === "dark");
        }
    </script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="main-bg">
<header class="border-radius secondary-bg site-margin">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/assets/logo_tmp.png" alt="SzachUZ logo" class="logo">
    </a>
    <h1>${page_name}</h1>

    <div class="header-actions">
        <button onClick="toggleTheme()" class="nav-btn">Motyw</button>
        <div><p style="margin: 0; line-height: 1.2;">🇵🇱<br/>🇬🇧</p></div>

        <div id="guest-view" class="${isLoggedIn ? 'hidden' : ''}">
            <button
                    class="nav-btn"
                    onclick="window.location.href='${pageContext.request.contextPath}/users/login.jsp'">
                Zaloguj się
            </button>
        </div>

        <div id="user-view" class="dropdown-container ${!isLoggedIn ? 'hidden' : ''}">
            <button class="profile-trigger" onclick="toggleDropdown()">
                <span id="nav-username" class="profile-name"></span>
                <img id="nav-avatar" src="" alt="A" class="profile-avatar">
            </button>
            <div id="user-dropdown" class="dropdown-menu hidden" style="display: none;">
                <a href="${pageContext.request.contextPath}/users/userProfile.jsp" class="dropdown-item">Profil
                    użytkownika</a>
                <a onclick="doLogout()" class="dropdown-item">Wyloguj się</a>
            </div>
        </div>

        <script>
            async function toggleTheme() {
                const root = document.documentElement;
                const newTheme = root.classList.contains("dark") ? "light" : "dark";
                applyTheme(newTheme);
                localStorage.setItem("theme", newTheme);
                try {
                    if (${isLoggedIn}) {
                        await authFetch("${pageContext.request.contextPath}/api/profile/me/darkMode", {
                            method: "PUT", headers: {"Content-Type": "application/json"},
                            body: JSON.stringify({darkMode: newTheme === "dark"})
                        });
                    }
                } catch (e) {
                    console.warn(e);
                }
            }
        </script>
    </div>
</header>
<hr class="header-split"/>
<jsp:invoke fragment="body"/>
</body>
</html>