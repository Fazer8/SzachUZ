package lol.szachuz.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lol.szachuz.db.Repository.UsersRepository;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UsersRepository usersRepository;

    @Override
    public void init() throws ServletException {
        super.init();
        usersRepository = new UsersRepository(); // <- ręczna inicjalizacja
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String email = req.getParameter("email");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String result = usersRepository.register(username, email, password);

        if (result.startsWith("Registration successful")) {
            resp.sendRedirect(req.getContextPath() + "/users/login.jsp");
        } else {
            req.setAttribute("errorMessage", result);
            req.getRequestDispatcher("/users/register.jsp").forward(req, resp);
        }
    }
}
