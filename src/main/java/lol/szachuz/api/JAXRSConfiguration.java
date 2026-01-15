package lol.szachuz.api;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.util.HashSet;
import java.util.Set;

/**
 * Configures a JAX-RS application by extending the javax.ws.rs.core.Application class.
 * This class is used to define the available resources and their configuration within
 * the application's RESTful API. It provides an entry point to scan and manage resource
 * classes available for processing requests.
 *
 * Annotations:
 * - @ApplicationPath("/api"): Specifies the base URI for all JAX-RS resources.
 * - @LoginConfig(authMethod = "MP-JWT"): Configures the security mechanism utilizing
 *   MicroProfile JWT for authentication.
 * - @DeclareRoles({"USER", "ADMIN"}): Declares the roles supported for application security.
 *
 * Methods:
 * - getClasses(): Returns a set of classes that represent REST resource endpoints and
 *   features. This method registers specific resource classes like AuthResource,
 *   ProfileResource, and others as well as additional features such as multipart support.
 */
@ApplicationPath("/api")
@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"USER", "ADMIN"})
public class JAXRSConfiguration extends Application {

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