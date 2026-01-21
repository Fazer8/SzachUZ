<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
    String envKey = System.getenv("RECAPTCHA_SITE_KEY");
    if (envKey == null || envKey.isEmpty()) {
        envKey = "6LeIHigsAAAAAC4-fcb9a6HEROaZFy3qNBlhwJLU"; // fallback
    }
    pageContext.setAttribute("recaptchaSiteKey", envKey);
%>

<t:layout page_name="Register" block="logged">
    <jsp:attribute name="head">
    <script>
        window.onload = function () {

            async function registerUser() {
                const username = document.getElementById("username").value.trim();
                const email = document.getElementById("email").value.trim();
                const password = document.getElementById("password").value.trim();
                const captchaToken = grecaptcha.getResponse();
                const resultBox = document.getElementById("result");

                if (!username || !email || !password) {
                    resultBox.className = "error";
                    resultBox.textContent = "Wypełnij wszystkie pola!";
                    return;
                }

                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(email)) {
                    resultBox.className = "error";
                    resultBox.textContent = "Podaj poprawny adres email!";
                    return;
                }

                const body = {username, email, password, captcha: captchaToken};

                try {
                    const res = await fetch("${pageContext.request.contextPath}/api/auth/register", {
                        method: "POST",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify(body)
                    });

                    const text = await res.text();

                    if (!res.ok) {
                        resultBox.className = "error";
                        resultBox.textContent = "Błąd: " + text;
                    } else {
                        resultBox.className = "success";
                        resultBox.textContent = "Sukces: " + text;
                    }
                } catch (err) {
                    console.error(err);
                    resultBox.className = "error";
                    resultBox.textContent = "Wystąpił błąd połączenia z serwerem.";
                }

                grecaptcha.reset();
            }

            let usernameTimer;
            let emailTimer;

            function debounce(func, timeout = 300) {
                return (...args) => {
                    clearTimeout(usernameTimer);
                    usernameTimer = setTimeout(() => func.apply(this, args), timeout);
                };
            }

            function debounceEmail(func, timeout = 300) {
                return (...args) => {
                    clearTimeout(emailTimer);
                    emailTimer = setTimeout(() => func.apply(this, args), timeout);
                };
            }

            async function checkUsername() {
                const username = document.getElementById("username").value.trim();
                const box = document.getElementById("username-status");

                if (username.length < 1) {
                    box.textContent = "";
                    return;
                }

                const res = await fetch("${pageContext.request.contextPath}/api/auth/check-username", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({username})
                });

                const data = await res.json();

                box.style.color = res.status === 409 ? "red" : "green";
                box.textContent = data.message;
            }

            async function checkEmail() {
                const email = document.getElementById("email").value.trim();
                const box = document.getElementById("email-status");

                if (email.length < 3) {
                    box.textContent = "";
                    return;
                }

                const res = await fetch("${pageContext.request.contextPath}/api/auth/check-email", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({email})
                });

                const data = await res.json();

                box.style.color = res.status === 409 ? "red" : "green";
                box.textContent = data.message;
            }

            document.getElementById("username").addEventListener("input", debounce(checkUsername));
            document.getElementById("email").addEventListener("input", debounceEmail(checkEmail));

            window.registerUser = registerUser;
        };
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
    </style>

    </jsp:attribute>

    <jsp:attribute name="body">

    <main class="site-margin border-color border-radius container center-content">
        <div class="secondary-bg-1 container center-content" style="border-radius: 50px;padding: 20px 20px 20px 20px">
            <form>
                <label for="username">Nazwa uzytkownika</label>
                <input type="text" id="username" placeholder="Username"/>
                <div id="username-status" class="status"></div>
                <label for="email">E-mail</label>
                <input type="email" id="email" placeholder="Email"/>
                <div id="email-status" class="status"></div>
                <label for="password">Hasło</label>
                <input type="password" id="password" placeholder="Password"/>
                <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>

                <script src="https://www.google.com/recaptcha/api.js" async defer></script>

                <button type="button" onclick="registerUser()">Register</button>

                <div id="result"></div>
            </form>
        </div>

    </main>
    </jsp:attribute>
</t:layout>
