package com.bigfly.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类
 */
@Component
public class I18nUtils {

    private static MessageSource messageSource;

    public I18nUtils(MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息code
     * @return 国际化消息
     */
    public static String getMessage(String code) {
        return getMessage(code, (Object) null);
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息code
     * @param args 参数
     * @return 国际化消息
     */
    public static String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * 获取国际化消息（指定语言）
     *
     * @param code   消息code
     * @param locale 语言环境
     * @return 国际化消息
     */
    public static String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    /**
     * 获取国际化消息（指定语言和参数）
     *
     * @param code   消息code
     * @param locale 语言环境
     * @param args   参数
     * @return 国际化消息
     */
    public static String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }
}
