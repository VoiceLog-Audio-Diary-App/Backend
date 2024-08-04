package voicelog.voicelog.common;

public interface ResponseCode {
    //HTTP 200
    String SUCCESS = "SU";

    //HTTP 400
    String DUPLICATE_EMAIL = "DE";
    String CERTIFICATE_FAIL = "CF";
    String INVALID_FILE = "IF";
    String DUPLICATE_DIARY = "DD";

    //HTTP 401
    String SIGN_IN_FAIL = "SF";
    String INVALID_REFRESH_TOKEN = "IRT";
    String AUTHORIZATION_FAIL = "AF";

    //HTTP 403
    String NO_PERMISSION = "NP";

    //HTTP 500
    String DATABASE_ERROR = "DBE";
    String MAIL_FAIL = "MF";
    String STT_FAIL = "SF";
    String GPT_FAIL = "GF";
}
