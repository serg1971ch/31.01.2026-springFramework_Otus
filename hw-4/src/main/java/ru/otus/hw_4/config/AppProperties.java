package ru.otus.hw_4.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Map;

@ToString
@Getter
@Setter
@ConfigurationProperties(prefix = "test")
public class AppProperties implements TestConfig, TestFileNameProvider, LocaleConfig {

    private int rightAnswersCountToPass;

    private Locale locale;

    private Map<String, String> fileNameByLocaleTag;

    public void setLocale(String locale) {
        this.locale = Locale.forLanguageTag(locale);
    }

    @Override
    public String getTestFileName() {
        return fileNameByLocaleTag.get(locale.toLanguageTag());
    }
}

