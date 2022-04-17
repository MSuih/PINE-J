package pinej;

import pinej.internal.PineAccessorImpl;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Client for accessing the underlying interface in OOP-friendly way.
 */
public class PineClient implements AutoCloseable {
    private static final int STATE_SLOTS = 4;

    private final PineAccessor pineAccessor;

    private final MemoryAddress ipcStruct;

    private final State[] states = IntStream.range(0, STATE_SLOTS)
            .mapToObj(State::new)
            .toArray(State[]::new);

    private final PineEnums.TargetPlatform target;

    public PineClient(Path path, PineEnums.TargetPlatform targetPlatform) {
        this.pineAccessor = new PineAccessorImpl(path);
        this.target = targetPlatform ;
        ipcStruct = switch (target) {
            case PS2 -> pineAccessor.allocatePS2();
            case PS3 -> pineAccessor.allocatePS3();
        };
        checkError("Allocate");
    }

    private String toString(MemoryAddress address) {
        String s = CLinker.toJavaString(address);
        pineAccessor.freeDataStream(address);
        return s;
    }

    public PineEnums.Status getStatus() {
        PineEnums.Status from = PineEnums.Status.from(pineAccessor.emuStatus(ipcStruct, (byte) 0));
        if (getError() != PineEnums.Error.SUCCESS) {
            return PineEnums.Status.ERROR;
        }
        return from;
    }

    public PineEnums.Error getError() {
        return PineEnums.Error.from(pineAccessor.getError(ipcStruct));
    }

    public State getStateSlot(int i) {
        return states[i];
    }

    public Optional<GameInfo> getGameInfo() {
        List<Supplier<MemoryAddress>> functions = List.of(
                () -> pineAccessor.getGameTitle(ipcStruct, (byte) 0),
                () -> pineAccessor.getGameId(ipcStruct, (byte) 0),
                () -> pineAccessor.getGameUuid(ipcStruct, (byte) 0),
                () -> pineAccessor.getGameVersion(ipcStruct, (byte) 0));
        List<String> strings = new ArrayList<>(4);
        for (Supplier<MemoryAddress> function : functions) {
            MemoryAddress a = function.get();
            if (getError() != PineEnums.Error.SUCCESS) {
                return Optional.empty();
            }
            strings.add(toString(a));
        }
        return Optional.of(new GameInfo(strings.get(0), strings.get(1), strings.get(2), strings.get(3)));
    }

    private long read(int address, PineEnums.Command command) {
        return pineAccessor.read(ipcStruct, address, command.toByte(), (byte) 0);
    }

    public byte readByte(int address) {
        return (byte) read(address, PineEnums.Command.READ_8);
    }

    public char readChar(int address) {
        return (char) read(address, PineEnums.Command.READ_16);
    }

    public int readInt(int address) {
        return (int) read(address, PineEnums.Command.READ_32);
    }

    public long readLong(int address) {
        return read(address, PineEnums.Command.READ_64);
    }

    public float readFloat(int address) {
        return Float.intBitsToFloat((int) read(address, PineEnums.Command.READ_32));
    }

    public double readDouble(int address) {
        return Double.longBitsToDouble(read(address, PineEnums.Command.READ_64));
    }

    private void write(int address, long value, PineEnums.Command command) {
        pineAccessor.write(ipcStruct, address, value, command.toByte(), (byte) 0);
    }

    public void write(int address, byte value) {
        write(address, value, PineEnums.Command.WRITE_8);
    }

    public void write(int address, char value) {
        write(address, value, PineEnums.Command.WRITE_16);
    }

    public void write(int address, int value) {
        write(address, value, PineEnums.Command.WRITE_32);
    }

    public void write(int address, long value) {
        write(address, value, PineEnums.Command.WRITE_64);
    }

    public void write(int address, float value) {
        write(address, Float.floatToRawIntBits(value), PineEnums.Command.WRITE_32);
    }

    public void write(int address, double value) {
        write(address, Double.doubleToRawLongBits(value), PineEnums.Command.WRITE_64);
    }

    @Override
    public void close() {
        switch (target) {
            case PS2 -> pineAccessor.deletePS2(ipcStruct);
            case PS3 -> pineAccessor.deletePS3(ipcStruct);
        }
    }


    public class State {
        private final byte id;

        private State(int id) {
            this.id = (byte) id;
        }


        public void load() {
            pineAccessor.saveState(ipcStruct, id, (byte) 0);
            checkError("StateLoad");
        }

        public void save() {
            pineAccessor.loadState(ipcStruct, id, (byte) 0);
            checkError("StateSave");
        }
    }

    private void checkError(String function) {
        PineEnums.Error error = getError();
        if (error != PineEnums.Error.SUCCESS) {
            throw new IllegalStateException("Command failed! Function: %s Error: %s".formatted(function, error));
        }
    }

    public record GameInfo(String title, String id, String uuid, String version) { }
}
