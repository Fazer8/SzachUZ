<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="Strona główna">
    <jsp:attribute name="head">
    <script>
        // skrypty
    </script>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
        <a href="hello-servlet">Hello Servlet</a>
        <a href="users/userProfile.jsp">Profil Użytkownika</a>
        <a href="users/login.jsp">Zaloguj Się</a>
        <a href="users/register.jsp">Zarejestruj Się</a>
    </main>
    </jsp:attribute>
</t:layout>
