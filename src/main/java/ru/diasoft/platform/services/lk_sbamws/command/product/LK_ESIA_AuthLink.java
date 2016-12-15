package ru.diasoft.platform.services.lk_sbamws.command.product;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.TimeZone;

/*
<dependency>
<groupId>org.bouncycastle</groupId>
<artifactId>bcprov-jdk16</artifactId>
<version>1.46</version>
</dependency>
*/

public class LK_ESIA_AuthLink {

    public static void main(String... args) throws Exception {
        TimeZone tzone = TimeZone.getDefault();
        System.out.println("Dn:"+tzone.getDisplayName());
        System.out.println("Id:"+tzone.getID());
        tzone.setID(tzone.getID());
        System.out.println(Utility.get_timestamp());
        System.out.println(get_signature("test", "", "", ""));
    }

    private static String get_signature(String scope, String timestamp, String client_id, String state) throws Exception {
        String string_to_sign = String.format("%s%s%s%s",scope,timestamp,client_id,state);
        String certificate_file = "sb_am_esia.pem";
        String keyPath = "sb_am_esia.key";
        final String   passphrase = "1234";

        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(Files.newInputStream(Paths.get("src/main/resources/" + certificate_file)));

        PEMReader pr = new PEMReader(Files.newBufferedReader(Paths.get("src/main/resources/" + keyPath)), new PasswordFinder() {
            //@Override
            public char[] getPassword() {
                return passphrase.toCharArray();
            }
        });

        PrivateKey privateKey = ((KeyPair) pr.readObject()).getPrivate();

        CMSTypedData msg = new CMSProcessableByteArray(string_to_sign.getBytes("UTF-8"));
        Store certs = new JcaCertStore(Collections.singletonList(cert));

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privateKey);
        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                        .build(sha1Signer, cert));
        gen.addCertificates(certs);
        CMSSignedData sigData = gen.generate(msg, false);
        return Base64.encode(sigData.getEncoded());
    }
        
}


