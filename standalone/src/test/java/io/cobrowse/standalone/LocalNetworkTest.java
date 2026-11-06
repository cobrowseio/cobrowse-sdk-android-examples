package io.cobrowse.standalone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalNetworkTest {

    @Test
    public void isLocalUrl_localAddresses() {
        assertTrue(LocalNetwork.isLocalUrl("http://10.0.2.2:3000"));
        assertTrue(LocalNetwork.isLocalUrl("https://172.16.0.1"));
        assertTrue(LocalNetwork.isLocalUrl("https://172.31.255.255"));
        assertTrue(LocalNetwork.isLocalUrl("https://192.168.1.10/"));
        assertTrue(LocalNetwork.isLocalUrl("http://169.254.10.20"));
        assertTrue(LocalNetwork.isLocalUrl("http://100.64.0.1"));
        assertTrue(LocalNetwork.isLocalUrl("http://100.127.255.255"));
        assertTrue(LocalNetwork.isLocalUrl("http://[fe80::1]:3000"));
        assertTrue(LocalNetwork.isLocalUrl("http://[FEBF::abcd]"));
        assertTrue(LocalNetwork.isLocalUrl("https://cobrowse.local"));
        assertTrue(LocalNetwork.isLocalUrl("https://Cobrowse.LOCAL:8443"));
    }

    @Test
    public void isLocalUrl_otherAddresses() {
        assertFalse(LocalNetwork.isLocalUrl(null));
        assertFalse(LocalNetwork.isLocalUrl(""));
        assertFalse(LocalNetwork.isLocalUrl("https://cobrowse.io"));
        assertFalse(LocalNetwork.isLocalUrl("https://cobrowse.corp.example"));
        assertFalse(LocalNetwork.isLocalUrl("http://localhost:3000"));
        assertFalse(LocalNetwork.isLocalUrl("http://127.0.0.1"));
        assertFalse(LocalNetwork.isLocalUrl("http://[::1]"));
        assertFalse(LocalNetwork.isLocalUrl("http://[fec0::1]"));
        assertFalse(LocalNetwork.isLocalUrl("https://8.8.8.8"));
        assertFalse(LocalNetwork.isLocalUrl("https://172.32.0.1"));
        assertFalse(LocalNetwork.isLocalUrl("https://100.128.0.1"));
        assertFalse(LocalNetwork.isLocalUrl("https://10.0.0.256"));
        assertFalse(LocalNetwork.isLocalUrl("https://10.example.com"));
        assertFalse(LocalNetwork.isLocalUrl("10.0.2.2:3000"));
        assertFalse(LocalNetwork.isLocalUrl("not a url"));
    }
}
