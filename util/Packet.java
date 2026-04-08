package util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import models.*;

import org.apache.commons.lang3.SerializationUtils;

// HEADER   DATA    HEADER   DATA
public class Packet implements Serializable {
    public byte[] serializedClientUUID;
    public byte packetsAmount;
    public byte currentPacket; // Пакеты нумеровать будем как: [0;4]
    public short readableDataLength;
    public byte[] data = new byte[782];

    public Packet(UUID clientUUID, byte packetsAmount, byte currentPacket, byte[] data) {
        this.serializedClientUUID = SerializationUtils.serialize(clientUUID);
        this.packetsAmount = packetsAmount;
        this.currentPacket = currentPacket;
        if (data != null) {
            this.readableDataLength = (short) data.length;
            if (data.length < 782) {
                this.data = Arrays.copyOf(data, 782);
            } else {
                this.data = data;
            }
        } else {
            this.readableDataLength = 0;
            this.data = new byte[782];
        }
    }


    public Packet(UUID clientUUID, int packetsAmount, int currentPacket, byte[] data) {
        this.serializedClientUUID = SerializationUtils.serialize(clientUUID);
        this.packetsAmount = (byte) packetsAmount;
        this.currentPacket = (byte) currentPacket;
        if (data != null) {
            this.readableDataLength = (short) data.length;
            if (data.length < 782) {
                this.data = Arrays.copyOf(data, 782);
            } else {
                this.data = data;
            }
        } else {
            this.readableDataLength = 0;
            this.data = new byte[782];
        }
    }

    public static void main(String[] args) {
        Color panda = Color.BLACK;
        Packet bear = new Packet(UUID.randomUUID(), (byte) 1, (byte) 0, SerializationUtils.serialize(panda));
        System.out.println(SerializationUtils.serialize(bear).length);
        Color panda2 = SerializationUtils.deserialize(bear.readActualData());
        System.out.println(panda2);
    }

    public byte[] getSerializedClientUUID() {
        return serializedClientUUID;
    }

    public UUID getClientUUID() {
        return SerializationUtils.deserialize(serializedClientUUID);
    }

    public byte getPacketsAmount() {
        return packetsAmount;
    }

    public byte getCurrentPacket() {
        return currentPacket;
    }

    public short getReadableDataLength() {
        return readableDataLength;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] readActualData() {
        return Arrays.copyOfRange(this.data, 0, this.readableDataLength);
    }

    public boolean isSingle() {
        return packetsAmount == 1;
    }

    public boolean isConnectionDefining() {
        return readableDataLength == 0;
    }
}
