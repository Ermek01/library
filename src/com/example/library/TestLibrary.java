package com.example.library;

import com.example.library.model.*;
import com.example.library.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;

public class TestLibrary {

    private final String filePath;
    private String baseUrl;
    private Boolean isDublicate;
    private static String data = "{\n" +
            "  \"action\": \"REG\",\n" +
            "  \"anyData\": {\"createdUserId\":\"112\",\"username\":\"Эрмек\",\"email\":\"email.ru\"}\n" +
            "}";
    private BouncyCastleProvider bouncyCastleProvider;
    public final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private APIInterface apiInterface;

    private LibraryResponse libraryResponse;
    private String action = "";
    private String qrResult = "";
    private String userHash = "";

    public static void main(String[] args) {
    }

    public TestLibrary(String packageName) {
        filePath = "/data/data/" + packageName + "/keystore.jks";
    }

    public TestLibrary(String baseUrl, String packageName) {
        this.baseUrl = baseUrl;
        filePath = "/data/data/" + packageName + "/keystore.jks";
        initProvider();
        initRetrofit();
    }

    public String signQrData(String qrResult) {
        this.qrResult = qrResult;
        libraryResponse = new LibraryResponse();
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(qrResult, JsonObject.class);
            action = jsonObject.get("action").getAsString();
            switch (action) {
                case "REG":
                    JsonObject anyDataReg = jsonObject.get("anyData").getAsJsonObject();
                    UserData userDataReg = JsonUtils.fromJson(anyDataReg.toString(), UserData.class);
                    register(userDataReg, action);
                    break;
                case "LOG":
                    JsonObject anyDataLog = jsonObject.get("anyData").getAsJsonObject();
                    UserData userDataLog = JsonUtils.fromJson(anyDataLog.toString(), UserData.class);
                    login(userDataLog, action);
                    break;
                case "DOC":
                    JsonObject anyDataDoc = jsonObject.get("anyData").getAsJsonObject();
                    DocData docData = JsonUtils.fromJson(anyDataDoc.toString(), DocData.class);
                    document(docData, action);
                    break;
            }

        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        } catch (GeneralSecurityException | IOException e) {
        }
        return JsonUtils.toJson(libraryResponse);
    }

    private void register(UserData userData, String action) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateException, KeyStoreException, IOException {
        SecureRandom random = new SecureRandom();
        userHash = userIdToHash(userData.getCreatedUserId());
        try {
            getPrivateKey(userHash);
            isDublicate = true;
        } catch (UnrecoverableKeyException | IOException e) {
            isDublicate = false;
        }
        KeyPair keyPair = generateKeys(random);
        X509Certificate cert = generateX509Certificate(keyPair);
        storeToKeyStore(cert, keyPair.getPrivate(), userHash);
//        String userDataJson = JsonUtils.toJson(userData);
//        byte[] digitalSignature = signingData(keyPair.getPrivate(), random, userDataJson);
//        boolean verified = verifiedSignedData(keyPair.getPublic(), userDataJson, digitalSignature);
//        if (verified) {
//            showResponse("success", true, "");
//        } else {
//            showResponse("failure", false, "");
//        }
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(1);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sendStartDate = startDate.format(myFormatObj);
        String sendEndDate = endDate.format(myFormatObj);
        UserUpdate userUpdate = new UserUpdate();
        UserUpdate.Data user = new UserUpdate.Data();
        String publicKeyString = encodePublicKey(keyPair.getPublic());
        user.setPublicKey(publicKeyString);
        user.setPublicKeyName(userHash);
        user.setPeriod(sendStartDate + "-" + sendEndDate);
        userUpdate.setData(user);
        String json = JsonUtils.toJson(userUpdate);
        System.out.println(json);
        //userUpdate(digitalSignature, action, publicKey);
    }

    private String userIdToHash(String createdUserId) throws NoSuchAlgorithmException {
        byte[] hash = getMD5Hash(createdUserId);
        return bytesToHex(hash);
    }

    private void showResponse(String message, boolean result, String doc) {
        libraryResponse.setAction(action);
        libraryResponse.setAnyData(qrResult);
        libraryResponse.setUserHash(userHash);
        libraryResponse.setMessage(message);
        libraryResponse.setResult(result);
        libraryResponse.setDocument(doc);
    }

    private void login(UserData userData, String action) {
        try {
            userHash = userIdToHash(userData.getCreatedUserId());
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                SecureRandom random = new SecureRandom();
                String userDataJson = JsonUtils.toJson(userData);
                byte[] digitalSignature = signingData(privateKey, random, userDataJson);
                boolean verified = verifiedSignedData(null, userDataJson, digitalSignature);
                if (verified) {
                    showResponse("success", true, "");
                } else {
                    showResponse("failure", false, "");
                }
//                request(digitalSignature, action, null);
            } else {
                System.out.println("Invalid User Id");
                showResponse("Invalid User Id", false, "");
            }

        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
        } catch (IOException e) {
            showResponse("PrivateKey does not exist!", false, "");
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    private void document(DocData docData, String action) {
        try {
            userHash = userIdToHash(docData.getCreatedUserid());
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                SecureRandom random = new SecureRandom();
                String docDataJson = JsonUtils.toJson(docData);
                byte[] digitalSignature = signingData(privateKey, random, docDataJson);
                boolean verified = verifiedSignedData(null, docDataJson, digitalSignature);
                if (verified) {
                    showResponse("success", true, "");
                } else {
                    showResponse("failure", false, "");
                }
//                request(digitalSignature, action, null);
            } else {
                showResponse("Invalid User Id", false, "");
            }
        } catch (KeyStoreException | IOException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
            showResponse("PrivateKey does not exist!", false, "");
        } catch (SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }


    private PrivateKey getPrivateKey(String hexHash) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream inputStream = new FileInputStream(new File(filePath));
//        FileInputStream inputStream = new FileInputStream("keystore.jks");
        ks.load(inputStream, "passwd".toCharArray());
        return (PrivateKey) ks.getKey(hexHash, "passwd".toCharArray());
    }

    private void storeToKeyStore(X509Certificate cert, PrivateKey privateKey, String hexHash) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setKeyEntry(hexHash, privateKey, "passwd".toCharArray(), new java.security.cert.Certificate[]{cert});
        FileOutputStream fos = new FileOutputStream(new File(filePath));
