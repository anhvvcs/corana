/*
 * BinUtils - access various binary formats from Java
 *
 * (C) Copyright 2016 - JaWi - j.w.janssen@lxtreme.nl
 *
 * Licensed under Apache License v2.
 */
package elfutils;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

import static elfutils.Elf.*;


/**
 * Represents an ELF header.
 */
public class Header {
    private static final int EI_NIDENT = 16;

    public final ElfClass elfClass;
    public final ByteOrder elfByteOrder;
    public final AbiType abiType;
    public final int abiVersion;
    public final ObjectFileType elfType;
    public final MachineType machineType;
    public final int elfVersion;
    public final long entryPoint;
    public final int flags;
    public final long programHeaderOffset;
    public final long sectionHeaderOffset;

    public Header(ReadableByteChannel channel) throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(128);

        buf.clear();
        buf.limit(EI_NIDENT);
        readFully(channel, buf, "Excepted a valid ELF header!");

        byte[] eIdent = buf.array();
        // Verify whether it is has the correct file ID...
        if (eIdent[0] != 0x7f || eIdent[1] != 'E' || eIdent[2] != 'L' || eIdent[3] != 'F') {
            throw new IOException("Unknown file format! Expected valid ELF header (EI_MAG0..3)!");
        }

        int eClass = expectByteInRange(eIdent[4], 1, 2, "Invalid ELF file! Invalid ELF class (EI_CLASS)!");
        elfClass = ElfClass.values()[eClass - 1];

        int byteOrder = expectByteInRange(eIdent[5], 1, 2, "Invalid ELF file! Unknown byte order (EI_DATA)!");
        elfByteOrder = byteOrder == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        expectByteInRange(eIdent[6], 1, 1, "Invalid ELF file! Unknown file version (EI_VERSION)!");

        abiType = AbiType.valueOf(eIdent[7]);
        abiVersion = eIdent[8];

        buf.clear();
        buf.limit(8);
        buf.order(elfByteOrder);

        readFully(channel, buf, "Failed to read ELF type, machine and version!");

        elfType = ObjectFileType.valueOf(buf.getShort());
        machineType = MachineType.valueOf(buf.getShort());
        elfVersion = buf.getInt();

        buf.clear();
        switch (elfClass) {
            case CLASS_32:
                buf.limit(12);
                readFully(channel, buf, "Failed to read ELF entry point and offsets!");
                entryPoint = buf.getInt() & 0xFFFFFFFFL;
                programHeaderOffset = buf.getInt() & 0xFFFFFFFFL;
                sectionHeaderOffset = buf.getInt() & 0xFFFFFFFFL;
                break;
            case CLASS_64:
                buf.limit(24);
                readFully(channel, buf, "Failed to read ELF entry point and offsets!");
                entryPoint = buf.getLong();
                programHeaderOffset = buf.getLong();
                sectionHeaderOffset = buf.getLong();
                break;
            default:
                throw new IOException("Unhandled ELF-class!");
        }

        buf.clear();
        buf.limit(6);
        readFully(channel, buf, "Failed to read ELF flags and size information!");

        flags = buf.getInt();

        int headerSize = buf.getShort();
        // TODO this might not always be true? According to the GABI/ELF spec it
        // should...
        if (programHeaderOffset != 0 && headerSize != programHeaderOffset) {
            throw new IOException("Header size and program header do not match?!");
        }
    }

    public boolean is32bit() {
        return elfClass == ElfClass.CLASS_32;
    }

    public boolean is64bit() {
        return elfClass == ElfClass.CLASS_64;
    }

    public boolean isBigEndian() {
        return elfByteOrder == ByteOrder.BIG_ENDIAN;
    }

    public boolean isLittleEndian() {
        return elfByteOrder == ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ELF ");
        switch (elfClass) {
            case CLASS_32:
                sb.append("32");
                break;
            case CLASS_64:
                sb.append("64");
                break;
        }
        sb.append("-bit ");
        if (isLittleEndian()) {
            sb.append("LSB ");
        } else {
            sb.append("MSB ");
        }
        sb.append(elfType).append(", ").append(machineType);
        switch (machineType) {
            case ARM:
                if ((flags & Flags.EF_ARM_EABI_VER5) == Flags.EF_ARM_EABI_VER5) {
                    sb.append(" EABIv5");
                } else if ((flags & Flags.EF_ARM_EABI_VER4) == Flags.EF_ARM_EABI_VER4) {
                    sb.append(" EABIv4");
                } else if ((flags & Flags.EF_ARM_EABI_VER3) == Flags.EF_ARM_EABI_VER3) {
                    sb.append(" EABIv3");
                } else if ((flags & Flags.EF_ARM_EABI_VER2) == Flags.EF_ARM_EABI_VER2) {
                    sb.append(" EABIv2");
                } else if ((flags & Flags.EF_ARM_EABI_VER1) == Flags.EF_ARM_EABI_VER1) {
                    sb.append(" EABIv1");
                } else if ((flags & Flags.EF_ARM_EABI_UNKNOWN) == Flags.EF_ARM_EABI_UNKNOWN) {
                    sb.append(" unknown EABI");
                }
                break;
            default:
                break;
        }
        sb.append(" version ");
        sb.append(elfVersion).append(" (").append(abiType).append(")\n");
        sb.append("Using entry point = 0x").append(Long.toHexString(entryPoint));
        if (machineType == MachineType.ARM) {
            if (isBitSet(flags, Flags.EF_ARM_RELEXEC)) {
                sb.append(", relocatable executable");
            }
            if (isBitSet(flags, Flags.EF_ARM_HASENTRY)) {
                sb.append(", has entry point");
            }
            if (isBitSet(flags, Flags.EF_ARM_INTERWORK)) {
                sb.append(", interworking enabled");
            }
            if (isBitSet(flags, Flags.EF_ARM_APCS_26)) {
                sb.append(", APCS-26");
            } else {
                sb.append(", APCS-32");
            }
            if (isBitSet(flags, Flags.EF_ARM_APCS_FLOAT)) {
                sb.append(", using float registers");
            } else {
                sb.append(", using integer registers");
            }
            if (isBitSet(flags, Flags.EF_ARM_PIC)) {
                sb.append(", position independent");
            }
            if (isBitSet(flags, Flags.EF_ARM_ALIGN8)) {
                sb.append(", 8-bit structure alignment");
            }
            if (isBitSet(flags, Flags.EF_ARM_NEW_ABI)) {
                sb.append(", new ABI");
            }
            if (isBitSet(flags, Flags.EF_ARM_OLD_ABI)) {
                sb.append(", old ABI");
            }
            if (isBitSet(flags, Flags.EF_ARM_SOFT_FLOAT)) {
                sb.append(", using software FP");
            }
            if (isBitSet(flags, Flags.EF_ARM_VFP_FLOAT)) {
                sb.append(", using VFP FP");
            }
            if (isBitSet(flags, Flags.EF_ARM_MAVERICK_FLOAT)) {
                sb.append(", using maverick FP");
            }
        } else {
            if (flags != 0) {
                sb.append("0x").append(Integer.toHexString(flags));
            }
        }
        return sb.toString();
    }
}
