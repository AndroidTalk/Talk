package computer.schroeder.talk.util;

import android.content.Context;
import android.util.Base64;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import computer.schroeder.talk.storage.SimpleStorage;

public class EncryptionService
{
    private SimpleStorage simpleStorage;

    private PrivateKey privateKey;

    /**
     * Creates a new encryption service
     * @param context the context
     * @throws Exception thrown if the stored key pair is invalid
     */
    public EncryptionService(Context context) throws Exception
    {
        simpleStorage = new SimpleStorage(context);

        if(simpleStorage.getPrivateKey() == null || simpleStorage.getPublicKey() == null) createKey();

        privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(simpleStorage.getPrivateKey(), Base64.DEFAULT)));
        //publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(simpleStorage.getPublicKey(), Base64.DEFAULT)));
    }

    /**
     * Creates and stores a new key pair
     * @throws Exception thrown if the creation fails
     */
    private void createKey() throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = generator.generateKeyPair();

        simpleStorage.setPrivateKey(Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.DEFAULT));
        simpleStorage.setPublicKey(Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT));
    }

    /**
     * Encrypts a sendable json
     * To do so it requests the public key of the recipient from the backend
     * @param restService the rest service used to request the public key
     * @param json the actual unencrypted json sendable
     * @param user the recipient id
     * @return the encrypted sendable (a base64 string containing a json with the aes key and the actual encrypted message)
     * @throws Exception
     */
    public String encryptMessage(RestService restService, String json, String user) throws Exception
    {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey key = generator.generateKey();

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessage = aesCipher.doFinal(json.getBytes());

        String publicKey = restService.getPublicKey(user);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PUBLIC_KEY, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(publicKey.getBytes(), Base64.DEFAULT))));
        byte[] encryptedKey = cipher.doFinal(key.getEncoded());

        JSONObject sendable = new JSONObject();
        sendable.put("encryptedMessage", new String(Base64.encode(encryptedMessage, Base64.DEFAULT)));
        sendable.put("encryptedKey", new String(Base64.encode(encryptedKey, Base64.DEFAULT)));

        return Base64.encodeToString(sendable.toString().getBytes(), Base64.DEFAULT);
    }

    /**
     * Decrypts a messages using the stored private key
     * @param message the encrypted message
     * @return the decrypted json sendable
     * @throws Exception if the decryption fails
     */
    public String decryptMesage(String message) throws Exception
    {
        JSONObject object = new JSONObject(new String(Base64.decode(message, Base64.DEFAULT)));
        byte[] encryptedMessage = Base64.decode(object.getString("encryptedMessage"), Base64.DEFAULT);
        byte[] encryptedKey = Base64.decode(object.getString("encryptedKey"), Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PRIVATE_KEY, privateKey);
        byte[] decryptedKey = cipher.doFinal(encryptedKey);

        SecretKey originalKey = new SecretKeySpec(decryptedKey , 0, decryptedKey.length, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] bytejson = aesCipher.doFinal(encryptedMessage);
        String json = new String(bytejson);

        return json;
    }
}
