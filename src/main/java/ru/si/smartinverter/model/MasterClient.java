package ru.si.smartinverter.model;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadCoilsRequest;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.responses.ReadCoilsResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.SneakyThrows;
import ru.si.smartinverter.configuration.Configuration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Data
public class MasterClient {

    private Configuration configuration;
    private int id;
    CompletableFuture<ReadCoilsResponse> futureCoils;
    CompletableFuture<ReadHoldingRegistersResponse> futureHoldingRegisters;
    ModbusTcpMaster master;

    public MasterClient() {
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder("localhost")
                .setPort(502)
                .setTimeout(Duration.ofSeconds(10))
                .build();

        master = new ModbusTcpMaster(config);
        master.connect();
    }

    @SneakyThrows
    public void getHoldingRegister(ModbusSignal ms) {
        futureHoldingRegisters = master.sendRequest(new ReadHoldingRegistersRequest(ms.getInitialRegister(), 1), 0);
        futureHoldingRegisters.thenAccept(response -> {
            System.out.println("ByteBuffer.wrap(response.getRegisters().array()).getDouble() = " + ByteBuffer.wrap(response.getRegisters().array()).getDouble());
//            ms.setValue(ByteBuffer.wrap(response.getRegisters().array()).getDouble());
            ReferenceCountUtil.release(response);
        });
    }

    @SneakyThrows
    public void getCoils(ModbusSignal ms) {
        futureCoils = master.sendRequest(new ReadCoilsRequest(ms.getInitialRegister(), 1), 0);
        futureCoils.thenAccept(response -> {
//            ms.setValue(Double.parseDouble(ByteBufUtil.hexDump(response.getCoilStatus())));
            ReferenceCountUtil.release(response);
        });
    }

}