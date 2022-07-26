package ru.si.smartinverter.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModbusSignal {
    @JacksonXmlProperty(isAttribute = true)
    private String id;
    @JacksonXmlProperty(isAttribute = true)
    private RegisterType registerType;
    @JacksonXmlProperty(isAttribute = true)
    private int initialRegister;
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    private float value;

    public ModbusSignal(String id, RegisterType registerType, int initialRegister, String type) {
        this.id = id;
        this.registerType = registerType;
        this.initialRegister = initialRegister;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModbusSignal that = (ModbusSignal) o;
        return getInitialRegister() == that.getInitialRegister() && getId().equals(that.getId()) && getRegisterType() == that.getRegisterType() && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getRegisterType(), getInitialRegister(), getType());
    }
}
