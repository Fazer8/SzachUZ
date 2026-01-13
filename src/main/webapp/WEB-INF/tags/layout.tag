<%@ tag description="Layout template" pageEncoding="UTF-8" %>
<%@ attribute name="page_name" required="true" %>
<%@ attribute name="head" fragment="true" required="false" %>
<%@ attribute name="body" fragment="true" required="true" %>
<%@ attribute name="block" required="false" %>
<%@ taglib prefix="guard" tagdir="/WEB-INF/tags" %>

<html>
    <head>
        <title>${page_name}</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/logo_tmp.png">
        <script>
            async function authFetch(url, options = {}) {
                options.headers = {...(options.headers || {})};
                const token = localStorage.getItem("authToken");
                if (token) {
                    options.headers["Authorization"] = "Bearer " + token;
                }
                return fetch(url, options);
            }
        </script>
        <guard:block user="${block}"/>
        <jsp:invoke fragment="head" />
        <script>
            async function resolveTheme() {
                let theme = null;

                try {
                    const response = await authFetch("${pageContext.request.contextPath}/api/profile/me", {method: "GET"});
                    if (response.ok) {
                        let profile = await response.json()
                        let themeResponse = profile.darkMode
                        theme = themeResponse ? "dark" : "light";
                    } else {
                        console.warn("Status:", response.status);
                    }
                } catch (e) {
                    console.error("Error:", e);
                    showToast("Błąd połączenia z serwerem.", "error");
                }

                if (theme === null) {
                    const local = localStorage.getItem("theme");
                    if (local === "dark" || local === "light") theme = local;
                }

                if (theme === null) {
                    theme = window.matchMedia("(prefers-color-scheme: dark)").matches
                        ? "dark"
                        : "light";
                }

                if (theme === null) theme = "light";

                applyTheme(theme);
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
            <div>
                <button onClick="toggleTheme()">theme</button>
                <div>
                    <p>🇵🇱<br/>🇬🇧</p>

                </div>
                <script>
                    async function toggleTheme() {
                        const root = document.documentElement;
                        const isDark = root.classList.contains("dark");
                        const newTheme = isDark ? "light" : "dark";

                        applyTheme(newTheme);
                        localStorage.setItem("theme", newTheme);

                        try {
                            const res = await authFetch("${pageContext.request.contextPath}/api/profile/me/darkMode", {
                                method: "PUT",
                                headers: {"Content-Type": "application/json"},
                                body: JSON.stringify({darkMode: newTheme === "dark"})
                            });
                            if (res.ok) {
                                console.log("Tryb ciemny: " + ((newTheme === "dark") ? "Włączony" : "Wyłączony"), "info");
                            }
                        } catch (e) {
                            console.warn("Błąd wysyłania pliku." + e, "error");
                        }
                    }
                </script>
            </div>
        </header>
        <hr class="header-split" />
        <jsp:invoke fragment="body" />
    </body>
</html>
