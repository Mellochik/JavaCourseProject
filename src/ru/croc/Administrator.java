package ru.croc;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * Класс администратора
 */
public class Administrator implements Run {
    private final Connection connection;
    private final String login;
    private final int id;

    /**
     * Конструктор класса администратора
     *
     * @param connection соедниение
     * @param login      логин пользователя
     * @param id         ID пользователя
     */
    public Administrator(Connection connection, String login, int id) {
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
    public void run() throws IOException {
        // Ввод команд пользователя
        Scanner in = new Scanner(System.in);
        while (true) {
            // Очистка консоли
            cls();

            // Команды администратора
            System.out.println("1. Начать тестирование");
            System.out.println("2. Добавить тест в БД");
            System.out.println("3. Вывести все тесты из БД");
            System.out.println("4. Удалить тест");
            System.out.println("5. Импорт тестов из XML-файла");
            System.out.println("6. Экспорт тестов в JSON-файл");
            System.out.println("----------------------------");
            System.out.println("7. Выход");

            // Команда пользователя
            System.out.print("Введите комманду: ");
            int command = in.nextInt();
            if (command == 1) {
                runTests();
            } else if (command == 2) {
                createTest();
            } else if (command == 3) {
                readTests();
            } else if (command == 4) {
                deleteTest();
            } else if (command == 5) {
                importFromXML();
            } else if (command == 6) {
                exportToJSON();
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
            System.out.println("Считывание тестов не удалось!!!");
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

    /**
     * Метод добавляющий в базу данных новый тест
     */
    public void createTest() {
        // Очитска экрана
        cls();

        //Формирование таблицы тем
        System.out.println("Таблица тем");
        System.out.printf("%2s|%20s\n", "ID", "Theme");
        String sql = "SELECT t.THEME_ID, t.THEME FROM THEME t ORDER BY t.THEME_ID";
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        String theme = resultSet.getString(2);
                        String format = String.format("%2d|%20s", id, theme);
                        System.out.println(format);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Формирование таблицы сложности
        System.out.println("Таблица сложности");
        System.out.printf("%2s|%10s\n", "ID", "Difficulty");
        sql = "SELECT d.DIFFICULTY_ID, d.DIFFICULTY FROM DIFFICULTY d ORDER BY d.DIFFICULTY_ID";
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        String difficulty = resultSet.getString(2);
                        String format = String.format("%2d|%10s", id, difficulty);
                        System.out.println(format);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Добавление теста
        System.out.println("Введите тест разделяя поля точной с запятой (Предолжение;Номер темы;Номер сложности):");
        Scanner in = new Scanner(System.in);
        String line = in.nextLine();
        sql = "INSERT INTO Test (Sentence, THEME_ID, DIFFICULTY_ID) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String[] parse = line.split(";");
            preparedStatement.setString(1, parse[0]);
            preparedStatement.setInt(2, Integer.parseInt(parse[1]));
            preparedStatement.setInt(3, Integer.parseInt(parse[2]));
            int result = preparedStatement.executeUpdate();
            if (result == 1) {
                System.out.println("Успешно добавили новый тест");
            } else {
                System.out.println("Тест не добавлен");
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        in = new Scanner(System.in);
        String next = in.nextLine();
    }

    /**
     * Метод считывающий все тесты с базы данных
     */
    public void readTests() {
        // Очитска экрана
        cls();

        //Формирование таблицы
        System.out.println("Таблица тестов");
        System.out.printf("%3s|%75s|%20s|%10s\n", "ID", "Sentence", "Theme", "Difficulty");
        // Получение всех тестов
        String sql = "SELECT t.TEST_ID, t.SENTENCE, t2.THEME, d.DIFFICULTY FROM TEST t, THEME t2, DIFFICULTY d " +
                "WHERE t.THEME_ID = t2.THEME_ID AND t.DIFFICULTY_ID  = d.DIFFICULTY_ID";
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        String test = resultSet.getString(2);
                        String theme = resultSet.getString(3);
                        String difficulty = resultSet.getString(4);
                        String format = String.format("%3d|%75s|%20s|%10s", id, test, theme, difficulty);
                        System.out.println(format);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        Scanner in = new Scanner(System.in);
        String next = in.nextLine();
    }

    /**
     * Метод удаляющий тест в базе данных
     */
    public void deleteTest() {
        // Очитска экрана
        cls();

        // Удаление теста
        System.out.print("Введите ID теста, который хотите удалить: ");
        Scanner in = new Scanner(System.in);
        int id = in.nextInt();
        String sql = "DELETE FROM TEST t WHERE t.TEST_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            int result = preparedStatement.executeUpdate();
            if (result == 1) {
                System.out.println("Удаление произошло успешно");
            } else {
                System.out.println("Произошла ошибка");
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        in = new Scanner(System.in);
        String next = in.nextLine();
    }

    /**
     * Метод извлекающий из XML-файла тесты
     */
    public void importFromXML() {
        // Очистка консоли
        cls();

        Scanner in = new Scanner(System.in);
        System.out.print("Введите путь к файлу: ");
        String filePath = in.nextLine();

        try {
            // Создаем контекст JAXB для класса TestSet
            JAXBContext jaxbContext = JAXBContext.newInstance(TestSet.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            TestSet testSet = (TestSet) unmarshaller.unmarshal(new File(filePath));
            List<TestXML> tests = testSet.getTests();

            // Обрабатываем каждый тест
            for (TestXML test : tests) {
                // Получаем данные теста
                String sentence = test.getSentence();
                int themeNumber = test.getThemeNumber();
                int difficultyNumber = test.getDifficultyNumber();

                String sql = "INSERT INTO Test (Sentence, THEME_ID, DIFFICULTY_ID) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, sentence);
                    preparedStatement.setInt(2, themeNumber);
                    preparedStatement.setInt(3, difficultyNumber);
                    int result = preparedStatement.executeUpdate();
                    if (result == 1) {
                        System.out.println("Успешно добавили новый тест");
                    } else {
                        System.out.println("Тест не добавлен");
                    }
                } catch (SQLException e) {
                    System.out.println("Попытка добовления уже имеющихся тестов!!!");
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        in = new Scanner(System.in);
        String next = in.nextLine();
    }

    /**
     * Метод сохраняющий все тесты в JSON-файл
     *
     * @throws IOException ошибка открытия файла
     */
    public void exportToJSON() throws IOException {
        // Очистка консоли
        cls();

        // Получение пути от пользователя
        Scanner in = new Scanner(System.in);
        System.out.print("Введите путь к файлу: ");
        String filePath = in.nextLine();

        // Создание списка тестов
        ArrayList<TestJSON> tests = new ArrayList<>();

        String sql = "SELECT t.SENTENCE, t2.THEME, d.DIFFICULTY FROM TEST t, THEME t2, DIFFICULTY d " +
                "WHERE t.THEME_ID = t2.THEME_ID AND t.DIFFICULTY_ID  = d.DIFFICULTY_ID";
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        String sentence = resultSet.getString(1);
                        String theme = resultSet.getString(2);
                        String difficulty = resultSet.getString(3);
                        TestJSON test = new TestJSON();
                        test.setSentence(sentence);
                        test.setTheme(theme);
                        test.setDifficulty(difficulty);
                        tests.add(test);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        // Сохранение в JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), tests);

        // Ожидание нажатия любой клавиши
        System.out.println("Нажмите 'enter', чтобы вернуться");
        in = new Scanner(System.in);
        String next = in.nextLine();
    }
}