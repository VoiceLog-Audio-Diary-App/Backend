package voicelog.voicelog.common;

public interface ResponseMessage {
    //HTTP 200
    String SUCCESS = "success";

    //HTTP 400
    String DUPLICATE_EMAIL = "duplicate email";
    String MAIL_FAIL = "mail send failed";
    String CERTIFICATE_FAIL = "certification failed";

    //HTTP 401
    String SIGN_IN_FAIL = "Login information mismatch";
    String INVALID_REFRESH_TOKEN = "Invalid refresh token";

    //HTTP 500
    String DATABASE_ERROR = "database error";

}
