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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TestLibrary {

    private final String filePath;
    public static String baseUrl;
    private Boolean isDublicate;
    private static String data = "{\n" +
            "  \"action\": \"REG\",\n" +
            "  \"anyData\": {\"createdUserId\":\"112\",\"username\":\"Эрмек\",\"email\":\"email.ru\"}\n" +
            "}";
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

    public TestLibrary(String packageName) {
        filePath = "/data/data/" + packageName + "/keystore.jks";
    }

    public TestLibrary(String baseUrl, String packageName) {
        TestLibrary.baseUrl = baseUrl;
        filePath = "/data/data/" + packageName + "/keystore.jks";
        initProvider();
        initRetrofit();
    }

    private void initProvider() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BOUNCY_CASTLE_PROVIDER);
        }
    }

    public static void main(String[] args) {

        Provider[] providers = Security.getProviders();
        for (int i = 0; i <providers.length; i++) {
            System.out.println(providers[i].getName());
        }

        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String iso8601String = now.format(formatter);
        System.out.println(iso8601String);

    }

    public String signQrData(String qrResult) {
        try {
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
                        showResponse(e.getMessage(), false);
                    } catch (GeneralSecurityException | IOException e) {
                        showResponse(e.getMessage(), false);
                    }
                    catch (NullPointerException e) {
                        System.out.println("Данный QR не распознан!");
                        showResponse("Данный QR не распознан!", false);
                    }
                    catch (Exception e) {
                        showResponse(e.getMessage(), false);
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            catch (Exception e) {
                showResponse(e.getMessage(), false);
            }
        }
        catch (RuntimeException e) {
            showResponse(e.getMessage(), false);
        }
        catch (Exception exception) {
            showResponse(exception.getMessage(), false);
        }

        return JsonUtils.toJson(libraryResponse);
    }

    private void register(UserData userData, String action) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateException, KeyStoreException, IOException, NoSuchProviderException, OperatorCreationException {
        SecureRandom random = new SecureRandom();
        String userId = String.valueOf(userData.getUserId());
        userHash = userIdToHash(userId);
        try {
            getPrivateKey(userHash);
            isDublicate = true;
        } catch (UnrecoverableKeyException | IOException e) {
            showResponse(e.getMessage(), false);
            isDublicate = false;
        }
        catch (Exception e) {
            showResponse(e.getMessage(), false);
        }
        KeyPair keyPair = generateKeys(random);
        X509Certificate cert = generateX509Certificate(keyPair);
        storeToKeyStore(cert, keyPair.getPrivate(), userHash);
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
            showResponse(e.getMessage(), false);
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

    private void login(UserData userData, String action) throws InvalidAlgorithmParameterException, CertificateException, NoSuchAlgorithmException, SignatureException, KeyStoreException, IOException, InvalidKeyException, NoSuchProviderException, OperatorCreationException {
        try {
            String userId = String.valueOf(userData.getUserId());
            userHash = userIdToHash(userId);
            PrivateKey privateKey = getPrivateKey(userHash);
            if (privateKey != null) {
                isServerSuccess = true;
                showResponse("Авторизация прошла успешно!", true);
            } else {
                System.out.println("Invalid User Id");
                register(userData, action);

            }

        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 CertificateException e) {
        } catch (IOException e) {
            register(userData, action);
        } catch (Exception e) {
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
                ZonedDateTime now = ZonedDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                String iso8601String = now.format(formatter);
                DocUpdate docUpdate = new DocUpdate();
                DocUpdate.Data doc = new DocUpdate.Data();
                doc.setSubscription(true);
                doc.setSubscriptionDate(iso8601String);
                doc.setSubscriptionData(byteToString(digitalSignature));
                doc.setVersion(docData.getVersion());
                doc.setStatusSelect(2);
                doc.setId(docData.getDocId());
                docUpdate.setData(doc);
                String session = "JSESSIONID=" + sessionId;
                try {
                    docUpdate(docUpdate, docData.getDocId(), session);
                } catch (Exception e) {
                    showResponse(e.getMessage(), false);

                }
            } else {
                showResponse("Для подписания этого документа у вас нет прав!", false);
            }
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException |
                 IOException e) {
            showResponse("Отсутствует приватный ключ для подписания документа", false);
        } catch (SignatureException | InvalidKeyException e) {
            showResponse(e.getMessage(), false);
        }
    }


    private PrivateKey getPrivateKey(String hexHash) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream inputStream = new FileInputStream(new File(filePath));
        ks.load(inputStream, "passwd".toCharArray());
        return (PrivateKey) ks.getKey(hexHash, "passwd".toCharArray());
    }

    private void storeToKeyStore(X509Certificate cert, PrivateKey privateKey, String hexHash) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setKeyEntry(hexHash, privateKey, "passwd".toCharArray(), new java.security.cert.Certificate[]{cert});
        FileOutputStream fos = new FileOutputStream(new File(filePath));
        ks.store(fos, "passwd".toCharArray());
        fos.close();
    }

    private static X509Certificate generateX509Certificate(KeyPair keyPair) throws SignatureException, InvalidKeyException, NoSuchProviderException, OperatorCreationException, CertificateException {
        X500Name issuer = new X500Name("CN=Self-Signed");
        X500Name subject = new X500Name("CN=Self-Signed");
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now().plus(365, ChronoUnit.DAYS));

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                startDate,
                endDate,
                subject,
                keyPair.getPublic()
        );

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(certHolder);

