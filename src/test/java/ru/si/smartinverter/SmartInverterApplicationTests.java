package ru.si.smartinverter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.si.smartinverter.configuration.Configuration;

import java.io.File;

@SpringBootTest
class SmartInverterApplicationTests {

    @SneakyThrows
    @Test
    void contextLoads() {
        XmlMapper xmlMapper = new XmlMapper();
        Configuration value = xmlMapper.readValue(new File("src/main/java/ru/si/smartinverter/configuration/config"), Configuration.class);
        System.out.println("value = " + value);
    }

}
