package voicelog.voicelog.common;

public interface ResponseMessage {
    //HTTP 200
    String SUCCESS = "success";

    //HTTP 400
    String DUPLICATE_EMAIL = "duplicate email";
    String MAIL_FAIL = "mail send failed";
    String CERTIFICATE_FAIL = "certification failed";

    //HTTP 500
    String DATABASE_ERROR = "database error";

}
