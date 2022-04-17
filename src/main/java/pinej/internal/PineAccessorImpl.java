package pinej.internal;

import pinej.PineAccessor;
import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;

import static pinej.internal.CheckedExceptionHelper.call;
import static jdk.incubator.foreign.CLinker.*;

/**
 * Implementation of IPC Accessor which uses Foreign Linker API to find and call the C functions.
 */
public class PineAccessorImpl implements PineAccessor {

    private final SymbolLookup ipcLibrary;

    private final MethodHandle allocatePS2;
    private final MethodHandle allocatePS3;
    private final MethodHandle initializeBatch;
    private final MethodHandle freeDataStream;
    private final MethodHandle finalizeBatch;
    private final MethodHandle getReply;
    private final MethodHandle sendCommand;
    private final MethodHandle read;
    private final MethodHandle version;
    private final MethodHandle emuStatus;
    private final MethodHandle getGameTitle;
    private final MethodHandle getGameId;
    private final MethodHandle getGameUuid;
    private final MethodHandle getGameVersion;
    private final MethodHandle saveState;
    private final MethodHandle loadState;
    private final MethodHandle write;
    private final MethodHandle deletePS2;
    private final MethodHandle deletePS3;
    private final MethodHandle freeBatchCommand;
    private final MethodHandle getError;

