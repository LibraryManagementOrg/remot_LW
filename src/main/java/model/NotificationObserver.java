package service;
import model.User;

public interface NotificationObserver {
    void update(User user, String message);
}