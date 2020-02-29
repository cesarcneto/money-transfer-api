package io.cesarcneto.moneytransfer.shared.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.lang.String.valueOf;
import static java.lang.System.getenv;

@Slf4j
public abstract class ApplicationPropertiesService {

    private static Properties properties;

    static {
        initProperties();
    }

    public static String getProperty(String property) {
        String envProperty = getenv(valueOf(property).replaceAll("\\.", "_").toUpperCase());
        if (envProperty!=null) return envProperty;

        String sysProperty = System.getProperty(property);
        if (sysProperty != null) return sysProperty;

        String appProperty = properties.getProperty(property);
        if (appProperty != null) return appProperty;
        else throw new RuntimeException(String.format("Property %s not found!", property));
    }

    private static void initProperties() {
        properties = new Properties();
        try(InputStream resourceAsStream = getSystemResourceAsStream("application.properties")){
            if(resourceAsStream != null) {
                properties.load(resourceAsStream);
            } else {
                log.error("Application properties was NOT LOADED");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
