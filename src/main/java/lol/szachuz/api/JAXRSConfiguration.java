package lol.szachuz.api;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"USER", "ADMIN"})
public class JAXRSConfiguration extends Application {
    /**
     * Retrieves a set of resource and provider classes to be utilized by the JAX-RS application.
     * This method adds the classes associated with REST API resources and features to the set
     * to enable their registration within the application.
     *
     * @return a set containing the classes of REST resources and providers registered in the application
     */

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(AuthResource.class);
        resources.add(ProfileResource.class);
        resources.add(ScoreResource.class);
        resources.add(MultiPartFeature.class);
        return resources;
    }
}