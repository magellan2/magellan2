// class magellan.client.swing.OrderWriterDialogTest
// created on Mar 29, 2022
//
// Copyright 2003-2022 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.client.swing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

import magellan.client.swing.OrderWriterDialog.EmailVerifier;

public class OrderWriterDialogTest {

  @Before
  public void setUp() throws Exception {
  }

  public void storeKey() throws KeyStoreException, IOException, NoSuchAlgorithmException,
      CertificateException {
    // Creating the KeyStore object
    KeyStore keyStore = KeyStore.getInstance("JCEKS");

    // Loading the KeyStore object
    char[] password = "changeit".toCharArray();
    String path = "cacerts";
    File keyFile = new File(path);
    if (!keyFile.isFile()) {
      System.out.println("keystore created");
      keyStore.load(null);
      keyStore.store(new FileOutputStream(keyFile), password);
    } else {
      System.out.println("keystore found");
      java.io.FileInputStream fis = new FileInputStream(path);
      keyStore.load(fis, password);
    }

    // Creating the KeyStore.ProtectionParameter object
    KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(password);

    // Creating SecretKey object
    SecretKey mySecretKey = new SecretKeySpec("myPassword".getBytes(), "DSA");

    // Creating SecretKeyEntry object
    KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(mySecretKey);
    keyStore.setEntry("secretKeyAlias", secretKeyEntry, protectionParam);

    // Storing the KeyStore object
    java.io.FileOutputStream fos = null;
    fos = new java.io.FileOutputStream("newKeyStoreName");
    keyStore.store(fos, password);
    System.out.println("data stored");
  }

  @Test
  public void testLoadKey() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
      UnrecoverableEntryException {

    storeKey();

    // Creating the KeyStore object
    KeyStore keyStore = KeyStore.getInstance("JCEKS");

    // Loading the KeyStore object
    char[] password = "changeit".toCharArray();
    String path = "newKeyStoreName";
    String pwAlias = "secretKeyAlias";
    File keyFile = new File(path);
    if (!keyFile.isFile()) {
      keyStore.load(null);
      keyStore.store(new FileOutputStream(keyFile), password);
    } else {
      java.io.FileInputStream fis = new FileInputStream(path);
      keyStore.load(fis, password);
    }

    // Creating the KeyStore.ProtectionParameter object
    KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(password);

    SecretKeyEntry entry = (SecretKeyEntry) keyStore.getEntry(pwAlias, protectionParam);
    SecretKey key = entry.getSecretKey();
    System.out.println("key was " + new String(key.getEncoded()));

    System.out.println("data read");
  }

  @Test
  public void testEmailVerifier() {
    JTextField t = new JTextField("an@example.com");
    EmailVerifier v = new OrderWriterDialog.EmailVerifier();

    assertTrue(v.verify(t));
    t.setText("x");
    assertFalse(v.verify(t));
    t.setText("");
    assertFalse(v.verify(t));

    v = new EmailVerifier(true, false, "xyz");
    t.setText("xyz@x.com");
    assertTrue(v.verify(t));
    t.setText("");
    assertTrue(v.verify(t));
    t.setText("xyz@x.com;a@b.com");
    assertFalse(v.verify(t));

    v = new EmailVerifier(false, true, "[;,]");
    t.setText("");
    assertFalse(v.verify(t));
    t.setText("xyz@x.com");
    assertTrue(v.verify(t));
    t.setText("xyz@x.com,a@b.com;c@d.com");
    assertTrue(v.verify(t));
    t.setText("xyz@x.com,a");
    assertFalse(v.verify(t));
  }

}
