<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%
    String envKey = System.getenv("RECAPTCHA_SITE_KEY");
    if (envKey == null || envKey.isEmpty()) {
        envKey = "6LeIHigsAAAAAC4-fcb9a6HEROaZFy3qNBlhwJLU"; // fallback
    }
    pageContext.setAttribute("recaptchaSiteKey", envKey);
%>

<t:layout page_name="Register">
    <jsp:attribute name="head">
   <script>
       window.onload = function() {

           async function registerUser() {
               const username = document.getElementById("username").value.trim();
               const email = document.getElementById("email").value.trim();
               const password = document.getElementById("password").value.trim();
               const captchaToken = grecaptcha.getResponse();

               const body = { username, email, password, captcha: captchaToken };

               const res = await fetch("${pageContext.request.contextPath}/api/auth/register", {
                   method: "POST",
                   headers: { "Content-Type": "application/json" },
                   body: JSON.stringify(body)
               });

               const text = await res.text();
               const resultBox = document.getElementById("result");

               if (!res.ok) {
                   resultBox.className = "error";
                   resultBox.textContent = "Error (" + res.status + "): " + text;
               } else {
                   resultBox.className = "success";
                   resultBox.textContent = "Success: " + text;
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
                   headers: { "Content-Type": "application/json" },
                   body: JSON.stringify({ username })
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
                   headers: { "Content-Type": "application/json" },
                   body: JSON.stringify({ email })
               });

               const data = await res.json();

               box.style.color = res.status === 409 ? "red" : "green";
               box.textContent = data.message;
           }

           // PODŁĄCZ EVENTY ⇩⇩⇩
           document.getElementById("username").addEventListener("input", debounce(checkUsername));
           document.getElementById("email").addEventListener("input", debounceEmail(checkEmail));

           // UDOSTĘPNIJ FUNKCJĘ REGISTER DLA PRZYCISKU
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

            <button onclick="registerUser()">Register</button>

            <div id="result"></div>
        </form>

    </main>
    </jsp:attribute>
</t:layout>
