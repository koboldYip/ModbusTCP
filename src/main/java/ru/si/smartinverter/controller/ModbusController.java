package ru.si.smartinverter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.si.smartinverter.model.ModbusSignal;
import ru.si.smartinverter.services.ModbusService;

@Slf4j
@RestController
public class ModbusController {

    @Autowired
    private ModbusService modbusService;


    @GetMapping("/get")
    public ModbusSignal getModbusValue(@RequestBody ModbusSignal signal) {
        return modbusService.getMeasurement(signal);
    }

    @PostMapping("/post")
    public boolean setModbusValue(@RequestBody ModbusSignal signal) {
        return modbusService.setMeasurement(signal);
    }
}
