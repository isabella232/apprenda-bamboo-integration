package apprenda.clientservices.api;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("severity")
    public String Severity;
    @SerializedName("message")
    public String Message;
}
