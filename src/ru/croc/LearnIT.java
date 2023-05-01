package ru.croc;


import java.sql.*;
import java.util.Scanner;

public class LearnIT {
    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        // Очитска консоли
        cls();

        // Подключение к бд
        String url = "jdbc:h2:tcp://192.168.56.1:9092/D:/JavaCourseProject/db/learnit";

        try (Connection connection = DriverManager.getConnection(url, "admin", "")) {
            System.out.println("Консольное приложение для расставления слов в правильном порядке");

            // Получение логина от пользователя
            System.out.print("Введите логин: ");
            Scanner in = new Scanner(System.in);
            String login = in.nextLine();

            cls();

            // Проверка пользователя
            String sql = "SELECT r.ROLE_ID, u.USER_ID FROM ROLEDB r, USERDB u WHERE u.ROLE_ID = r.ROLE_ID AND u.LOGIN = ?";
            try (PreparedStatement preparedStatementLogin = connection.prepareStatement(sql)) {
                preparedStatementLogin.setString(1, login);
                try (ResultSet resultSetUser = preparedStatementLogin.executeQuery()) {
                    // Если есть записи в бд
                    if (resultSetUser.next()) {
                        int role_id = resultSetUser.getInt(1);
                        int user_id = resultSetUser.getInt(2);
                        // Вход под администратором
                        if (role_id == 1) {
                            System.out.println("Вы вошли как администратор");
                            Administrator administrator = new Administrator(connection, login, user_id);
                            Thread.sleep(1000);
                            administrator.run();
                        }
                        // Вход под пользователем
                        else {
                            System.out.println("Вы вошли как пользователь");
                            User user = new User(connection, login, user_id);
                            Thread.sleep(1000);
                            user.run();
                        }
                    }
                    // Логин не найден в бд
                    else {
                        // Создаем пользователя
                        sql = "INSERT INTO UserDB (Login, Role_ID) VALUES (?, ?)";
                        try (PreparedStatement preparedStatementUser = connection.prepareStatement(sql)) {
                            preparedStatementUser.setString(1, login);
                            preparedStatementUser.setInt(2, 2);
                            int result = preparedStatementUser.executeUpdate();
                            // Получаем ID пользователя
                            sql = "SELECT u.USER_ID FROM USERDB u WHERE u.LOGIN = ?";
                            try (PreparedStatement preparedStatementNewUser = connection.prepareStatement(sql)) {
                                preparedStatementNewUser.setString(1, login);
                                try (ResultSet resultSetNewUser = preparedStatementNewUser.executeQuery()) {
                                    System.out.println("Добро пожаловать новый пользователь");
                                    resultSetNewUser.next();
                                    int user_id = resultSetNewUser.getInt(1);
                                    User user = new User(connection, login, user_id);
                                    Thread.sleep(1000);
                                    user.run();
                                }
                            }
                        }
                    }
                }
            }

            in.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}
