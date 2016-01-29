package apprenda.clientservices;

/**
 * Constants Class to be used in other areas of the application.
 */
public class Constants {
    public final static String GET_APPLICATIONS_URL = "/developer/api/v1/apps";
    public final static String CREATE_APPLICATION_URL = "/developer/api/v1/apps";
    public final static String DELETE_APPLICATION_URL_FORMAT = "/developer/api/v1/apps/%1$2s";
    public final static String NEW_VERSION_URL_FORMAT = "/developer/api/v1/versions/%1$2s";
    public final static String AUTHENTICATION_URL = "/authentication/api/v1/sessions/developer";
    public final static String PATCH_AND_PROMOTE_URL_FORMAT = "/developer/api/v1/versions/%1$2s/%2$2s?action=patch&patchMode=destructive&stage=%3$2s";
}
