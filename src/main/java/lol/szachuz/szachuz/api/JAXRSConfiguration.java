package lol.szachuz.szachuz.api;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

@ApplicationPath("/api")
@ApplicationScoped
@LoginConfig(authMethod = "MP-JWT") // Włącz autentykację MicroProfile JWT
@DeclareRoles({"USER", "ADMIN"}) // Zadeklaruj role, których będziesz używać
public class JAXRSConfiguration extends Application {
}