/* Copyright 2016 Apprenda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
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
