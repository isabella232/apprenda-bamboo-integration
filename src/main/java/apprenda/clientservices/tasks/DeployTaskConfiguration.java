package apprenda.clientservices.tasks;

import com.atlassian.bamboo.task.CommonTaskContext;

public class DeployTaskConfiguration {
    public DeployTaskConfiguration(CommonTaskContext taskContext) {
        Url = taskContext.getConfigurationMap().get("url");
        Username = taskContext.getConfigurationMap().get("username");
        Password = taskContext.getConfigurationMap().get("password");
        Tenant = taskContext.getConfigurationMap().get("tenant");
        RemoveIfExists = taskContext.getConfigurationMap().getAsBoolean("remove");
        ApplicationAlias = taskContext.getConfigurationMap().get("appAlias");
        VersionAlias = taskContext.getConfigurationMap().get("verAlias");
        Stage = taskContext.getConfigurationMap().get("stage");
        ArchiveName = taskContext.getConfigurationMap().get("archiveName");
    }

    public String Url;
    public String Username;
    public String Password;
    public String Tenant;
    public Boolean RemoveIfExists;
    public String ApplicationAlias;
    public String VersionAlias;
    public String ArchiveName;
    public String Stage;

}
