package ru.croc;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Класс хронящий в себе тесты для XML-файла
 */
@XmlRootElement(name = "testSet")
public class TestSet {

    private List<TestXML> tests;

    /**
     * Метод возвращающий тесты
     * @return тесты
     */
    @XmlElement(name = "test")
    public List<TestXML> getTests() {
        return tests;
    }

    /**
     * Метод устанавилвающий тесты
     * @param tests тесты
     */
    public void setTests(List<TestXML> tests) {
        this.tests = tests;
    }
}