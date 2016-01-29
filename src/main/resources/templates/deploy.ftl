<!-- Copyright 2016 Apprenda

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. -->
[@ww.textfield labelKey="apprendaDeploy.url" name="url" required='true'/]
[@ww.textfield labelKey="apprendaDeploy.username" name="username" required='true'/]
[@ww.password labelKey="apprendaDeploy.password" name="password" required='true'/]
[@ww.textfield labelKey="apprendaDeploy.tenant" name="tenant" required='true'/]
[@ww.checkbox labelKey="apprendaDeploy.remove" name="remove"/]
[@ww.textfield labelKey="apprendaDeploy.appAlias" name="appAlias" required='true'/]
[@ww.textfield labelKey="apprendaDeploy.verAlias" name="verAlias" required='true'/]
[@ww.textfield labelKey="apprendaDeploy.archiveName" name="archiveName" required='true'/]
[@ww.select labelKey="apprendaDeploy.stage" name="stage" list="stageList" listKey='Id' listValue='Value' required='true'/]