<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="NAZWA STRONY">
    <jsp:attribute name="head">
    <script>
        // automatyczne przekierowanie do tymczasowej strony pośredniej
        window.onload = function() {
            window.location.href = "${pageContext.request.contextPath}/index.jsp";
        };
    </script>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
        <p>Trwa przekierowanie do strony użytkownika...</p>



    </main>
    </jsp:attribute>
</t:layout>
