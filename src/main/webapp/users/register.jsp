<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <meta charset="UTF-8" />
    <script>
        async function registerUser(event) {
            event.preventDefault();

            const username = document.getElementById("username").value.trim();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            if (!username || !email || !password) {
                showResult("Wszystkie pola są wymagane", "red");
                return;
            }

            const payload = { username, email, password }

            // <-- Tutaj ustaw URL backendu; dostosuj port jeśli potrzebne -->
            const apiUrl = 'localhost/api/auth/register';

            let response;
            try {
                response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                });
            } catch (e) {
                showResult("Błąd połączenia z serwerem", "red");
                return;
            }

            let result;
            try {
                result = await response.json();
            } catch (e) {
                showResult("Nieprawidłowa odpowiedź z serwera", "red");
                return;
            }

            if (response.ok) {
                showResult("Sukces: " + (result.message || "Zarejestrowano pomyślnie"), "green");
            } else {
                showResult("Błąd: " + (result.error || "Nieznany błąd"), "red");
            }
        }

        function showResult(message, color) {
            const box = document.getElementById("result");
            box.style.color = color;
            box.innerText = message;
        }
    </script>
</head>
<body>

<h2>Rejestracja</h2>

<form onsubmit="registerUser(event)">
    <label>Username:</label><br>
    <input type="text" id="username" required><br><br>

    <label>Email:</label><br>
    <input type="email" id="email" required><br><br>

    <label>Password:</label><br>
    <input type="password" id="password" required><br><br>

    <button type="submit">Zarejestruj</button>
</form>

<div id="result" style="margin-top:20px; font-weight:bold;"></div>

</body>
</html>
