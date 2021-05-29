package chord;

import utils.AddressPortList;

import java.io.Serializable;

public class ChordNodeData implements Serializable {
    private final int id;
    
    private final AddressPortList addressPortList;

    public ChordNodeData(int id, AddressPortList addressPortList) {
        this.id = id;
        this.addressPortList = addressPortList;
    }

    public int getId() {
        return id;
    }

    public AddressPortList getAddressPortList() {
        return addressPortList;
    }
}
