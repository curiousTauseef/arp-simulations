package com.secarp.protocol.arp;

import java.util.HashMap;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;

/**
 * Implements the standard ARP cache
 * It maps Ipv4Address to MacAddress
 * The timeout for any entry is 60 seconds(the default in Linux)
 */
public class ArpCache {
    // IP MAC hash map
    private HashMap<Ipv4Address, MacAddress> ipMacMap;

    // IP timeout hash map
    private HashMap<Ipv4Address, Integer> ipTimeoutMap;

    // The timeout of an entry in seconds
    private static final int TIMEOUT = 60;

    /**
     * Constructor function
     */
    public ArpCache() {
        this.ipMacMap = new HashMap<Ipv4Address, MacAddress>();
        this.ipTimeoutMap = new HashMap<Ipv4Address, Integer>();
    }

    /**
     * Add entry in cache
     *
     * @param ipv4Address The ipv4 address
     * @param macAddress The Mac address
     */
    public synchronized void put(Ipv4Address ipv4Address,
                                 MacAddress macAddress) {
        this.ipMacMap.put(ipv4Address,
                          macAddress);
        this.ipTimeoutMap.put(ipv4Address,
                              Timer.getCurrentTime());
    }

    /**
     * Lookup an entry in cache
     *
     * @param ipv4Address The ipv4 address
     *
     * @return The MAC address found in cache, null otherwise
     */
    public MacAddress lookup(Ipv4Address ipv4Address) {
        if (!this.ipTimeoutMap.containsKey(ipv4Address)) {
            return null;
        }
        if ((this.ipTimeoutMap.get(ipv4Address) + TIMEOUT) <
            Timer.getCurrentTime()
            ) {
            // Entry was present but has expired
            return null;
        }
        return this.ipMacMap.get(ipv4Address);
    }
}