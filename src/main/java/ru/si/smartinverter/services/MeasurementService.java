package ru.si.smartinverter.services;

import ru.si.smartinverter.model.ModbusSignal;

public interface MeasurementService {

    boolean setMeasurement(ModbusSignal ms);

    ModbusSignal getMeasurement(ModbusSignal ms);
}
