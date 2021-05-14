package protocol;

import chord.ChordPeer;
import filehandler.FileHandler;

import java.io.File;

public abstract class Protocol {
    protected final File file;
    protected final ChordPeer peer;
    protected String path;

    public Protocol(File file, ChordPeer peer) {
        this.file = file;
        this.peer = peer;
    }

    public Protocol(String path, ChordPeer peer) {
        this.file = FileHandler.getFile(path);
        this.path = path;
        this.peer = peer;
    }

    public abstract void initialize();
}
