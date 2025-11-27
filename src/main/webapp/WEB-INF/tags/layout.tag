<%@ tag description="Layout template" pageEncoding="UTF-8" %>
<%@ attribute name="page_name" required="true" %>
<%@ attribute name="head" fragment="true" required="false" %>
<%@ attribute name="body" fragment="true" required="true" %>

<html>
    <head>
        <title>${page_name}</title>
        <jsp:invoke fragment="head" />
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    </head>
    <body class="main-bg">
        <header class="border-radius secondary-bg site-margin">
            <img src="szachuz_logo.png" alt="«SzachUZ»">
            <h1>My Profile</h1>
            <div>
                <button>theme</button>
                <div>
                    <p>🇵🇱<br/>🇬🇧</p>
                </div>
            </div>
        </header>
        <hr class="header-split" />
        <jsp:invoke fragment="body" />
    </body>
</html>
