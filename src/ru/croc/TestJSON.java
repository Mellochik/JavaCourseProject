package ru.croc;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Класс для перобразования данных в JSON-файл
 */
@JsonPropertyOrder({"sentence", "theme", "difficulty"})
public class TestJSON {
    private String sentence;
    private String theme;
    private String difficulty;

    /**
     * Конструктор класса
     */
    public TestJSON() {
    }

    /**
     * Метод возвращающий предложение
     *
     * @return предложение
     */
    public String getSentence() {
        return sentence;
    }

    /**
     * Метод для устанавилвающий предложение
     *
     * @param sentence предложение
     */
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    /**
     * Метод возвращающий тему
     *
     * @return тема
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Метод устанавилвающий тему
     *
     * @param theme тема
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Метод возвращающий сложность
     *
     * @return сложность
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * Метод устанавливающий слодность
     *
     * @param difficulty сложность
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}