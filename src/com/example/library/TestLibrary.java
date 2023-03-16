package com.example.library;

import com.example.library.model.*;
import com.example.library.model.doc.DocData;
import com.example.library.model.doc.DocUpdate;
import com.example.library.model.key.ExKeyModel;
import com.example.library.model.user.UserData;
import com.example.library.model.user.UserUpdate;
import com.example.library.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestLibrary {

    private final String filePath;
    public static String baseUrl;
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
    private String sessionId = "";
    private DocData docData;

    private String filePathTest = "keystore.jks";
    private int httpCode = 0;

    private boolean isServerSuccess = false;

    private Thread thread;
    private UserData userDataReg;

    public static void main(String[] args) {

        String data = "{\n" +
                "\"action\": \"DOC\",\n" +
                "\"sessionId\": \"9FC4BA12C1248600C1ABE0E8753DF035\",\n" +
                "\"anyData\": {\n" +
                "\"docId\": 1,\n" +
                "\"version\": 0,\n" +
                "\"createdUserId\": 1\n" +
                "}\n" +
                "}";

        String dataLog = "{\"sessionId\":\"697E924D229CC1B61C2970A28D5A7A9D\",\"action\":\"LOG\",\"anyData\":{\"id\":1,\"version\":9}}";

//        String bUrl = "http://st.24mycrm.com/sanarip-tamga/";
        TestLibrary.baseUrl = "http://st.24mycrm.com/sanarip-tamga/";
        String pName = "com.PIB.uved";

        TestLibrary testLibrary = new TestLibrary(baseUrl, pName);
        String response = testLibrary.signQrData(data);
        System.out.println(response);


//        TimeZone tz = TimeZone.getTimeZone("GMT+6");
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"); // Quoted "Z" to indicate UTC, no timezone offset
//        df.setTimeZone(tz);
//        String nowAsISO = df.format(new Date());
//
//        System.out.println(nowAsISO);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date()); // устанавливаем текущую дату
//        Date startDate = calendar.getTime();
//        calendar.add(Calendar.MONTH, 1); // добавляем 1 месяц
//        Date endDate = calendar.getTime(); // получаем дату через 1 месяц
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedEndDate = sdf.format(endDate);
//        String formattedStartDate = sdf.format(startDate);
//        System.out.println(formattedStartDate + " + " + "Дата через 1 месяц: " + formattedEndDate);
    }

    public TestLibrary(String packageName) {
        filePath = "/data/data/" + packageName + "/keystore.jks";
    }

    public TestLibrary(String baseUrl, String packageName) {
        TestLibrary.baseUrl = baseUrl;
        filePath = "/data/data/" + packageName + "/keystore.jks";
        initProvider();
        initRetrofit();
    }

    public String signQrData(String qrResult) {
        this.qrResult = qrResult;
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                libraryResponse = new LibraryResponse();
                try {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(qrResult, JsonObject.class);
                    action = jsonObject.get("action").getAsString();
                    sessionId = jsonObject.get("sessionId").getAsString();

                    switch (action) {
                        case "REG":
                            JsonObject anyDataReg = jsonObject.get("anyData").getAsJsonObject();
                            userDataReg = JsonUtils.fromJson(anyDataReg.toString(), UserData.class);
                            register(userDataReg, action);
                            break;
                        case "LOG":
                            JsonObject anyDataLog = jsonObject.get("anyData").getAsJsonObject();
                            userDataReg = JsonUtils.fromJson(anyDataLog.toString(), UserData.class);
                            login(userDataReg, action);
                            break;
                        case "DOC":
                            JsonObject anyDataDoc = jsonObject.get("anyData").getAsJsonObject();
                            docData = JsonUtils.fromJson(anyDataDoc.toString(), DocData.class);
                            document(docData, action);
                            break;
                    }

                } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
                    System.err.println(e.getMessage());
                } catch (GeneralSecurityException | IOException e) {
                }
                catch (NullPointerException e) {
                    System.out.println("Данный QR не распознан!");
                    showResponse("Данный QR не распознан!", false);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JsonUtils.toJson(libraryResponse);
    }

    private void register(UserData userData, String action) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateException, KeyStoreException, IOException {
        SecureRandom random = new SecureRandom();
        String userId = String.valueOf(userData.getUserId());
        userHash = userIdToHash(userId);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // устанавливаем текущую дату
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1); // добавляем 1 месяц
        Date endDate = calendar.getTime(); // получаем дату через 1 месяц

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedEndDate = sdf.format(endDate);
        String formattedStartDate = sdf.format(startDate);
        UserUpdate userUpdate = new UserUpdate();
        UserUpdate.Data user = new UserUpdate.Data();
        String publicKeyString = encodePublicKey(keyPair.getPublic());
        user.setPublicKey(publicKeyString);
        user.setPublicKeyName(userHash);
        user.setPeriod(formattedStartDate + " - " + formattedEndDate);
        user.setVersion(userData.getVersion());
        userUpdate.setData(user);
        String session = "JSESSIONID=" + sessionId;
//        userUpdate(userUpdate, userData.getUserId(), session);
        try{
            userUpdate(userUpdate, userData.getUserId(), session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String userIdToHash(String createdUserId) throws NoSuchAlgorithmException {
        byte[] hash = getMD5Hash(createdUserId);
        return bytesToHex(hash);
    }

    private void showResponse(String message, boolean result) {
        libraryResponse.setAction(action);
        switch (action) {
            case "LOG":
                if (userDataReg != null) {
                    if (isServerSuccess) {
                        int versionCounter = userDataReg.getVersion() + 1;
                        userDataReg.setVersion(versionCounter);
                    }
                    libraryResponse.setAnyData(userDataReg);
                }
                break;
            case "REG":
                if (userDataReg != null) {
                    if (isServerSuccess) {
                        int versionCounter = userDataReg.getVersion() + 1;
                        userDataReg.setVersion(versionCounter);
                    }
                    libraryResponse.setAnyData(userDataReg);
                }
                break;
            case "DOC":
                if (docData != null) {
                    if (isServerSuccess) {
                        int versionCounter = docData.getVersion() + 1;
                        docData.setVersion(versionCounter);
                    }
                    libraryResponse.setAnyData(docData);
                }
                break;
        }
        libraryResponse.setHttpCode(httpCode);
        libraryResponse.setMessage(message);
        libraryResponse.setResult(result);
    }

    private void login(UserData userData, String action) throws InvalidAlgorithmParameterException, CertificateException, NoSuchAlgorithmException, SignatureException, KeyStoreException, IOException, InvalidKeyException {
        try {
            String userId = String.valueOf(userData.getUserId());
            userHash = userIdToHash(userId);
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                isServerSuccess = true;
                showResponse("Авторизация прошла успешно!", true);
//                SecureRandom random = new SecureRandom();
//                String userDataJson = JsonUtils.toJson(userData);
//                byte[] digitalSignature = signingData(privateKey, random, userDataJson);
//                boolean verified = verifiedSignedData(null, userDataJson, digitalSignature);
//                if (verified) {
//                    showResponse("success", true);
//                } else {
//                    showResponse("failure", false);
//                }
//                request(digitalSignature, action, null);
            } else {
                System.out.println("Invalid User Id");
                register(userData, action);

            }

        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
        } catch (IOException e) {
//            showResponse("PrivateKey does not exist!", false);
            register(userData, action);
        } catch (SignatureException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    private void document(DocData docData, String action) {
        try {
            userHash = userIdToHash(String.valueOf(docData.getCreatedUserId()));
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                SecureRandom random = new SecureRandom();
                String docDataJson = JsonUtils.toJson(docData);
                byte[] digitalSignature = signingData(privateKey, random, docDataJson);
                TimeZone tz = TimeZone.getTimeZone("GMT+6");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                df.setTimeZone(tz);
                String nowAsISO = df.format(new Date());
                DocUpdate docUpdate = new DocUpdate();
                DocUpdate.Data doc = new DocUpdate.Data();
                doc.setSubscription(true);
                doc.setSubscriptionDate(nowAsISO);
                doc.setSubscriptionData(byteToString(digitalSignature));
                doc.setVersion(docData.getVersion());
                doc.setStatusSelect(2);
                docUpdate.setData(doc);
                String session = "JSESSIONID=" + sessionId;
//                docUpdate(docUpdate, docData.getDocId(), session);
                try {
                    docUpdate(docUpdate, docData.getDocId(), session);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                boolean verified = verifiedSignedData(null, docDataJson, digitalSignature);
//                if (verified) {
//                    showResponse("success", true, "");
//                } else {
//                    showResponse("failure", false, "");
//                }
//                request(digitalSignature, action, null);
            } else {
                showResponse("Для подписания этого документа у вас нет прав!", false);
            }
        } catch (KeyStoreException | IOException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
            showResponse("Отсутствует приватный ключ для подписания документа", false);
        } catch (SignatureException | InvalidKeyException e) {
            e.printStackTrace();
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
                .baseUrl(TestLibrary.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.apiInterface = retrofit.create(APIInterface.class);
    }

    private void userUpdate(UserUpdate userUpdate, int userId, String session) throws Exception {
        Call<ResponseBody> call = apiInterface.updateUserData(userId, session, userUpdate);
        Response<ResponseBody> response = call.execute();
        if (response.code() != 200) {
            httpCode = response.code();
            System.out.println(response.code());
            System.out.println(response.errorBody());
            isServerSuccess = true;
            switch (action) {
                case "REG": {
                    showResponse("Регистрация прошла успешно!", true);
                    break;
                }
                case "LOG": {
                    showResponse("Авторизация прошла успешно!", true);
                    break;
                }
            }
        } else {
            isServerSuccess = false;
            httpCode = response.code();
            switch (action) {
                case "REG": {
                    showResponse("Регистрация прошла успешно!", true);
                    break;
                }
                case "LOG": {
                    showResponse("Авторизация прошла успешно!", true);
                    break;
                }
            }
        }
    }
//    private void userUpdate(UserUpdate userUpdate, int userId, String session) {
//
//        Call<ResponseBody> call = apiInterface.updateUserData(userId, session, userUpdate);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.code() != 200) {
//                    System.out.println(response.code());
//                    System.out.println(response.errorBody());
//                    switch (action) {
//                        case "REG": {
//                            showResponse("Регистрация прошла успешно!", true);
//                            break;
//                        }
//                        case "LOG": {
//                            showResponse("Авторизация прошла успешно!", true);
//                            break;
//                        }
//                    }
//
//                }
//                switch (action) {
//                    case "REG": {
//                        showResponse("Регистрация прошла успешно!", true);
//                        break;
//                    }
//                    case "LOG": {
//                        showResponse("Авторизация прошла успешно!", true);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                switch (action) {
//                    case "REG": {
//                        showResponse("Регистрация прошла успешно!", true);
//                        break;
//                    }
//                    case "LOG": {
//                        showResponse("Авторизация прошла успешно!", true);
//                        break;
//                    }
//                }
//                t.printStackTrace();
//            }
//        });
//    }
    private  void docUpdate(DocUpdate docUpdate, int docId, String session) throws Exception {
        Call<ResponseBody> call = apiInterface.updateDocData(docId, session, docUpdate);
        Response<ResponseBody> response = call.execute();
        if (response.code() != 200) {
            System.out.println(response.code());
            System.out.println(response.errorBody());
            isServerSuccess = false;
            httpCode = response.code();
            showResponse("Подписание документов прошло успешно!", true);
        } else {
            isServerSuccess = true;
            httpCode = response.code();
            showResponse("Подписание документов прошло успешно!", true);
        }
    }
//    private void docUpdate(DocUpdate docUpdate, int docId, String session) {
//
//        Call<ResponseBody> call = apiInterface.updateDocData(docId, session, docUpdate);
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.code() != 200) {
//                    System.out.println(response.code());
//                    System.out.println(response.errorBody());
//                    showResponse("Подписание документов прошло успешно!", true);
//                }
//                showResponse("Подписание документов прошло успешно!", true);
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                showResponse("Подписание документов прошло успешно!", true);
//                t.printStackTrace();
//            }
//        });
//    }

    private static String encodePublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    private static String byteToString(byte[] digitalSignature) {
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    private void initProvider() {
        bouncyCastleProvider = BOUNCY_CASTLE_PROVIDER;
        Security.removeProvider("BC");
        Security.addProvider(bouncyCastleProvider);
    }

    private static byte[] getMD5Hash(String userId) throws NoSuchAlgorithmException {
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
            e.printStackTrace();
        }
        return keys;
    }

    public String deletePrivateKey(String sessionId, int id) {
        String userId = String.valueOf(id);
        LibraryResponse libraryResponse = new LibraryResponse();
        try {
            userHash = userIdToHash(userId);
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.delete()) {
                        libraryResponse.setResult(true);
                        libraryResponse.setMessage("Приватный ключ успешно удален!");
                        System.out.println("Приватный ключ успешно удален!");
//                        ExKeyModel exKeyModel = new ExKeyModel();
//                        TimeZone tz = TimeZone.getTimeZone("GMT+6");
//                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
//                        df.setTimeZone(tz);
//                        String nowAsISO = df.format(new Date());
//                        exKeyModel.setDate(nowAsISO);
//                        exKeyModel.setKeyName(userHash);
//                        thread = new Thread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Call<ResponseBody> call = apiInterface.notifyToServer(id, sessionId, exKeyModel);
//                                try {
//                                    Response<ResponseBody> response = call.execute();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });

                    } else {
                        System.out.println("Произошла ошибка при удалении приватного ключа");
                    }
                } else {
                    System.out.println("Приватный ключ отсутствует");
                }
            } else {
                System.out.println("Invalid User Id");
            }

        } catch (NoSuchAlgorithmException | IOException | KeyStoreException | UnrecoverableKeyException |
                 CertificateException e) {
            e.printStackTrace();
        }
        return JsonUtils.toJson(libraryResponse);
    }


    private interface APIInterface {
        @Headers({
                "Content-Type: application/json",
                "Accept: application/json",
                "X-Requested-With': 'XMLHttpRequest",
        })
        @POST("ws/rest/com.axelor.auth.db.User/{id}")
        Call<ResponseBody> updateUserData(
                @Path("id") int id,
                @Header("Cookie") String headers,
                @Body UserUpdate userUpdate
        );

        @Headers({
                "Content-Type: application/json",
                "Accept: application/json",
                "X-Requested-With': 'XMLHttpRequest",
        })
        @POST("ws/rest/com.axelor.apps.sale.db.Declaration/{id}")
        Call<ResponseBody> updateDocData(
                @Path("id") int id,
                @Header("Cookie") String headers,
                @Body DocUpdate docUpdate
        );

        @Headers({
                "Content-Type: application/json",
                "Accept: application/json",
                "X-Requested-With': 'XMLHttpRequest",
        })
        @POST("ws/rest/com.axelor.apps.sale.db.Declaration/{id}")
        Call<ResponseBody> notifyToServer(
                @Path("id") int id,
                @Header("Cookie") String headers,
                @Body ExKeyModel exKeyModel
        );
    }

    /*
    headers: {
        "Cookie": "JSESSIONID=
    }
     */

}
