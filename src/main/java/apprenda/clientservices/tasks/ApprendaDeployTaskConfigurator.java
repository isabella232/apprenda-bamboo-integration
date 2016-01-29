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

import apprenda.clientservices.api.StageListItem;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.i18n.I18nBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jvanbrackel on 1/27/2016.
 */
public class ApprendaDeployTaskConfigurator extends AbstractTaskConfigurator {
    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull ActionParametersMap params, @Nullable TaskDefinition previousTaskDefinition) {

        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put("url", params.getString("url"));
        config.put("username", params.getString("username"));
        config.put("password", params.getString("password"));
        config.put("tenant", params.getString("tenant"));
        config.put("remove", String.valueOf(params.getBoolean("remove")));
        config.put("appAlias", params.getString("appAlias"));
        config.put("verAlias", params.getString("verAlias"));
        config.put("stage", params.getString("stage"));
        config.put("archiveName", params.getString("archiveName"));

        return config;
    }

    @Override
    public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
        super.validate(params, errorCollection);


        String url = params.getString("url");
        Pattern pattern = Pattern.compile("^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher matcher = pattern.matcher(url);

        if(!matcher.matches())
        {
            errorCollection.addError("url", "Platform Url Root must be a valid secure url (e.g. http://example.apprenda.com)");
        }
    }

    @Override
    public void populateContextForCreate(@NotNull Map<String, Object> context) {
        super.populateContextForCreate(context);

        context.put("url", "https://");
        context.put("stageList", generateStageList());



    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.put("url", taskDefinition.getConfiguration().get("url"));
        context.put("username", taskDefinition.getConfiguration().get("username"));
        context.put("password", taskDefinition.getConfiguration().get("password"));
        context.put("tenant", taskDefinition.getConfiguration().get("tenant"));
        context.put("appAlias", taskDefinition.getConfiguration().get("appAlias"));
        context.put("verAlias", taskDefinition.getConfiguration().get("verAlias"));
        context.put("archiveName", taskDefinition.getConfiguration().get("archiveName"));
        context.put("remove", Boolean.valueOf(taskDefinition.getConfiguration().get("remove")));
        context.put("stageList", generateStageList());
        context.put("stage", taskDefinition.getConfiguration().get("stage"));

    }

    @Override
    public void populateContextForView(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        context.put("url", taskDefinition.getConfiguration().get("url"));
        context.put("username", taskDefinition.getConfiguration().get("username"));
        context.put("password", taskDefinition.getConfiguration().get("password"));
        context.put("tenant", taskDefinition.getConfiguration().get("tenant"));
        context.put("appAlias", taskDefinition.getConfiguration().get("appAlias"));
        context.put("verAlias", taskDefinition.getConfiguration().get("verAlias"));
        context.put("archiveName", taskDefinition.getConfiguration().get("archiveName"));
        context.put("remove", Boolean.valueOf(taskDefinition.getConfiguration().get("remove")));
        context.put("stageList", generateStageList());
        context.put("stage", taskDefinition.getConfiguration().get("stage"));

    }

    private List<StageListItem> generateStageList() {
        List<StageListItem> stages = new ArrayList<StageListItem>();

        stages.add(new StageListItem("sandbox", "sandbox"));
        stages.add(new StageListItem("published", "published"));

        return stages;
    };
}
