package service;

public class NotificationService {

    private static NotificationService instance;
    private MessageSender smsSender;
    private MessageSender emailSender;

    private NotificationService() {
        this.smsSender = new LogSmsSender();
        this.emailSender = new LogEmailSender();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public String sendSmsMessage(String phone, String message) {
        return smsSender.send(phone, message);
    }
    public String sendEmailMessage(String email, String message){
        return emailSender.send(email, message);
    }
}