    public PineAccessorImpl(Path file) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Library does not exists in " + file.toAbsolutePath());
        }

        try {
            System.load(file.toAbsolutePath().toString());
            this.ipcLibrary = SymbolLookup.loaderLookup();
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalArgumentException("Could not load library from " + file.toAbsolutePath(), e);
        } catch (IllegalCallerException e) {
            throw new IllegalStateException("Could not create IPC library, make sure --enable-native-access is set", e);
        }
        CLinker cLinker = CLinker.getInstance();

        allocatePS2 = cLinker.downcallHandle(
                functionLookup("pcsx2_new"),
                MethodType.methodType(MemoryAddress.class),
                FunctionDescriptor.of(C_POINTER));
        allocatePS3 = cLinker.downcallHandle(
                functionLookup("rpcs3_new"),
                MethodType.methodType(MemoryAddress.class),
                FunctionDescriptor.of(C_POINTER));
        initializeBatch = cLinker.downcallHandle(
                functionLookup("initialize_batch"),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER));
        freeDataStream = cLinker.downcallHandle(
                functionLookup("free_datastream"),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER));
        finalizeBatch = cLinker.downcallHandle(
                functionLookup("finalize_batch"),
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(C_INT, C_POINTER));
        getReply = cLinker.downcallHandle(
                functionLookup("get_reply_int"),
                MethodType.methodType(long.class, MemoryAddress.class, int.class, int.class, byte.class),
                FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_INT, C_INT, C_CHAR));
        sendCommand = cLinker.downcallHandle(
                functionLookup("send_command"),
                MethodType.methodType(void.class, MemoryAddress.class, int.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_INT));
        read = cLinker.downcallHandle(
                functionLookup("read"),
                MethodType.methodType(long.class, MemoryAddress.class, int.class, byte.class, byte.class),
                FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_INT, C_CHAR, C_CHAR));
        version = cLinker.downcallHandle(
                functionLookup("version"),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_CHAR));
        emuStatus = cLinker.downcallHandle(
                functionLookup("status"),
                MethodType.methodType(int.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_INT, C_POINTER, C_CHAR));
        getGameTitle = cLinker.downcallHandle(
                functionLookup("getgametitle"),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_CHAR));
        getGameId = cLinker.downcallHandle(
                functionLookup("getgameid"),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_CHAR));
        getGameUuid = cLinker.downcallHandle(
                functionLookup("getgameuuid"),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_CHAR));
        getGameVersion = cLinker.downcallHandle(
                functionLookup("getgametitle"),
                MethodType.methodType(MemoryAddress.class, MemoryAddress.class, byte.class),
                FunctionDescriptor.of(C_POINTER, C_POINTER, C_CHAR));
        saveState = cLinker.downcallHandle(
                functionLookup("savestate"),
                MethodType.methodType(void.class, MemoryAddress.class, byte.class, byte.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_CHAR, C_CHAR));
        loadState = cLinker.downcallHandle(
                functionLookup("loadstate"),
                MethodType.methodType(void.class, MemoryAddress.class, byte.class, byte.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_CHAR, C_CHAR));
        write = cLinker.downcallHandle(
                functionLookup("write"),
                MethodType.methodType(void.class, MemoryAddress.class, int.class, long.class, byte.class, byte.class),
                FunctionDescriptor.ofVoid(C_POINTER, C_INT, C_LONG_LONG, C_CHAR, C_CHAR));
        deletePS2 = cLinker.downcallHandle(
                functionLookup("pcsx2_delete"),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER));
        deletePS3 = cLinker.downcallHandle(
                functionLookup("rpcs3_delete"),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(C_POINTER));
        freeBatchCommand = cLinker.downcallHandle(
                functionLookup("free_batch_command"),
                MethodType.methodType(void.class, int.class),
                FunctionDescriptor.ofVoid(C_INT));
        getError = cLinker.downcallHandle(
                functionLookup("get_error"),
                MethodType.methodType(int.class, MemoryAddress.class),
                FunctionDescriptor.of(C_INT, C_POINTER));
    }

    @Override
    public MemoryAddress allocatePS2() {
        return (MemoryAddress) call(allocatePS2::invoke);
    }

    @Override
    public MemoryAddress allocatePS3() {
        return (MemoryAddress) call(allocatePS3::invoke);
    }

    @Override
    public void initializeBatch(MemoryAddress ipc) {
        call(() -> initializeBatch.invoke(ipc));
    }

    @Override
    public void freeDataStream(MemoryAddress ipc) {
        call(() -> freeDataStream.invoke(ipc));
    }

    @Override
    public int finalizeBatch(MemoryAddress ipc) {
        return (int) call(() -> finalizeBatch.invoke(ipc));
    }

    @Override
    public long getReply(MemoryAddress ipc, int cmd, int place, byte command) {
        return (long) call(() -> getReply.invoke(ipc, cmd, place, command));
    }

    @Override
    public void sendCommand(MemoryAddress ipc, int command) {
        call (() -> sendCommand.invoke(ipc, command));
    }

    @Override
    public long read(MemoryAddress ipc, int address, byte command, byte batch) {
        return (long) call(() -> read.invoke(ipc, address, command, batch));
    }

    @Override
    public MemoryAddress version(MemoryAddress ipc, byte batch) {
        return (MemoryAddress) call(() -> version.invoke(ipc, batch));
    }

    @Override
    public int emuStatus(MemoryAddress ipc, byte batch) {
        return (int) call(() -> emuStatus.invoke(ipc, batch));
    }

    @Override
    public MemoryAddress getGameTitle(MemoryAddress ipc, byte batch) {
        return (MemoryAddress) call(() -> getGameTitle.invoke(ipc, batch));
    }

    @Override
    public MemoryAddress getGameId(MemoryAddress ipc, byte batch) {
        return (MemoryAddress) call(() -> getGameId.invoke(ipc, batch));
    }

    @Override
    public MemoryAddress getGameUuid(MemoryAddress ipc, byte batch) {
        return (MemoryAddress) call(() -> getGameUuid.invoke(ipc, batch));
    }

    @Override
    public MemoryAddress getGameVersion(MemoryAddress ipc, byte batch) {
        return (MemoryAddress) call(() -> getGameVersion.invoke(ipc, batch));
    }

    @Override
    public void saveState(MemoryAddress ipc, byte slot, byte batch) {
        call(() -> saveState.invoke(ipc, slot, batch));
    }

    @Override
    public void loadState(MemoryAddress ipc, byte slot, byte batch) {
        call(() -> loadState.invoke(ipc, slot, batch));
    }

    @Override
    public void write(MemoryAddress ipc, int address, long value, byte command, byte batch) {
        call(() -> write.invoke(ipc, address, value, command, batch));
    }

    @Override
    public void deletePS2(MemoryAddress ipc) {
        call(() -> deletePS2.invoke(ipc));
    }

    @Override
    public void deletePS3(MemoryAddress ipc) {
        call(() -> deletePS3.invoke(ipc));
    }

    @Override
    public void freeBatchCommand(int cmd) {
        call(() -> freeBatchCommand.invoke(cmd));
    }

    @Override
    public int getError(MemoryAddress ipc) {
        return (int) call(() -> getError.invoke(ipc));
    }

    private Addressable functionLookup(String name) {
        return call(() -> ipcLibrary.lookup("pine_" + name).orElseThrow(() -> new IllegalStateException("Function " + name + " lookup failed")));
    }
}
