<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Rejestracja</title>
</head>
<body>

<h2>Rejestracja</h2>

<form action="${pageContext.request.contextPath}/register" method="post">

    Nazwa użytkownika: <br>
    <input type="text" name="username" required><br><br>

    Email: <br>
    <input type="email" name="email" required><br><br>

    Hasło: <br>
    <input type="password" name="password" required><br><br>

    <button type="submit">Utwórz konto</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/users/login.jsp">Logowanie</a>

</body>
</html>
