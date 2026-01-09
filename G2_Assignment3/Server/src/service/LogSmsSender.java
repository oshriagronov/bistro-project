package service;

public class LogSmsSender implements MessageSender {

    @Override
    public String send(String phone, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[SMS LOG] To: ");
        sb.append(phone);
        sb.append("\n[Message]: ");
        sb.append(message);
        return sb.toString();
    }
}
