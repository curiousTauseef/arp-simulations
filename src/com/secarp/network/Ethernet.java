package com.secarp.network;

import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.device.Node;
import com.secarp.protocol.Packet;

/**
 * An abstraction over the ethernet network
 * This assumes that packets do not get sniffed by any other node
 */
public class Ethernet extends Network {
    /**
     * Constructor
     *
     * @param capacity The capacity of the receiving queue
     */
    public Ethernet(int capacity) {
        super(capacity);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void sendPacket(int id,
                           Packet packet,
                           MacAddress address) {
        if (address.isBroadcast()) {
            for (Node node : this.nodes) {
                if (node.getId() != id) {
                    node.handlePacket(packet);
                }
            }
        } else {
            for (Node node : this.nodes) {
                if (node.
                    getMacAddress().
                    toString().
                    equals(address.
                           toString()
                           )
                    ) {
                    node.handlePacket(packet);
                }
            }
        }
    }
}
