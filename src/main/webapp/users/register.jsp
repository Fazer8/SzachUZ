<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register</title>
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

            const res = await fetch("<%=request.getContextPath()%>/api/auth/register", {
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
</head>

<body>
<div class="container">
    <h2>Create Account</h2>

    <input type="text" id="username" placeholder="Username"/>
    <input type="email" id="email" placeholder="Email"/>
    <input type="password" id="password" placeholder="Password"/>

    <button onclick="registerUser()">Register</button>

    <div id="result"></div>
</div>
</body>
</html>
