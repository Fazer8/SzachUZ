<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login Test</title>

    <script>
        async function loginUser() {
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            const body = {
                email: email,
                password: password
            };

            const res = await fetch("<%=request.getContextPath()%>/api/auth/login", {
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
    <h2>Login Test</h2>

    <input type="email" id="email" placeholder="Email"/>
    <input type="password" id="password" placeholder="Password"/>

    <button onclick="loginUser()">Login</button>

    <div id="result"></div>
</div>
</body>
</html>
