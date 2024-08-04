package voicelog.voicelog.common;

public interface ResponseMessage {
    //HTTP 200
    String SUCCESS = "success";

    //HTTP 400
    String DUPLICATE_EMAIL = "duplicate email";
    String CERTIFICATE_FAIL = "certification failed";
    String INVALID_FILE = "invalid file";
    String DUPLICATE_DIARY = "duplicate diary";

    //HTTP 401
    String SIGN_IN_FAIL = "Login information mismatch";
    String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    String AUTHORIZATION_FAIL = "Authorization Failed.";

    //HTTP 403
    String NO_PERMISSION = "Do not have permission.";

    //HTTP 500
    String DATABASE_ERROR = "database error";
    String MAIL_FAIL = "mail send failed";
    String STT_FAIL = "STT error";
    String GPT_FAIL = "ChatGPT error";
}
