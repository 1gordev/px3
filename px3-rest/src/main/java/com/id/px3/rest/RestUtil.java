package com.id.px3.rest;

import com.id.px3.error.PxException;
import com.id.px3.model.auth.BasicAuth;
import org.springframework.http.HttpStatus;

import java.util.Base64;

public class RestUtil {

    /**
     * Extracts the username and password from the Basic Auth header
     *
     * @param authHeader the Basic Auth header
     * @return the extracted username and password
     */
    public static BasicAuth extractBasicAuth(String authHeader) {
        //  extract base64 encoded username and password from the Basic Auth header
        if (!authHeader.startsWith("Basic ")) {
            throw new PxException(HttpStatus.UNAUTHORIZED, "Invalid authentication method");
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded);

        //  credentials = username:password
        final String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            throw new PxException(HttpStatus.UNAUTHORIZED, "Invalid authentication format");
        }

        return new BasicAuth(values[0], values[1]);
    }

}
