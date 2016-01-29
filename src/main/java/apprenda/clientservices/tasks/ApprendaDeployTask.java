package apprenda.clientservices.tasks;

import apprenda.clientservices.Constants;
import apprenda.clientservices.api.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApprendaDeployTask implements CommonTaskType {
    @NotNull
    private static String apprendaSession;
    private static String platformRoot;

    public TaskResult execute(@NotNull CommonTaskContext taskContext) throws TaskException {
        BuildLogger buildLogger = taskContext.getBuildLogger();
        TaskResultBuilder resultsBuilder = TaskResultBuilder.newBuilder(taskContext).failed();

        final DeployTaskConfiguration configuration = new DeployTaskConfiguration(taskContext);

        DefaultHttpClient client = new DefaultHttpClient();
        client.setRedirectStrategy(new LaxRedirectStrategy());

        platformRoot = configuration.Url;
        Application application;

        //Login
        AuthenticationResponse result = LoginToApprenda(client, buildLogger, configuration);

        if (result.ApprendaSessionToken == null) {
            return closeConnectionAndBuild(client, resultsBuilder);
        }

        apprendaSession = result.ApprendaSessionToken;

        //Get Applications
        List<Application> applications = (List<Application>) GetAppList(client, buildLogger, configuration.Tenant);
        List appAliases = applications.stream().map(new Function<Application, String>() {
            public String apply(Application a) {
                return a.Alias;
            }
        }).collect(Collectors.toList());

        Boolean appExists = appAliases.stream().anyMatch(new Predicate<String>() {
            public boolean test(String a) {
                return a.equalsIgnoreCase(configuration.ApplicationAlias);
            }
        });

        //Get the application for checking
        if (appExists) {
            application = applications.stream().filter(new Predicate<Application>() {
                public boolean test(Application a) {
                    return a.Alias.equalsIgnoreCase(configuration.ApplicationAlias);
                }
            }).findFirst().get();
            if (configuration.RemoveIfExists) {
                Response response = RemoveApplication(client, buildLogger, application);
                appExists = false;
                if (!response.wasExpectedResponse())
                    return closeConnectionAndBuild(client, resultsBuilder);

            } else {
                if (application.CurrentVersion.Alias.equals(configuration.VersionAlias)) {
                    buildLogger.addErrorLogEntry("Can not create a new version for application " + configuration.ApplicationAlias +
                            " with version " + configuration.VersionAlias + ".  The version " + configuration.VersionAlias + " already exists.");
                    return closeConnectionAndBuild(client, resultsBuilder);
                }

            }
        }

        Response response;

        if (!appExists) {
            response = createNewApplication(client, buildLogger, configuration.ApplicationAlias);
            buildLogger.addBuildLogEntry("New application " + configuration.ApplicationAlias + " created. Version alias will be set to v1.  Requested setting " + configuration.VersionAlias + " will be ignored.");
            configuration.VersionAlias = "v1";
        } else {
            response = createNewApplicationVersion(client, buildLogger, configuration.ApplicationAlias, configuration.VersionAlias);
        }

        if (!response.wasExpectedResponse())
            return closeConnectionAndBuild(client, resultsBuilder);


        response = PatchAndPromoteApplication(client, buildLogger, configuration.ApplicationAlias, configuration.VersionAlias, configuration.Stage, taskContext.getWorkingDirectory().getPath() + "/" + configuration.ArchiveName);

        if (!response.wasExpectedResponse()) {
            return closeConnectionAndBuild(client, resultsBuilder);
        }

        resultsBuilder.success();
        return closeConnectionAndBuild(client, resultsBuilder);
    }

    private AuthenticationResponse LoginToApprenda(HttpClient client, BuildLogger buildLogger, DeployTaskConfiguration configuration) {
        buildLogger.addBuildLogEntry("Logging in to " + configuration.Url + ". Username " + configuration.Username + ". Tenant " + configuration.Tenant + ".");
        HttpPost post = new HttpPost(platformRoot + Constants.AUTHENTICATION_URL);
        post.addHeader("Content-Type", "application/json");
        AuthenticationRequest request = new AuthenticationRequest();
        request.username = configuration.Username;
        request.password = configuration.Password;
        request.tenantAlias = configuration.Tenant;
        String authRequestString = getJsonFromObject(request);

        try {
            post.setEntity(new StringEntity(authRequestString));
        } catch (UnsupportedEncodingException e) {
            addExeceptionToBuildLog(buildLogger, e);
        }

        Response result = executeMessage(client, post, buildLogger, HttpStatus.SC_CREATED);
        if (result != null) {
            AuthenticationResponse response = getObjectFromJson(result.ResponseBody, AuthenticationResponse.class);
            if (response.ApprendaSessionToken == null) {
                buildLogger.addErrorLogEntry("Failed to retrieve anthentication token.   " + result.ResponseBody);
            }
            return response;
        }
        return new AuthenticationResponse();
    }

    private Collection<Application> GetAppList(HttpClient client, BuildLogger buildLogger, String tenant) {
        buildLogger.addBuildLogEntry("Grabbing Existing Applications for " + tenant + ".");
        HttpGet httpGet = new HttpGet(platformRoot + Constants.GET_APPLICATIONS_URL);
        httpGet.addHeader("Content-Type", "application/json");
        httpGet.addHeader("ApprendaSessionToken", apprendaSession);
        Response result = executeMessage(client, httpGet, buildLogger);
        Collection<Application> applications = getObjectFromJson(result.ResponseBody, new TypeToken<Collection<Application>>() {
        });

        return applications;
    }

    private Response RemoveApplication(HttpClient client, BuildLogger buildLogger, Application application) {
        buildLogger.addBuildLogEntry("Deleting application " + application.Name + " (" + application.Alias + ").");
        HttpDelete method = new HttpDelete(platformRoot + String.format(Constants.DELETE_APPLICATION_URL_FORMAT, application.Alias));
        method.addHeader("Content-Type", "application/json");
        method.addHeader("ApprendaSessionToken", apprendaSession);
        return executeMessage(client, method, buildLogger, HttpStatus.SC_NO_CONTENT);
    }

    private Response createNewApplication(HttpClient client, BuildLogger buildLogger, String applicationAlias) {
        buildLogger.addBuildLogEntry("Creating new application " + applicationAlias + ".");
        HttpPost httpPost = new HttpPost(platformRoot + "/" + Constants.CREATE_APPLICATION_URL);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("ApprendaSessionToken", apprendaSession);
        NewApplicationRequest newApplicationRequestBody = new NewApplicationRequest();
        newApplicationRequestBody.Alias = applicationAlias;
        newApplicationRequestBody.Name = applicationAlias;

        String authRequestString = getJsonFromObject(newApplicationRequestBody);
        StringEntity entity = null;
        try {
            entity = new StringEntity(authRequestString);
        } catch (UnsupportedEncodingException e) {
            addExeceptionToBuildLog(buildLogger, e);
        }
        httpPost.setEntity(entity);
        return executeMessage(client, httpPost, buildLogger, HttpStatus.SC_CREATED);
    }

    private Response createNewApplicationVersion(HttpClient client, BuildLogger buildLogger, String applicationAlias, String versionAlias) {
        buildLogger.addBuildLogEntry("Creating new version " + versionAlias + " for application " + applicationAlias + ".");
        HttpPost httpPost = new HttpPost(platformRoot + String.format(Constants.NEW_VERSION_URL_FORMAT, applicationAlias));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("ApprendaSessionToken", apprendaSession);
        NewVersionRequest newVersionRequestBody = new NewVersionRequest();
        newVersionRequestBody.Alias = versionAlias;
        newVersionRequestBody.Name = versionAlias;

        String authRequestString = getJsonFromObject(newVersionRequestBody);
        StringEntity entity = null;
        try {
            entity = new StringEntity(authRequestString);
        } catch (UnsupportedEncodingException e) {
            addExeceptionToBuildLog(buildLogger, e);
        }
        httpPost.setEntity(entity);
        return executeMessage(client, httpPost, buildLogger, HttpStatus.SC_CREATED);
    }

    private Response PatchAndPromoteApplication(HttpClient client, BuildLogger buildLogger, String applicationAlias, String versionAlias, String stage, String filePath) {
        buildLogger.addBuildLogEntry("Uploading application archive for application " + applicationAlias + " version " + versionAlias + " and promoting to " + stage + ".");
        HttpPost httpPost = new HttpPost(platformRoot + String.format(Constants.PATCH_AND_PROMOTE_URL_FORMAT, applicationAlias, versionAlias, stage));
        httpPost.addHeader("Content-Type", "application/octet-stream");
        httpPost.addHeader("ApprendaSessionToken", apprendaSession);
        File file = new File(filePath);
        InputStreamEntity entity = null;
        try {
            entity = new InputStreamEntity(new FileInputStream(file), file.length());
        } catch (FileNotFoundException e) {
            addExeceptionToBuildLog(buildLogger, e);
        }
        httpPost.setEntity(entity);
        Response response = executeMessage(client, httpPost, buildLogger);
        if (response.wasExpectedResponse()) {
            PatchAndPromoteResponse patchResponse = getObjectFromJson(response.ResponseBody, PatchAndPromoteResponse.class);
            for (Section section :  patchResponse.Sections) {
                buildLogger.addBuildLogEntry(section.Title);

                for (Message aMessage :
                        section.Messages) {
                    buildLogger.addBuildLogEntry("Severity: " + aMessage.Severity + "  Message: " + aMessage.Message);
                }
            }

            if (!patchResponse.Success) {
                buildLogger.addErrorLogEntry("Patch and promote was not successful! Review the logs for details");
            } else {
                buildLogger.addBuildLogEntry("Patch and promote was successful.");
            }
        }
        return response;

    }

    private Response executeMessage(HttpClient client, HttpRequestBase request, BuildLogger buildLogger) {
        return executeMessage(client, request, buildLogger, HttpStatus.SC_OK);
    }

    private Response executeMessage(HttpClient client, HttpRequestBase message, BuildLogger buildLogger, int expectedResult) {
        HttpResponse response = null;
        try {
            buildLogger.addBuildLogEntry("Executing request: " + message.getURI());
            response = client.execute(message);

            if (response.getStatusLine().getStatusCode() != expectedResult) {
                buildLogger.addErrorLogEntry("Method failed. " + message.getURI() + ".   " + response.getStatusLine());
            }
            return new Response(response.getStatusLine().getStatusCode(), response.getEntity(), expectedResult);
        } catch (IOException e) {
            addExeceptionToBuildLog(buildLogger, e);
        } finally {
            if(response != null)
            {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    addExeceptionToBuildLog(buildLogger, e);
                }
            }
        }
        return null;
    }

    private TaskResult closeConnectionAndBuild(HttpClient client, TaskResultBuilder builder)
    {
        client.getConnectionManager().shutdown();
        return builder.build();
    }

    private void addExeceptionToBuildLog(BuildLogger buildLogger, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);

        buildLogger.addErrorLogEntry(t.getMessage() + "  " + sw.toString());
    }

    private <T> T getObjectFromJson(String jsonString, Class<T> conversionType) {
        Gson gson = getGsonBuilder();
        T returnValue = gson.fromJson(jsonString, conversionType);

        return returnValue;
    }

    private <T> T getObjectFromJson(String jsonString, TypeToken<T> conversionType) {
        Gson gson = getGsonBuilder();
        Type collectionType = conversionType.getType();
        T returnValue = gson.fromJson(jsonString, collectionType);
        return returnValue;
    }

    private <T> String getJsonFromObject(T object) {
        Gson gson = getGsonBuilder();
        return gson.toJson(object);
    }

    @NotNull
    private Gson getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

}