//        X500Principal dnName = new X500Principal("CN=Test");
//        Date startDate = new Date();
//        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L);
//        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
//        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
//        certGen.setSerialNumber(serialNumber);
//        certGen.setIssuerDN(dnName);
//        certGen.setNotBefore(startDate);
//        certGen.setNotAfter(endDate);
//        certGen.setSubjectDN(dnName);
//        certGen.setPublicKey(keyPair.getPublic());
//        certGen.setSignatureAlgorithm("SHA256withRSA");
//        return certGen.generateX509Certificate(keyPair.getPrivate(), "BC");
    }

    private byte[] signingData(PrivateKey privateKey, SecureRandom random, String anyData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey, random);
        signature.update(anyData.getBytes());
        return signature.sign();
    }

    private boolean verifiedSignedData(PublicKey publicKey, String anyData, byte[] digitalSignature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(anyData.getBytes());
        return signature.verify(digitalSignature);
    }

    private KeyPair generateKeys(SecureRandom random) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
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
        try {
            Call<ResponseBody> call = apiInterface.updateUserData(userId, session, userUpdate);
            Response<ResponseBody> response = call.execute();
            if (response.code() != 200) {
                httpCode = response.code();
                System.out.println(response.code());
                System.out.println(response.errorBody());
                isServerSuccess = false;
                switch (action) {
                    case "REG": {
                        showResponse("При регистрации произошла ошибка!", false);
                        break;
                    }
                    case "LOG": {
                        showResponse("При авторизации произошла ошибка!", false);
                        break;
                    }
                }
            } else {
                isServerSuccess = true;
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
        } catch (Exception e) {
            showResponse(e.getMessage(), false);
        }

    }

    private  void docUpdate(DocUpdate docUpdate, int docId, String session) throws Exception {
        System.out.println(JsonUtils.toJson(docUpdate));
        Call<ResponseBody> call = apiInterface.updateDocData(docId, session, docUpdate);
        Response<ResponseBody> response = call.execute();
        String responseBody = response.body().string();
        System.out.println(responseBody);
        if (response.code() != 200) {
            System.out.println(response.code());
            System.out.println(response.errorBody());
            isServerSuccess = false;
            httpCode = response.code();
            showResponse("При подписании документов произошла ошибка!", false);
        } else {
            isServerSuccess = true;
            httpCode = response.code();
            showResponse("Подписание документов прошло успешно!", true);
        }
    }

    private static String encodePublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }

    private static String byteToString(byte[] digitalSignature) {
        return Base64.getEncoder().encodeToString(digitalSignature);
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
            showResponse(e.getMessage(), false);
        }
        catch (Exception e) {
            showResponse(e.getMessage(), false);
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
            showResponse(e.getMessage(), false);
        }
        catch (Exception e) {
            showResponse(e.getMessage(), false);
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

}
