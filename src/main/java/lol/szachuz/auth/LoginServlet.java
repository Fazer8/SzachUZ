package lol.szachuz.auth;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lol.szachuz.db.Repository.UsersRepository;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Inject
    private UsersRepository usersRepository;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String token = usersRepository.login(email, password);

        resp.setContentType("text/plain");

        if (token == null) {
            resp.getWriter().println("Błędny email lub hasło");
            return;
        }

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        resp.addCookie(cookie);

        resp.getWriter().println("Zalogowano pomyślnie! Token: " + token);
    }
}
