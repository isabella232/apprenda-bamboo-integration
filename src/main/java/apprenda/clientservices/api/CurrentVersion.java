package apprenda.clientservices.api;

import com.google.gson.annotations.SerializedName;

public class CurrentVersion {
    public CurrentVersion() {
    }

    @SerializedName("name")
    public String Name;

    @SerializedName("alias")
    public String Alias;

    @SerializedName("description")
    public String Description;

    @SerializedName("stage")
    public String Stage;
}
