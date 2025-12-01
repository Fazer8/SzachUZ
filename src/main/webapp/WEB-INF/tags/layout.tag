<%@ tag description="Layout template" pageEncoding="UTF-8" %>
<%@ attribute name="page_name" required="true" %>
<%@ attribute name="head" fragment="true" required="false" %>
<%@ attribute name="body" fragment="true" required="true" %>

<html>
    <head>
        <title>${page_name}</title>
        <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/assets/logo_tmp.png">
        <jsp:invoke fragment="head" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
        <script>
            async function resolveTheme() {
                let theme = null;

                try {
                    const resp = await fetch("${pageContext.request.contextPath}/profile/me/");
                    if (resp.ok) {
                        const data = await resp.json();
                        if (data && (data.darkMode === true || data.theme === false)) {
                            theme = data.darkMode ? "dark" : "light";
                        }
                    }
                } catch (e) {
                    console.warn("Theme fetch failed.");
                }

                if (!theme) {
                    const local = localStorage.getItem("theme");
                    if (local === "dark" || local === "light") theme = local;
                }

                if (!theme) {
                    theme = window.matchMedia("(prefers-color-scheme: dark)").matches
                        ? "dark"
                        : "light";
                }

                if (!theme) theme = "light";

                applyTheme(theme);
            }

            function applyTheme(theme) {
                document.documentElement.classList.toggle("dark", theme === "dark");
            }

            // Run before page finishes loading
            document.addEventListener("DOMContentLoaded", resolveTheme);
        </script>
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

                        applyTheme(newTheme); // update instantly
                        localStorage.setItem("theme", newTheme);

                        // Save to server if user is logged in
                        try {
                            await fetch("${pageContext.request.contextPath}/profile/me/darkMode", {
                                method: "PUT",
                                headers: {"Content-Type": "application/json"},
                                body: JSON.stringify({theme: (newTheme === "dark")})
                            });
                        } catch (e) {
                            console.warn(e);
                        }
                    }
                </script>
            </div>
        </header>
        <hr class="header-split" />
        <jsp:invoke fragment="body" />
    </body>
</html>
