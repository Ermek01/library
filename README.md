## Библиотека ЭЦП

## **Введение**

Библиотека предоставляет функциональность для генерации ключей, подписания данных и проверки подписей. Она использует алгоритмы RSA и SHA-256 для обеспечения высокого уровня безопасности. В зависимости от типа данных (авторизация, регистрация или подписание документов), библиотека формирует и отправляет POST-запросы на сервер для проверки подписанных данных.

Эта документация предназначена для разработчиков и пользователей, которые хотят использовать нашу библиотеку ЭЦП в своих проектах. Здесь вы найдете информацию о компонентах, инструкции по использованию и интеграции, а также примеры кода.

Библиотека разработана для обеспечения безопасности и подписания данных в приложениях на платформах Android и iOS. Она успешно интегрирована в проекты Android, iOS и Flutter с использованием MethodChannel для вызова функций библиотеки.

## Интеграция с платформами

#### ***Интеграция с Android и Java-проектами:***

Для интеграции нашей библиотеки с Android-проектом выполните следующие шаги:

- Скопируйте .jar файл библиотеки в папку 'libs' вашего Android-проекта. Если такой папки нет, создайте ее в корне проекта.
- В файле 'build.gradle' (Module: app) вашего Android-проекта, добавьте зависимость для .jar файла в разделе dependencies:
```
implementation files('libs/library.jar')
```
Теперь вы можете импортировать классы из вашей .jar библиотеки в коде вашего Android-проекта и использовать их для вызова методов библиотеки.

#### ***Интеграция с Flutter-проектами:***

Для интеграции нашей библиотеки с Flutter-проектом выполните следующие шаги:

- Создайте MethodChannel в коде Flutter-проекта для взаимодействия с нативными платформами (Android и iOS).
- В файле MainActivity (для Android) или AppDelegate (для iOS) вашего Flutter-проекта, добавьте код для вызова методов вашей библиотеки.

## Генерация и хранение ключей

#### ***Генерация ключей:***
Библиотека использует алгоритм RSA для генерации пары ключей (открытый и закрытый). В процессе генерации ключей используется идентификатор пользователя, который конвертируется с помощью алгоритма MD5 для создания уникального имени ключа. Это имя ключа будет использоваться для хранения закрытого ключа в JKS (Java KeyStore) или Keychain.

```
private KeyPair generateKeys(SecureRandom random) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, random);
        return keyPairGenerator.generateKeyPair();
    }
```
#### ***Хранение ключей в JKS (Java KeyStore):***
На платформе Java закрытый ключ хранится в JKS. JKS предоставляет стандартный и безопасный способ хранения и управления ключами. Он защищает закрытые ключи паролем, обеспечивая их безопасность от несанкционированного доступа.

```
private void storeToKeyStore(X509Certificate cert, PrivateKey privateKey, String hexHash) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setKeyEntry(hexHash, privateKey, "passwd".toCharArray(), new java.security.cert.Certificate[]{cert});
        FileOutputStream fos = new FileOutputStream(new File(filePath));
        ks.store(fos, "passwd".toCharArray());
        fos.close();
    }
```
## Создание и проверка подписи

### ***Создание подписи:***
Подписание данных происходит с использованием сгенерированного закрытого ключа и JSON-поля "anyData", который приходит внутри параметра inQrModel. Библиотека использует алгоритмы RSA и SHA-256 для создания цифровой подписи, обеспечивая высокий уровень безопасности и надежности.

```
private byte[] signingData(PrivateKey privateKey, SecureRandom random, String anyData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey, random);
        signature.update(anyData.getBytes());
        return signature.sign();
    }
```
Подпись создается для модели обновления и уведомления, которая включает в себя дату и время подписи в формате "yyyy-MM-dd'T'HH:mm:ss.SSSZ", подписанные данные в формате массива байтов (отправляемые на сервер в закодированном виде Base64).
```
byte[] digitalSignature = signingData(privateKey, random, docDataJson);
                ZonedDateTime now = ZonedDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                String iso8601String = now.format(formatter);
                DocUpdate docUpdate = new DocUpdate();
                DocUpdate.Data doc = new DocUpdate.Data();
                doc.setSubscription(true);
                doc.setSubscriptionDate(iso8601String);
                doc.setSubscriptionData(byteToString(digitalSignature));
```
### ***Отправка подписанных данных на сервер:***
После создания подписи библиотека отправляет POST-запрос на сервер с использованием переданного baseUrl и подписанных данных. Запрос на сервер отправляется в двух форматах, в зависимости от значения "action" в POST-запросе: для обновления информации о пользователе ("LOG" или "REG") или для проверки корректности документов ("DOC").
```
Call<ResponseBody> call = apiInterface.updateDocData(docId, session, docUpdate);
Response<ResponseBody> response = call.execute();
String responseBody = response.body().string();
```
### ***Проверка подписи на сервере:***
Проверка подписанных данных происходит на сервере. Сервер использует открытый ключ для декодирования и проверки подписи, чтобы убедиться в том, что данные были подписаны с использованием закрытого ключа и не были изменены после подписания.
```
Signature signature = Signature.getInstance("SHA256withRSA");
signature.initVerify(publicKey);
signature.update(anyData.getBytes());
return signature.verify(digitalSignature);
```
## Блок-схема библиотеки
![Принцип работы библиотеки](/images/block_diagram.png)
