package apprenda.clientservices.tasks;

/**
 * Created by jvanbrackel on 1/27/2016.
 */
public class Response {
    public Response(int httpStatus, String responseBody, int expectedResponse) {
        HttpStatus = httpStatus;
        ResponseBody = responseBody;
        ExpectedResponse = expectedResponse;
    }

    public int HttpStatus;
    public String ResponseBody;
    public int ExpectedResponse;

    public boolean wasExpectedResponse() {
        return HttpStatus == ExpectedResponse;
    }
}
