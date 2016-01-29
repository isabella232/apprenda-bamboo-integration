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
