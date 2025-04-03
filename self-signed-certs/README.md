## Comparison of RSA and Elliptic Curve Cryptography (ECC) as used below:
- RSA (Rivest-Shamir-Adleman) is a widely used public-key cryptosystem that relies on the difficulty of factoring large integers.
- ECC (Elliptic Curve Cryptography) is based on the algebraic structure of elliptic curves over finite fields, providing similar security with smaller key sizes.

### Characteristics of secp384r1
* Curve Name: secp384r1
* Key Size: 384 bits
* Security Level: Equivalent to a 7680-bit RSA key (as per NIST recommendations)
* Curve Equation:  over a finite field
* Standards: Defined in standards such as FIPS 186-4 (Digital Signature Standard) and RFC 5639
  
### Advantages of ECC over RSA:
* Security: secp384r1 provides a much higher security level compared to a 2048-bit RSA key. A 384-bit ECC key is roughly equivalent in security to a 7680-bit RSA key.
* Key Size: ECC keys are significantly smaller than RSA keys for the same security level, reducing storage and transmission overhead.
* Performance: ECC operations are generally faster and more efficient than RSA operations, making ECC a better choice for performance-critical applications.

## Create self-signed certificates for local development

```bash
# Generate a self-signed certificate using elliptic curve cryptography
openssl req -x509 -newkey ec:<(openssl ecparam -name secp384r1) -nodes -keyout selfsigned.key -out selfsigned.crt -days 3650 -subj "/C=US/ST=State/L=Locality/O=Organization/CN=localhost"
# Generate a self-signed certificate using RSA
openssl req -x509 -newkey rsa:2048 -nodes -keyout selfsigned.key -out selfsigned.crt -days 3650 -subj "/C=US/ST=State/L=Locality/O=Organization/CN=localhost"
# create a PKCS#12 file that contains both the certificate and the private key, and then import that into the Java KeyStore. For Java KeyStores, both the certificate and the private key need to be present for the entry.
openssl pkcs12 -export -in selfsigned.crt -inkey selfsigned.key -out selfsigned.p12 -name selfsigned -password pass:changeit
```

To import self signed certificates into your java keystore:
```bash
keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore keystore.jks -srckeystore selfsigned.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias selfsigned
```
