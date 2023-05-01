package ru.croc;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс пользователя
 */
public class User implements Run {
    private final Connection connection;
    private final String login;
    private final int id;

    /**
     * Конструктор класса пользователь
     *
     * @param connection соедниение
     * @param login      логин пользователя
     * @param id         ID пользователя
     */
    public User(Connection connection, String login, int id) {
        this.connection = connection;
        this.login = login;
        this.id = id;
    }

    /**
     * Очистка консоли
     */
    public static void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Метод запуска выбора выполнения функций
     */
    @Override
    public void run() {
        // Ввод команд пользователя
        Scanner in = new Scanner(System.in);
        while (true) {
            // Очистка консоли
            cls();

            // Команды администратора
            System.out.println("1. Начать тестирование");
            System.out.println("----------------------------");
            System.out.println("2. Выход");

            // Команда пользователя
            System.out.print("Введите комманду: ");
            int command = in.nextInt();
            if (command == 1) {
                runTests();
            } else {
                System.exit(0);
            }
        }
    }

    /**
     * Метод запуска тестов
     */
    public void runTests() {
        //Очистка консоли
        cls();

        // Получение всех тестов не пройденных пользователем
        Map<Integer, String[]> tests = new HashMap<>();
        String sql = "SELECT t.TEST_ID, t.SENTENCE, t2.THEME, d.DIFFICULTY " +
                "FROM Test t, USERDB u, DIFFICULTY d, THEME t2 " +
                "WHERE NOT EXISTS (SELECT * FROM COMPLETEDTEST c WHERE c.TEST_ID  = t.TEST_ID AND u.USER_ID = c.USER_ID)" +
                "AND u.LOGIN = ? " +
                "AND t.THEME_ID  = t2.THEME_ID " +
                "AND t.DIFFICULTY_ID = d.DIFFICULTY_ID " +
                "ORDER BY t.TEST_ID";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Если есть тесты дял пользователя
                while (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    String sentence = resultSet.getString(2);
                    String theme = resultSet.getString(3);
                    String difficulty = resultSet.getString(4);
                    tests.put(id, Arrays.asList(sentence, theme, difficulty).toArray(new String[0]));
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Проверка на наличие тестов
        if (tests.size() != 0) {
            Scanner in = new Scanner(System.in);
            boolean run = true;
            while (run) {
                // Количетсво попыток
                int count = 1;

                // Получение случайного теста из tests
                Random random = new Random();
                Integer[] keys = tests.keySet().toArray(new Integer[0]);
                int randomIndex = random.nextInt(keys.length);
                int key = keys[randomIndex];
                String sentence = tests.get(key)[0];
                String theme = tests.get(key)[1];
                String difficulty = tests.get(key)[2];

                // Вывод информации о предложении
                System.out.printf("Тест №%d по теме %s с сложностью %s\n", key, theme, difficulty);

                // Разбиение строки
                String[] parseSentence = sentence.split("\\s+");
                Collections.shuffle(Arrays.asList(parseSentence));
                for (String value : parseSentence) {
                    System.out.printf("%s ", value);
                }
                System.out.print("\n");

                // Прохождение теста
                System.out.print("Введите ответ: ");
                while (true) {
                    String answer = in.nextLine();
                    if (sentence.equals(answer)) {
                        sql = "INSERT INTO COMPLETEDTEST (COUNT_COMPLETE, DATE_COMPLETE, USER_ID, TEST_ID) " +
                                "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                            var dateTime = LocalDateTime.now();
                            preparedStatement.setInt(1, count);
                            preparedStatement.setTimestamp(2, Timestamp.valueOf(dateTime));
                            preparedStatement.setInt(3, id);
                            preparedStatement.setInt(4, key);
                            int result = preparedStatement.executeUpdate();
                            if (result == 1) {
                                System.out.println("Успешно прошли тест");
                                tests.remove(key);
                                System.out.print("Еще один тест? Если да, введите 'y', иначе любой другой сивол: ");
                                String line = in.nextLine();
                                if (!"y".equals(line)) {
                                    run = false;
                                }
                                break;
                            } else {
                                System.out.println("Не получилось сохранить прохождение теста");
                            }
                        } catch (SQLException e) {
                            System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        }
                    } else {
                        System.out.print("Попробуйте еще раз: ");
                        count++;
                    }
                }
            }
        } else {
            System.out.println("Тесты для вас закончились, ожидайте новых тестов в будущем");
        }

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        Scanner in = new Scanner(System.in);
        String next = in.nextLine();
    }
}
