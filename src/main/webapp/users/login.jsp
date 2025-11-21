<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Logowanie</title>
</head>
<body>

<h2>Logowanie</h2>

<form action="${pageContext.request.contextPath}/login" method="post">

    Email: <br>
    <input type="email" name="email" required><br><br>

    Hasło: <br>
    <input type="password" name="password" required><br><br>

    <button type="submit">Zaloguj się</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/users/register.jsp">Rejestracja</a>

</body>
</html>
