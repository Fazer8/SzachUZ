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
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin border-color border-radius container">
        <input type="email" id="email" placeholder="Email"/>
        <input type="password" id="password" placeholder="Password"/>
        <div class="g-recaptcha" data-sitekey="6Le06h8sAAAAAOJ3xtyqsTqNgrjlZokjvtPW9yw2"></div>
        <script src="https://www.google.com/recaptcha/api.js" async defer></script>
        <button onclick="loginUser()">Login</button>
        <div id="result"></div>
    </main>
    </jsp:attribute>
</t:layout>
