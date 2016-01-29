package apprenda.clientservices.api;

import com.google.gson.annotations.SerializedName;

public class Section {
    @SerializedName("title")
    public String Title;

    @SerializedName("success")
    public boolean Success;

    @SerializedName("messages")
    public Message[] Messages;
}
