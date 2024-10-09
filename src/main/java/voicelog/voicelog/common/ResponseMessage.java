package voicelog.voicelog.common;

public interface ResponseMessage {
    //HTTP 200
    String SUCCESS = "success";

    //HTTP 400
    String DUPLICATE_EMAIL = "duplicate email";
    String CERTIFICATE_FAIL = "certification failed";
    String INVALID_FILE = "invalid file";
    String DUPLICATE_DIARY = "duplicate diary";
    String NOT_EXIST_DIARY = "not exist diary";
    String NOT_INVALID_PASSWORD = "not invalid password";
    String PASSWORD_REUSE = "reused password";
    String NOT_EXIST_USER = "not exist user";

    //HTTP 402
    String COIN_REQUIRED = "not enough coin";

    //HTTP 401
    String SIGN_IN_FAIL = "Login information mismatch";
    String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    String AUTHORIZATION_FAIL = "Authorization Failed.";

    //HTTP 403
    String NO_PERMISSION = "Do not have permission.";

    //HTTP 404
    String NOT_FOUND = "Not Found resource";

    //HTTP 500
    String DATABASE_ERROR = "database error";
    String MAIL_FAIL = "mail send failed";
    String STT_FAIL = "STT error";
    String GPT_FAIL = "ChatGPT error";
}
