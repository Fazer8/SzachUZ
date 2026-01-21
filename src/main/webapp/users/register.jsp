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

<t:layout page_name="register.title" block="logged">
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
                    resultBox.textContent =
                      translations["register.error.empty"] || "Wypełnij wszystkie pola!";
                    return;
                }

                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (!emailRegex.test(email)) {
                    resultBox.className = "error";
                    resultBox.textContent =
                      translations["register.error.email"] || "Podaj poprawny adres email!";
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
                        resultBox.textContent =
                          (translations["register.error"] || "Błąd: ") + text;
                    } else {
                        resultBox.className = "success";
                        resultBox.textContent =
                          (translations["register.success"] || "Sukces: ") + text;
                    }
                } catch (err) {
                    console.error(err);
                    resultBox.className = "error";
                    resultBox.textContent =
                       translations["register.error.connection"] ||
                       "Wystąpił błąd połączenia z serwerem.";
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

        <div style="align-self: flex-end; margin-bottom: 10px;">
            <button type="button" onclick="setLang('pl')">PL</button>
            <button type="button" onclick="setLang('en')">EN</button>
        </div>
      <div class="secondary-bg-1 container center-content" style="border-radius: 50px;padding: 20px 20px 20px 20px">
        <form>
            <label for="username" data-i18n="register.username"></label>
            <input type="text"
                   id="username"
                   data-i18n-placeholder="register.username.placeholder"/>
            <div id="username-status" class="status"></div>
            <label for="email" data-i18n="register.email"></label>
            <input type="email"
                   id="email"
                   data-i18n-placeholder="register.email.placeholder"/>
            <div id="email-status" class="status"></div>
            <label for="password" data-i18n="register.password"></label>
            <input type="password"
                   id="password"
                   data-i18n-placeholder="register.password.placeholder"/>
            <div class="g-recaptcha" data-sitekey="${recaptchaSiteKey}"></div>

            <script src="https://www.google.com/recaptcha/api.js" async defer></script>

            <button type="button"
                    onclick="registerUser()"
                    data-i18n="register.submit">
            </button>

            <div id="result"></div>
        </form>
      </div>
        <script src="${pageContext.request.contextPath}/js/i18n.js"></script>
    </main>
    </jsp:attribute>
</t:layout>
