package pinej;

public interface PineEnums {
    enum TargetPlatform {
        PS2, PS3
    }

    enum Error {
        SUCCESS, FAIL, OUT_OF_MEMORY, NO_CONNECTION, UNIMPLEMENTED, UNKNOWN;

        public static Error from(int i) {
            return switch (i) {
                case 0 -> SUCCESS;
                case 1 -> FAIL;
                case 2 -> OUT_OF_MEMORY;
                case 3 -> NO_CONNECTION;
                case 4 -> UNIMPLEMENTED;
                case 5 -> UNKNOWN;
                default -> throw new IllegalArgumentException("No value for " + i);
            };
        }
    }

    enum Command {
        READ_8, READ_16, READ_32, READ_64, WRITE_8, WRITE_16, WRITE_32, WRITE_64, VERSION, SAVE_STATE, LOAD_STATE,
        GAME_TITLE, GAME_ID, GAME_UUID, GAME_VERSION, STATUS, UNIMPLEMENTED;

        public byte toByte() {
            return (byte) switch (this) {
                case READ_8 -> 0;
                case READ_16 -> 1;
                case READ_32 -> 2;
                case READ_64 -> 3;
                case WRITE_8 -> 4;
                case WRITE_16 -> 5;
                case WRITE_32 -> 6;
                case WRITE_64 -> 7;
                case VERSION -> 8;
                case SAVE_STATE -> 9;
                case LOAD_STATE -> 0xA;
                case GAME_TITLE -> 0xB;
                case GAME_ID -> 0xC;
                case GAME_UUID -> 0xD;
                case GAME_VERSION -> 0xE;
                case STATUS -> 0xF;
                case UNIMPLEMENTED -> 0xFF;
            };
        }
    }

    enum Status {
        ERROR, RUNNING, PAUSED, SHUTDOWN;

        public static Status from(int i) {
            return switch (i) {
                default -> ERROR;
                case 0 -> RUNNING;
                case 1 -> PAUSED;
                case 2 -> SHUTDOWN;
            };
        }
    }
}
