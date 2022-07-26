package ru.si.smartinverter.configuration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import ru.si.smartinverter.model.ModbusSignal;

import java.util.List;

@Data
@JacksonXmlRootElement
public class Configuration {

    @JacksonXmlProperty(localName = "modbusSignal")
    @JacksonXmlElementWrapper(localName = "signals")
    private List<ModbusSignal> signals;
    private int pollingDelay;
    private int timeOut;
    private String address;
    private int port;

}
