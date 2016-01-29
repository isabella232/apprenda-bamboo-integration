package apprenda.clientservices.tasks;

import apprenda.clientservices.Constants;
import apprenda.clientservices.api.*;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by JvB on 1/26/2016.
 */
public class ApprendaDeployTask implements CommonTaskType {
    @NotNull
    private static String apprendaSession;
    private static String platformRoot;
    public TaskResult execute(@NotNull CommonTaskContext taskContext) throws TaskException {
        BuildLogger buildLogger = taskContext.getBuildLogger();

        //Grab values from the task context
        String url = taskContext.getConfigurationMap().get("url");
        String username = taskContext.getConfigurationMap().get("username");
        String password = taskContext.getConfigurationMap().get("password");
        String tenant = taskContext.getConfigurationMap().get("tenant");
        Boolean removeIfExists = taskContext.getConfigurationMap().getAsBoolean("remove");
        final String applicationAlias = taskContext.getConfigurationMap().get("appAlias");
        String versionAlias = taskContext.getConfigurationMap().get("verAlias");
        String stage = taskContext.getConfigurationMap().get("stage");
        String archiveName = taskContext.getConfigurationMap().get("archiveName");

        HttpClient client = new HttpClient();
        platformRoot = url;
        Application application;

        //Login
        AuthenticationResponse result = LoginToApprenda(client, buildLogger, url, username, password, tenant);

        if(result.ApprendaSessionToken == null)
        {
            return TaskResultBuilder.newBuilder(taskContext).failed().build();
        }

        apprendaSession = result.ApprendaSessionToken;

        //Get Applications
        List<Application> applications = (List<Application>)GetAppList(client, buildLogger, tenant);
        List appAliases = applications.stream().map(new Function<Application, String>() {
            public String apply(Application a) {
                return a.Alias;
            }
        }).collect(Collectors.toList());

        Boolean appExists = appAliases.stream().anyMatch(new Predicate<String>() {
            public boolean test(String a) {
                return a.equals(applicationAlias);
            }
        });

        //Get the application for checking
        if (appExists) {
            application = applications.stream().filter(new Predicate<Application>() {
                public boolean test(Application a) {
                    return a.equals(applicationAlias);
                }
            }).findFirst().get();
            if(removeIfExists) {
                Response response = RemoveApplication(client, buildLogger, application);
                if(!response.wasExpectedResponse())
                    return TaskResultBuilder.newBuilder(taskContext).failed().build();

                appExists = false;
            } else
            {
                if(application.CurrentVersion.Alias.equals(versionAlias))
                {
                    buildLogger.addErrorLogEntry("Can not create a new version for application " + applicationAlias +
                            " with version " + versionAlias + ".  The version " + versionAlias + " already exists.");
                    return TaskResultBuilder.newBuilder(taskContext).failed().build();
                }

            }
        }

        Response response;

        if(!appExists)
        {
            response = CreateNewApplication(client, buildLogger, applicationAlias);
            versionAlias = "v1";
            buildLogger.addBuildLogEntry("New application " + applicationAlias + " created. Version alias will be set to v1.  Requested setting " + versionAlias + " will be ignored.");
        } else
        {
            response = CreateNewApplicationVersion(client, buildLogger, applicationAlias, versionAlias);
        }

        if(!response.wasExpectedResponse())
            return TaskResultBuilder.newBuilder(taskContext).failed().build();


        response = PatchAndPromoteApplication(client, buildLogger, applicationAlias, versionAlias, stage, taskContext.getWorkingDirectory().getPath() + "/" + archiveName);

        if(!response.wasExpectedResponse())
        {
            return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();
        }

        return TaskResultBuilder.newBuilder(taskContext).checkTestFailures().build();

    }

    private AuthenticationResponse LoginToApprenda(HttpClient client, BuildLogger buildLogger, String url, String username, String password, String tenant) {
        buildLogger.addBuildLogEntry("Logging in to " + url + ". Username " + username + ". Tenant " + tenant + ".");
        PostMethod method = new PostMethod(platformRoot + Constants.AUTHENTICATION_URL);
        method.addRequestHeader("Content-Type", "application/json");
        AuthenticationRequest request = new AuthenticationRequest();
        request.username = username;
        request.password = password;
        request.tenantAlias = tenant;
        Gson gson = new Gson();
        String authRequestString = gson.toJson(request);
        RequestEntity entity = new StringRequestEntity(authRequestString);
        method.setRequestEntity(entity);
        Response result = ExecuteMethod(client, method, buildLogger);
        if(result != null)
        {
            return gson.fromJson(result.ResponseBody, AuthenticationResponse.class);
        }
        return new AuthenticationResponse();
    }

    private Collection<Application> GetAppList(HttpClient client, BuildLogger buildLogger, String tenant) {
        buildLogger.addBuildLogEntry("Grabbing Existing Applications for " + tenant + ".");
        GetMethod method = new GetMethod(platformRoot + Constants.GET_APPLICATIONS_URL);
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("ApprendaSessionToken", apprendaSession);
        Response result = ExecuteMethod(client, method, buildLogger);

        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<Application>>(){}.getType();
        Collection<Application> applications =  gson.fromJson(result.ResponseBody, collectionType);

        return applications;
    }

