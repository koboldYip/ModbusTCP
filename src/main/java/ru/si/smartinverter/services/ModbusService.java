package ru.si.smartinverter.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.si.smartinverter.model.ModbusSignal;
import ru.si.smartinverter.scheduled.ModbusGate;

@Slf4j
@Service
public class ModbusService implements MeasurementService {

    @Autowired
    private ModbusGate gate;

    @Override
    public boolean setMeasurement(ModbusSignal ms) {
        if (gate.getConfiguration().getSignals().contains(ms)) {
            gate.writeValue(ms);
            return true;
        }
        return false;
    }

    @Override
    public ModbusSignal getMeasurement(ModbusSignal ms) {
        return gate.getConfiguration()
                .getSignals().stream()
                .filter(modbusSignal -> modbusSignal.equals(ms))
                .findAny().orElse(null);
    }
}
