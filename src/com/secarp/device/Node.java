package com.secarp.device;

import java.util.*;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.network.Network;
import com.secarp.protocol.arp.Arp;
import com.secarp.protocol.arp.ArpCache;
import com.secarp.protocol.Packet;
import com.secarp.protocol.Receivable;

/**
 * An abstraction of a particular node
 */
public class Node {
    // The id of the node
    private int id;

    // Ipv4 address
    private Ipv4Address ipv4Address;

    // Mac address
    private MacAddress macAddress;

    // The underlying network
    private Network network;

    // The registered receivable instances that handle incoming packets
    private ArrayList<Receivable> receivables;

    // The arp protocol stack
    private Arp arp;

    // The arp cache
    private ArpCache arpCache;

    /**
     * Constructor function
     */
    public Node(Ipv4Address ipv4Address, MacAddress macAddress) {
        this.ipv4Address = ipv4Address;
        this.macAddress = macAddress;
        this.receivables = new ArrayList<Receivable>();
    }

    /**
     * Getter and setter functions
     */
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ipv4Address getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(Ipv4Address ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public MacAddress getMacAddress() {
        return this.macAddress;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return this.network;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    public ArrayList<Receivable> getReceivables() {
        return this.receivables;
    }

    public void setReceivables(ArrayList<Receivable> receivables) {
        this.receivables = receivables;
    }

    public ArpCache getArpCache() {
        return this.arpCache;
    }

    public void setArpCache(ArpCache arpCache) {
        this.arpCache = arpCache;
    }

    public Arp getArp() {
        return arp;
    }

    public void setArp(Arp arp) {
        this.arp = arp;
    }

    /**
     * Registers a new receivable
     *
     * @param Receivable The new receivable to be registered
     */
    public void registerReceivable(Receivable receivable) {
        this.receivables.add(receivable);
    }

    /**
     * Handles a packet receive operation
     * Iteratively calls all the receivables in a separate thread
     *
     * @param Packet The received packet
     */
    public void handlePacket(Packet packet) {
        for (Receivable receivable : this.receivables) {
            // Spawning a new thread
            // The good old Java way :)
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receivable.handlePacket(packet);
                    }
                }).start();
        }
    }

    /**
     * A wrapper over network's send packet
     * Spawns a new thread
     *
     * @param packet The packet to send
     * @param address The MAC address of the target node/nodes
     */
    private void sendPacket(Packet packet,
                            MacAddress address) {
        new Thread(new Runnable() {
                @Override
                public void run() {
                    network.sendPacket(id,
                                       packet,
                                       address);
                }
            }).start();
    }

    /**
     * Sends a packet to the underlying network by IP address
     *
     * @param packet The packet to be sent
     * @param targetIpv4Address The target Ipv4Address
     */
    public void sendPacket(Packet packet, Ipv4Address targetIpv4Address) {
        MacAddress targetAddress;

        while((targetAddress = this.arpCache.lookup(targetIpv4Address))
              != null) {
            Packet arpRequestPacket = this.arp.
                createRequestPacket(this.macAddress, // Source MAC
                                    this.ipv4Address, // Source IP
                                    targetIpv4Address
                                    );

            this.sendPacket(arpRequestPacket,
                            MacAddress.getBroadcast()
                            );
            // Waiting before looking up in cache
            Timer.sleep(1000);
        }
        this.sendPacket(packet,
                        targetAddress
                        );
    }
}
