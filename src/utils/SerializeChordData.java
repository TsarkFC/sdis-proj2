package utils;

import chord.ChordNodeData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeChordData {
    public byte[] serialize(ChordNodeData node) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(node);
            so.flush();
            return bo.toByteArray();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public ChordNodeData deserialize(byte[] serializedObject) {
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(serializedObject);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (ChordNodeData) si.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
