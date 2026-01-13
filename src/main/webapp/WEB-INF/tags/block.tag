<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="user" required="false" %>
<%@ tag import="jakarta.servlet.http.Cookie" %>

<%
    /*
      user attribute values:
        - "logged" -> redirect logged-in users to "/users/userProfile.jsp"
        - "guest"  -> redirect guests to "/users/login.jsp"
        - null / empty -> do nothing
    */

    if (user == null || user.isBlank()) {
        return;
    }

    boolean loggedIn = false;

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie c : cookies) {
            if ("authToken".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                loggedIn = true;
                break;
            }
        }
    }

    String contextPath = request.getContextPath();

    if ("logged".equals(user) && loggedIn) {
        response.sendRedirect(contextPath + "/users/userProfile.jsp");
        return;
    }

    if ("guest".equals(user) && !loggedIn) {
        response.sendRedirect(contextPath + "/users/login.jsp");
    }
%>
