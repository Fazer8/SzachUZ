<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="user" required="false" %>

<%
    /*
      user attribute values:
        - "logged" -> block logged-in users
        - "guest"  -> block guests
        - null / empty -> do nothing
    */

    // YOU control login detection here
    boolean isLogged = session.getAttribute("user") != null;
    // or: Boolean.TRUE.equals(session.getAttribute("isLogged"))

    if ("logged".equals(user) && isLogged) {
        //response.sendRedirect(request.getContextPath() + "/users/userProfile.jsp");
        return;
    }

    if ("guest".equals(user) && !isLogged) {
        //response.sendRedirect(request.getContextPath() + "/users/login.jsp");
        return;
    }
%>