//        FileOutputStream fos = new FileOutputStream("keystore.jks");
        ks.store(fos, "passwd".toCharArray());
        fos.close();
    }

    private static X509Certificate generateX509Certificate(KeyPair keyPair) throws SignatureException, InvalidKeyException {
        X500Principal dnName = new X500Principal("CN=Test");
        Date startDate = new Date();
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L);
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(dnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(endDate);
        certGen.setSubjectDN(dnName);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256withRSA");
        return certGen.generateX509Certificate(keyPair.getPrivate());
    }

    private byte[] signingData(PrivateKey privateKey, SecureRandom random, String anyData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA", bouncyCastleProvider);
        signature.initSign(privateKey, random);
        signature.update(anyData.getBytes());
        return signature.sign();
    }

    private boolean verifiedSignedData(PublicKey publicKey, String anyData, byte[] digitalSignature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA", bouncyCastleProvider);
        signature.initVerify(publicKey);
        signature.update(anyData.getBytes());
        return signature.verify(digitalSignature);
    }

    private KeyPair generateKeys(SecureRandom random) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider);
        keyPairGenerator.initialize(2048, random);
        return keyPairGenerator.generateKeyPair();
    }

    private void initRetrofit() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiInterface = retrofit.create(APIInterface.class);
    }

    private void userUpdate(UserUpdate userUpdate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    String encodedPublicKey = encodePublicKey(publicKey);
//                    ExQrResult exQrResult = new ExQrResult(action, digitalSignature, encodedPublicKey, isDublicate);
//                    Call<ResponseBody> call = apiInterface.postData(exQrResult);
//                    call.enqueue(new retrofit2.Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//                            if (response.isSuccessful()) {
//
//                            } else {
//
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            t.printStackTrace();
//                        }
//                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String encodePublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    private void initProvider() {
        bouncyCastleProvider = BOUNCY_CASTLE_PROVIDER;
        Security.removeProvider("BC");
        Security.addProvider(bouncyCastleProvider);
    }

    private byte[] getMD5Hash(String userId) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        md.update(userId.getBytes());
        return md.digest();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public ArrayList<String> getKeys() {
        ArrayList<String> keys = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fis, "passwd".toCharArray());
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                keys.add(alias);
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }


    private interface APIInterface {
        @FormUrlEncoded
        @POST("/ws/rest/com.axelor.auth.db.User/{id}")
        Call<ResponseBody> postData(
                @Path("id") int id,
                @Body UserUpdate userUpdate
        );
    }

}
