package apprenda.clientservices.api;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {
    public AuthenticationResponse() {
    }

    @SerializedName("apprendaSessionToken")
    public String ApprendaSessionToken;

    @SerializedName("href")
    public String Href;
}
