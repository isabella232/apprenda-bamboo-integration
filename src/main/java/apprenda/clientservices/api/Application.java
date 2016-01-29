package apprenda.clientservices.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jvanbrackel on 1/27/2016.
 */
public class Application {
    public Application() {
    }

    @SerializedName("name")
    public String Name;
    @SerializedName("description")
    public String Description;
    @SerializedName("alias")
    public String Alias;
    @SerializedName("currentVersion")
    public CurrentVersion CurrentVersion;
}

