<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<t:layout page_name="Login">
    <jsp:attribute name="head">
    <script>
        async function loginUser() {
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();
            const captchaToken = grecaptcha.getResponse();

            const body = { email, password, captcha: captchaToken };

            const res = await fetch("${pageContext.request.contextPath}/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            const resultBox = document.getElementById("result");

            if (!res.ok) {
                const text = await res.text();
                resultBox.className = "error";
                resultBox.textContent = "Error (" + res.status + "):\n" + text;
            } else {
                // Odczyt tokenu z JSON i zapis w localStorage
                const data = await res.json();
                if (data.token) {
                    localStorage.setItem("authToken", data.token);
                }

                resultBox.className = "success";
                resultBox.textContent = "Login successful! Redirecting...";

                // przekierowanie do strony profilu
                window.location.href = "${pageContext.request.contextPath}/dashboard.jsp";
            }

            grecaptcha.reset();
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
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin border-color border-radius container center-content">
        <form>
            <label for="email">E-mail</label>
            <input type="email" id="email" placeholder="Email"/>
            <label for="password">Hasło</label>
            <input type="password" id="password" placeholder="Password"/>
            <div class="g-recaptcha" data-sitekey="6Le06h8sAAAAAOJ3xtyqsTqNgrjlZokjvtPW9yw2"></div>
            <script src="https://www.google.com/recaptcha/api.js" async defer></script>
            <div class="row">
                <button onclick="loginUser()">Login</button>
                <a href="${pageContext.request.contextPath}/users/register.jsp">Rejestracja</a>
            </div>
            <div id="result"></div>
        </form>
    </main>
    </jsp:attribute>
</t:layout>
