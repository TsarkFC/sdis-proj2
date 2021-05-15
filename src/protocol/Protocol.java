package protocol;

import chord.Chord;
import peer.Peer;
import filehandler.FileHandler;

import java.io.File;
import java.io.IOException;

public abstract class Protocol {
    protected final File file;
    protected final Peer peer;
    protected String path;

    public Protocol(File file, Peer peer) {
        this.file = file;
        this.peer = peer;
    }

    public Protocol(String path, Peer peer) {
        this.file = FileHandler.getFile(path);
        this.path = path;
        this.peer = peer;
    }



    public abstract void initialize();
}
