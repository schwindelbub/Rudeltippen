package services;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import models.Confirmation;
import models.ConfirmationType;
import models.User;
import ninja.utils.NinjaProperties;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mchange.v1.util.UnexpectedException;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private DataService dataService;

    /**
     * Encrypt a String with the AES encryption standard using the application secret
     * @param value The String to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value) {
        return encryptAES(value, ninjaProperties.get("application.secret").substring(0, 16));
    }

    /**
     * Encrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value The String to encrypt
     * @param privateKey The key used to encrypt
     * @return An hexadecimal encrypted string
     */
    public String encryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            return byteToHexString(cipher.doFinal(value.getBytes()));
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    /**
     * Decrypt a String with the AES encryption standard using the application secret
     * @param value An hexadecimal encrypted string
     * @return The decrypted String
     */
    public String decryptAES(String value) {
        return decryptAES(value, ninjaProperties.get("application.secret").substring(0, 16));
    }

    /**
     * Decrypt a String with the AES encryption standard. Private key must have a length of 16 bytes
     * @param value An hexadecimal encrypted string
     * @param privateKey The key used to encrypt
     * @return The decrypted String
     */
    public String decryptAES(String value, String privateKey) {
        try {
            byte[] raw = privateKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            return new String(cipher.doFinal(hexStringToByte(value)));
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }

    public byte[] hexStringToByte(String hexString) {
        try {
            return Hex.decodeHex(hexString.toCharArray());
        } catch (DecoderException e) {
            throw new UnexpectedException(e);
        }
    }

    public String byteToHexString(byte[] bytes) {
        return String.valueOf(Hex.encodeHex(bytes));
    }

    public void activateAndSetAvatar(final User user) {
        final String avatar = AppUtils.getGravatarImage(user.getEmail(), "retro", 128);
        final String avatarSmall = AppUtils.getGravatarImage(user.getEmail(), "retro", 64);
        if (StringUtils.isNotBlank(avatar)) {
            user.setPictureLarge(avatar);
        }
        if (StringUtils.isNotBlank(avatarSmall)) {
            user.setPicture(avatarSmall);
        }

        user.setActive(true);
        dataService.save(user);
    }


    public void confirmationUser(final Confirmation confirmation, final User user, final ConfirmationType confirmationType) {
        if ((ConfirmationType.ACTIVATION).equals(confirmationType)) {
            activateAndSetAvatar(user);
            //flash.put("infomessage", Messages.get("controller.users.accountactivated"));
            dataService.delete(confirmation);

            LOG.info("User activated: " + user.getEmail());
        } else if ((ConfirmationType.CHANGEUSERNAME).equals(confirmationType)) {
            final String oldusername = user.getEmail();
            final String newusername = decryptAES(confirmation.getConfirmValue());
            user.setEmail(newusername);
            dataService.save(user);
            //session.remove("username");
            //flash.put("infomessage", Messages.get("controller.users.changedusername"));
            dataService.delete(confirmation);

            LOG.info("User changed username... old username: " + oldusername + " - " + "new username: " + newusername);
        } else if ((ConfirmationType.CHANGEUSERPASS).equals(confirmationType)) {
            user.setUserpass(decryptAES(confirmation.getConfirmValue()));
            dataService.save(user);
            //session.remove("username");
            //flash.put("infomessage", Messages.get("controller.users.changeduserpass"));
            dataService.delete(confirmation);

            LOG.info(user.getEmail() + " changed his password");
        }
    }

    public boolean authenticate(String username, String userpass) {
        boolean authenticated = false;
        User user = dataService.findUserByUsernameOrEmail(username);
        if (user != null) {
            if (user.getUserpass().equals(AppUtils.hashPassword(username, user.getSalt()))) {
                authenticated = true;
            }
        }

        return authenticated;
    }

    /**
     * Sign a message using the application secret key (HMAC-SHA1)
     */
    public String sign(String message) {
        return sign(message, ninjaProperties.get("application.secret").getBytes());
    }

    /**
     * Sign a message with a key
     * @param message The message to sign
     * @param key The key to use
     * @return The signed message (in hexadecimal)
     * @throws java.lang.Exception
     */
    public String sign(String message, byte[] key) {
        if (key.length == 0) {
            return message;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            mac.init(signingKey);
            byte[] messageBytes = message.getBytes("utf-8");
            byte[] result = mac.doFinal(messageBytes);
            int len = result.length;
            char[] hexChars = new char[len * 2];


            for (int charIndex = 0, startIndex = 0; charIndex < hexChars.length;) {
                int bite = result[startIndex++] & 0xff;
                hexChars[charIndex++] = HEX_CHARS[bite >> 4];
                hexChars[charIndex++] = HEX_CHARS[bite & 0xf];
            }
            return new String(hexChars);
        } catch (Exception ex) {
            throw new UnexpectedException(ex);
        }
    }
}
