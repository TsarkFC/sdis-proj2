package utils;

import chord.ChordNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeChordNode {
    public byte[] serialize(ChordNode node) {
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

    public ChordNode deserialize(byte[] serializedObject) {
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(serializedObject);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (ChordNode) si.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
