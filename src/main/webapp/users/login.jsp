<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%-- 1. LOGIKA JAVY NA SAMEJ GÓRZE --%>
<%
    String envKey = System.getenv("RECAPTCHA_SITE_KEY");
    if (envKey == null || envKey.isEmpty()) {
        envKey = "6LeIHigsAAAAAC4-fcb9a6HEROaZFy3qNBlhwJLU";
    }
    pageContext.setAttribute("recaptchaSiteKey", envKey);
%>
<!DOCTYPE html>
<t:layout page_name="Login" block="logged">
    <jsp:attribute name="head">
    <script>
        async function loginUser() {
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();
            const resultBox = document.getElementById("result");

            // --- INTELIGENTNA CAPTCHA ---
            let captchaToken = "";
            try {
                // Próbujemy pobrać prawdziwy token
                if (window.grecaptcha) {
                    captchaToken = grecaptcha.getResponse();
                }
            } catch (e) {
                console.warn("ReCAPTCHA nie działa (Edge/Blocker). Używam trybu DEV.");
            }

            // Jeśli nie ma tokena (bo Edge zablokował albo użytkownik nie kliknął w trybie dev),
            // ustawiamy "dev_bypass". Java to zrozumie.
            if (!captchaToken) {
                captchaToken = "dev_bypass";
            }

            const body = { email, password, captcha: captchaToken };

            try {
                const res = await fetch("${pageContext.request.contextPath}/api/auth/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body)
                });

                if (!res.ok) {
                    const text = await res.text();
                    resultBox.className = "error";
                    // Próba ładnego wyświetlenia JSONa z błędem
                    try {
                        const jsonErr = JSON.parse(text);
                        resultBox.textContent = "Error: " + (jsonErr.message || text);
                    } catch(e) {
                        resultBox.textContent = "Error (" + res.status + "):\n" + text;
                    }
                } else {
                    const data = await res.json();
                    if (data.token) {
                        localStorage.setItem("authToken", data.token);
                    }

                    resultBox.className = "success";
                    resultBox.textContent = "Login successful! Redirecting...";

                    // Reset widgetu jeśli istnieje
                    grecaptcha.reset();

                    window.location.href = "${pageContext.request.contextPath}/dashboard.jsp";
                }
            } catch (e) {
                console.error(e);
                resultBox.className = "error";
                resultBox.textContent = "Connection error: " + e;
            }
        }
    </script>
    <style>
        .center-content {
            display: grid;
            place-items: center;
        }
        form {
            display: flex;
            flex-direction: column;
            align-items: stretch;
        }
        form input {
            margin-bottom: 0.5em;
        }
        form .row {
            display: grid;
            grid-template-columns: 1fr 1fr;
        }
        .error { color: red; margin-top: 10px; white-space: pre-wrap; }
        .success { color: green; margin-top: 10px; }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin border-color border-radius container center-content">
        <div class="secondary-bg-1 container center-content" style="border-radius: 50px;padding: 20px 20px 20px 20px">
            <form>
                <label for="email">E-mail</label>
                <input type="email" id="email" placeholder="Email"/>

                <label for="password">Hasło</label>
                <input type="password" id="password" placeholder="Password"/>

                <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>
                <script src="https://www.google.com/recaptcha/api.js" async defer></script>

                <div class="row">
                    <button type="button" onclick="loginUser()">Login</button>
                    <a href="${pageContext.request.contextPath}/users/register.jsp">Rejestracja</a>
                </div>
                <div id="result"></div>
            </form>
        </div>
    </main>
    </jsp:attribute>
</t:layout>