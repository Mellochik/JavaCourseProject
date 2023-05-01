package ru.croc;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * Класс для перобразования данных из XML-файла
 */
public class TestXML {

    private String sentence;
    private int theme;
    private int difficulty;

    /**
     * Метод возвращающий предложение
     *
     * @return предложение
     */
    @XmlElement(name = "sentence")
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
    @XmlElement(name = "theme")
    public int getThemeNumber() {
        return theme;
    }

    /**
     * Метод устанавилвающий тему
     *
     * @param theme тема
     */
    public void setThemeNumber(int theme) {
        this.theme = theme;
    }

    /**
     * Метод возвращающий сложность
     *
     * @return сложность
     */
    @XmlElement(name = "difficulty")
    public int getDifficultyNumber() {
        return difficulty;
    }

    /**
     * Метод устанавливающий слодность
     *
     * @param difficulty сложность
     */
    public void setDifficultyNumber(int difficulty) {
        this.difficulty = difficulty;
    }
}
