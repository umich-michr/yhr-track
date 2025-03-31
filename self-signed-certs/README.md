To create self-signed certificates for local development, you can use the following command:

```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout selfsigned.key -out selfsigned.crt -days 365 -subj "/C=US/ST=State/L=Locality/O=Organization/CN=localhost"
```

To import self signed certificates into your java keystore:
```bash
keytool -importcert -file selfsigned.crt -keystore selfsigned.jks -storepass changeit -keypass changeit -alias selfsigned

```
