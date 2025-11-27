<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="Register">
    <jsp:attribute name="head">
    <script>
        async function registerUser() {
            const username = document.getElementById("username").value.trim();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            const body = {
                username: username,
                email: email,
                password: password
            };

            const res = await fetch("${pageContext.request.contextPath}/api/auth/register", {

                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            const text = await res.text();

            const resultBox = document.getElementById("result");

            if (!res.ok) {
                resultBox.className = "error";
                resultBox.textContent = "Error (" + res.status + "):\n" + text;
            } else {
                resultBox.className = "success";
                resultBox.textContent = "Success:\n" + text;
            }
        }
    </script>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin border-color border-radius container">

        <input type="text" id="username" placeholder="Username"/>
        <input type="email" id="email" placeholder="Email"/>
        <input type="password" id="password" placeholder="Password"/>

        <button onclick="registerUser()">Register</button>

        <div id="result"></div>
    </main>
    </jsp:attribute>
</t:layout>
