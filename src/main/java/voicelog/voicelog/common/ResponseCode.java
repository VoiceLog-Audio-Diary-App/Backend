package voicelog.voicelog.common;

public interface ResponseCode {
    //HTTP 200
    String SUCCESS = "SU";

    //HTTP 400
    String DUPLICATE_EMAIL = "DE";
    String CERTIFICATE_FAIL = "CF";

    //HTTP 401
    String SIGN_IN_FAIL = "SF";

    //HTTP 500
    String DATABASE_ERROR = "DBE";
    String MAIL_FAIL = "MF";
}
