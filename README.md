# PINE-J

Java implementation of PINE API for PCSX2 and RPCS3. Implemented by accessing the C API through Foreign Linker API
introduced in Java 16.

Read more about the original reference API [here](https://github.com/GovanifY/pine/).

Inspired by [KAMI](https://github.com/isJuhn/KAMI).

## Requirements

1. Java 17 (JDK for compiling, JRE for running)
2. pine_c.dll file compiled from the original reference API.

## Building

1. Compile with Maven.
2. Add `--enable-native-access=pinej` to VM launch parameters.

## Notes

You'll crash the JRE if you are not careful with this interface! For example, check that you're connected and a game is
running before you try to query for game information. 