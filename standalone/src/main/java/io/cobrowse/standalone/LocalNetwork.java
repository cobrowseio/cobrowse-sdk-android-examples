package io.cobrowse.standalone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class LocalNetwork {

    private LocalNetwork() { }

    /**
     * Checks if the URL points to the local network, which requires
     * the {@link android.Manifest.permission#ACCESS_LOCAL_NETWORK} permission on API 37+.
     * Host names are not resolved, so only IP addresses and {@code .local} names are detected.
     *
     * @see <a href="https://developer.android.com/privacy-and-security/local-network-definition">Local network definition</a>
     */
    public static boolean isLocalUrl(@Nullable String url) {
        if (url == null) {
            return false;
        }
        String host;
        try {
            host = new URI(url).getHost();
        } catch (URISyntaxException e) {
            return false;
        }
        if (host == null) {
            return false;
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.endsWith(".local")) {
            return true;
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            return isLinkLocalIpv6(host.substring(1, host.length() - 1));
        }
        return isLocalIpv4(host);
    }

    private static boolean isLocalIpv4(@NonNull String host) {
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        int[] octets = new int[4];
        for (int i = 0; i < 4; i++) {
            if (!parts[i].matches("\\d{1,3}")) {
                return false;
            }
            octets[i] = Integer.parseInt(parts[i]);
            if (octets[i] > 255) {
                return false;
            }
        }
        int a = octets[0];
        int b = octets[1];
        return a == 10                                  // 10.0.0.0/8
                || (a == 172 && b >= 16 && b <= 31)     // 172.16.0.0/12
                || (a == 192 && b == 168)               // 192.168.0.0/16
                || (a == 169 && b == 254)               // 169.254.0.0/16, link-local
                || (a == 100 && b >= 64 && b <= 127);   // 100.64.0.0/10, CGNAT
    }

    private static boolean isLinkLocalIpv6(@NonNull String host) {
        // fe80::/10, the first group is never compressed in a link-local address
        int end = host.indexOf(':');
        if (end <= 0 || end > 4) {
            return false;
        }
        try {
            return (Integer.parseInt(host.substring(0, end), 16) & 0xffc0) == 0xfe80;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
