package ru.si.smartinverter.scheduled;

import com.digitalpetri.modbus.codec.Modbus;
import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.ReadCoilsResponse;
import com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.si.smartinverter.configuration.Configuration;
import ru.si.smartinverter.model.ModbusSignal;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Data
public class ModbusGate {

    private Configuration configuration;
    private CompletableFuture<ReadCoilsResponse> futureCoils;
    private CompletableFuture<ReadHoldingRegistersResponse> futureHoldingRegisters;
    private CompletableFuture<ReadDiscreteInputsResponse> futureDiscreteInputsResponse;
    private CompletableFuture<ReadInputRegistersResponse> futureInputRegistersResponse;
    private ModbusTcpMaster master;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @SneakyThrows
    public ModbusGate() {
        XmlMapper xmlMapper = new XmlMapper();
        configuration = xmlMapper.readValue(new File("src/main/java/ru/si/smartinverter/configuration/config"),
                Configuration.class);
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(configuration.getAddress())
                .setPort(configuration.getPort())
                .setTimeout(Duration.ofSeconds(configuration.getTimeOut()))
                .build();
        master = new ModbusTcpMaster(config);
        master.connect();
        modbusGetData();
    }

    @Async
    void modbusGetData() {
        scheduler.scheduleAtFixedRate(() ->
                        configuration.getSignals().forEach(signal -> {
                            switch (signal.getRegisterType().ordinal()) {
                                case 0 -> modbusGetCoil(signal);
                                case 1, 2 -> modbusGetAnalogueValues(signal);
                                case 3 -> modbusGetDiscreteInput(signal);
                            }
                        })
                , 1, configuration.getPollingDelay(), TimeUnit.SECONDS);
    }

    @Async
    void modbusGetCoil(ModbusSignal signal) {
        futureCoils =
                master.sendRequest(new ReadCoilsRequest(signal.getInitialRegister(), 1), 0);
        futureCoils.whenCompleteAsync((response, ex) -> {
            if (response != null) {
                expandValue(signal, response.content());
                ReferenceCountUtil.release(response);
            } else {
                log.error("Completed exceptionally, message={}", ex.getMessage(), ex);
            }
        }, Modbus.sharedExecutor());
    }

    @Async
    void modbusGetAnalogueValues(ModbusSignal signal) {
        if (signal.getRegisterType().ordinal() == 1) {
            modbusGetHoldingRegister(signal, signal.getType().equals("Short") ? 1 : 2);
        } else if (signal.getRegisterType().ordinal() == 2) {
            modbusGetInputRegister(signal, signal.getType().equals("Short") ? 1 : 2);
        }
    }

    @Async
    void modbusGetHoldingRegister(ModbusSignal signal, int length) {
        futureHoldingRegisters =
                master.sendRequest(new ReadHoldingRegistersRequest(signal.getInitialRegister(), length), 0);
        futureHoldingRegisters.whenCompleteAsync((response, ex) -> {
            if (response != null) {
                expandValue(signal, response.content());
                ReferenceCountUtil.release(response);
            } else {
                log.error("Completed exceptionally, message={}", ex.getMessage(), ex);
            }
        }, Modbus.sharedExecutor());
    }

    @Async
    void modbusGetInputRegister(ModbusSignal signal, int length) {
        futureInputRegistersResponse =
                master.sendRequest(new ReadInputRegistersRequest(signal.getInitialRegister(), length), 0);
        futureInputRegistersResponse.whenCompleteAsync((response, ex) -> {
            if (response != null) {
                expandValue(signal, response.content());
                ReferenceCountUtil.release(response);
            } else {
                log.error("Completed exceptionally, message={}", ex.getMessage(), ex);
            }
        }, Modbus.sharedExecutor());
    }

    @Async
    void modbusGetDiscreteInput(ModbusSignal signal) {
        futureDiscreteInputsResponse =
                master.sendRequest(new ReadDiscreteInputsRequest(signal.getInitialRegister(), 1), 0);
        futureDiscreteInputsResponse.whenCompleteAsync((response, ex) -> {
            if (response != null) {
                expandValue(signal, response.content());
                ReferenceCountUtil.release(response);
            } else {
                log.error("Completed exceptionally, message={}", ex.getMessage(), ex);
            }
        }, Modbus.sharedExecutor());
    }

    private void expandValue(ModbusSignal signal, ByteBuf content) {
        switch (signal.getType()) {
            case "Float" -> signal.setValue(content.getFloat(0));
            case "Integer" -> signal.setValue(content.getInt(0));
            case "Short" -> signal.setValue(content.getShort(0));
            case "Boolean" -> signal.setValue(content.getBoolean(0) ? 1 : 0);
            default -> log.error("Type ERROR expand");
        }
    }

    public void writeValue(ModbusSignal signal) {
        switch (signal.getRegisterType()) {
            case COIL -> master.sendRequest(new WriteSingleCoilRequest(signal.getInitialRegister(),
                    signal.getValue() == 1), 0);
            case HOLDING_REGISTER, INPUT_REGISTER -> master
                    .sendRequest(new WriteMultipleRegistersRequest(signal.getInitialRegister(),
                            signal.getType().equals("Short") ? 1 : 2, collapseValue(signal)), 0);

        }
    }

    private byte[] collapseValue(ModbusSignal signal) {
        switch (signal.getType()) {
            case "Float":
                return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(signal.getValue()).array();
            case "Integer":
                return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) signal.getValue()).array();
            case "Short":
                return ByteBuffer.allocate(2).putShort((short) signal.getValue()).array();
            default:
                log.error("Type ERROR Collapse");
                return new byte[]{0};
        }
    }

}
