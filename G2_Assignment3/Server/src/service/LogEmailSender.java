package service;

public class LogEmailSender implements MessageSender {

    @Override
    public String send(String email, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[EMAIL LOG] To: ");
        sb.append(email);
        sb.append("\n[Message]: ");
        sb.append(message);
        return sb.toString();
    }
}
