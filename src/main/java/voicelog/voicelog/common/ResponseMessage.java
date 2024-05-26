package voicelog.voicelog.common;

public interface ResponseMessage {
    String SUCCESS = "success";

    String DUPLICATE_EMAIL = "duplicate email";

    String DATABASE_ERROR = "database error";
    String MAIL_FAIL = "mail send failed";
}
