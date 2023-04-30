package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CloudinaryUtils {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryUtils.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String signatureGenerator(String publicId, String apiSecret) {
        String toSign = "public_id=" + publicId + "&timestamp=" + System.currentTimeMillis() / 1000L + apiSecret;
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-1").digest(toSign.getBytes());
            String signature = bytesToHex(bytes);
            logger.info("Signature -> " + signature);
            return signature;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