    private Response RemoveApplication(HttpClient client, BuildLogger buildLogger, Application application) {
        buildLogger.addBuildLogEntry("Deleting application " + application.Name + " (" + application.Alias +").");
        DeleteMethod method = new DeleteMethod(platformRoot + String.format(Constants.DELETE_APPLICATION_URL_FORMAT, application.Alias));
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("ApprendaSessionToken", apprendaSession);
        return ExecuteMethod(client, method, buildLogger, HttpStatus.SC_NO_CONTENT);
    }

    private Response CreateNewApplication(HttpClient client, BuildLogger buildLogger, String applicationAlias) {
        buildLogger.addBuildLogEntry("Creating new application " + applicationAlias + ".");
        PostMethod method = new PostMethod(platformRoot + "/" + Constants.CREATE_APPLICATION_URL);
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("ApprendaSessionToken", apprendaSession);
        NewApplicationRequest requestBody = new NewApplicationRequest();
        requestBody.Alias = applicationAlias;
        requestBody.Name = applicationAlias;
        Gson gson = new Gson();
        String authRequestString = gson.toJson(requestBody);
        RequestEntity entity = new StringRequestEntity(authRequestString);
        method.setRequestEntity(entity);
        return ExecuteMethod(client, method, buildLogger, HttpStatus.SC_CREATED);
    }

    private Response CreateNewApplicationVersion(HttpClient client, BuildLogger buildLogger, String applicationAlias, String versionAlias){
        buildLogger.addBuildLogEntry("Creating new version " + versionAlias + " for application " + applicationAlias + ".");
        PostMethod method = new PostMethod(platformRoot + String.format(Constants.NEW_VERSION_URL_FORMAT, applicationAlias));
        method.addRequestHeader("Content-Type", "application/json");
        method.addRequestHeader("ApprendaSessionToken", apprendaSession);
        NewVersionRequest requestBody = new NewVersionRequest();
        requestBody.Alias = versionAlias;
        requestBody.Name = versionAlias;
        Gson gson = new Gson();
        String authRequestString = gson.toJson(requestBody);
        RequestEntity entity = new StringRequestEntity(authRequestString);
        method.setRequestEntity(entity);
        return ExecuteMethod(client, method, buildLogger, HttpStatus.SC_CREATED);
    }

    private Response PatchAndPromoteApplication(HttpClient client, BuildLogger buildLogger, String applicationAlias, String versionAlias, String stage, String filePath) {
        buildLogger.addBuildLogEntry("Uploading application archive for application " + applicationAlias + " version " + versionAlias + " and promoting to " + stage + ".");
        PostMethod method = new PostMethod(platformRoot + String.format(Constants.PATCH_AND_PROMOTE_URL_FORMAT, applicationAlias, versionAlias, stage));
        File file = new File(filePath);
        InputStreamRequestEntity entity = null;
        try {
            entity = new InputStreamRequestEntity(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            buildLogger.addErrorLogEntry(e.getMessage() + "\n " + sw.toString());
        }
        method.setRequestEntity(entity);
        Response response = ExecuteMethod(client, method, buildLogger);
        if(response.wasExpectedResponse()) {
            Gson gson = new Gson();
            PatchAndPromoteResponse patchResponse = gson.fromJson(response.ResponseBody, PatchAndPromoteResponse.class);
            buildLogger.addBuildLogEntry(patchResponse.Title);
            if(!patchResponse.Success)
            {
                buildLogger.addErrorLogEntry("Patch and promote was not successful! Review the logs for details");
            } else
            {
                buildLogger.addBuildLogEntry("Patch and promote was successful.");
            }
            for (Message message:
                 patchResponse.Messages) {
                buildLogger.addBuildLogEntry("Severity: " + message.Severity + "\nMessage: " + message.Message);
            }
            
        }
        return response;

    }




    private Response ExecuteMethod(HttpClient client, HttpMethod method, BuildLogger buildLogger, int expectedResult) {
        try {
            buildLogger.addBuildLogEntry("Executing method: " + method.getURI());
            int statusCode = client.executeMethod(method);

            if (statusCode != expectedResult) {
                buildLogger.addErrorLogEntry("Method failed. " + method.getStatusLine() + "\n" + method.getStatusText());
            }
            return new Response(statusCode, method.getResponseBodyAsString(), expectedResult);
        } catch (HttpException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            buildLogger.addErrorLogEntry(e.getMessage() + "\n " + sw.toString());

        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            buildLogger.addErrorLogEntry(e.getMessage() + "\n " + sw.toString());
        }
        return null;
    }

    private Response ExecuteMethod(HttpClient client, HttpMethod method, BuildLogger buildLogger)
    {
        return ExecuteMethod(client, method, buildLogger, HttpStatus.SC_OK);
    }


}
