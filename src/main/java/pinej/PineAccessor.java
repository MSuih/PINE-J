package pinej;

import jdk.incubator.foreign.MemoryAddress;

/**
 * Interface for accessing the C language bindings of the underlying PINE interface.
 */
public interface PineAccessor {

    MemoryAddress allocatePS2();

    MemoryAddress allocatePS3();

    void initializeBatch(MemoryAddress ipc);

    void freeDataStream(MemoryAddress ipc);

    int finalizeBatch(MemoryAddress ipc);

    long getReply(MemoryAddress ipc, int cmd, int place, byte command);

    void sendCommand(MemoryAddress ipc, int command);

    long read(MemoryAddress ipc, int address, byte command, byte batch);

    MemoryAddress version(MemoryAddress ipc, byte batch);

    int emuStatus(MemoryAddress ipc, byte batch);

    MemoryAddress getGameTitle(MemoryAddress ipc, byte batch);

    MemoryAddress getGameId(MemoryAddress ipc, byte batch);

    MemoryAddress getGameUuid(MemoryAddress ipc, byte batch);

    MemoryAddress getGameVersion(MemoryAddress ipc, byte batch);

    void saveState(MemoryAddress ipc, byte slot, byte batch);

    void loadState(MemoryAddress ipc, byte slot, byte batch);

    void write(MemoryAddress ipc, int address, long value, byte command, byte batch);

    void deletePS2(MemoryAddress ipc);

    void deletePS3(MemoryAddress ipc);

    void freeBatchCommand(int cmd);

    int getError(MemoryAddress ipc);
}