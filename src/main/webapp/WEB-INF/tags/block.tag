<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="user" required="false" %>
<%@ tag import="jakarta.servlet.http.Cookie" %>
<%@ variable name-given="isLoggedIn" variable-class="java.lang.Boolean" scope="AT_END" %>

<%
    /*
      user attribute values:
        - "logged" -> redirect logged-in users to "/users/userProfile.jsp"
        - "guest"  -> redirect guests to "/users/login.jsp"
        - null / empty -> do nothing
    */

    boolean status = false;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie c : cookies) {
            if ("authToken".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                status = true;
                break;
            }
        }
    }

    jspContext.setAttribute("isLoggedIn", status);

    if (user != null && !user.isBlank()) {
        String contextPath = request.getContextPath();

        if ("logged".equals(user) && status) {
            response.sendRedirect(contextPath + "/users/userProfile.jsp");
            return;
        }

        if ("guest".equals(user) && !status) {
            response.sendRedirect(contextPath + "/users/login.jsp");
            return;
        }
    }
%>