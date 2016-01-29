package apprenda.clientservices.tasks;

import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Response {
    public Response(int httpStatus, HttpEntity entity, int expectedResponse) throws IOException {
        HttpStatus = httpStatus;
        ResponseBody = parseResponseBody(entity);
        ExpectedResponse = expectedResponse;
    }

    public int HttpStatus;
    public String ResponseBody;
    public int ExpectedResponse;

    public boolean wasExpectedResponse() {
        return HttpStatus == ExpectedResponse;
    }

    private String parseResponseBody(HttpEntity entity) throws IOException {
        String returnValue = null;

        if (entity != null && entity.getContentLength() > 0) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            returnValue = sb.toString();
        }

        return returnValue;
    }
}
