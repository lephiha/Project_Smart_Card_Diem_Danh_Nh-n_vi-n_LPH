/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Admin
 */
public class RSAData {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/rsa_key";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void savePublicKey(PublicKey publicKey) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        
        // Kiểm tra xem key đã tồn tại chưa
        String checkSql = "SELECT COUNT(*) FROM rsa_keys WHERE key_name = ?";
        pstmt = conn.prepareStatement(checkSql);
        pstmt.setString(1, "default_key");
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        
        if (count > 0) {
            // Nếu key tồn tại, thực hiện cập nhật
            String updateSql = "UPDATE rsa_keys SET public_key = ? WHERE key_name = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setBytes(1, publicKey.getEncoded());
            pstmt.setString(2, "default_key");
            pstmt.executeUpdate();
            System.out.println("Public key đã được cập nhật.");
        } else {
            // Nếu key chưa tồn tại, thực hiện chèn mới
            String insertSql = "INSERT INTO rsa_keys (key_name, public_key) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, "default_key");
            pstmt.setBytes(2, publicKey.getEncoded());
            pstmt.executeUpdate();
            System.out.println("Public key đã được lưu vào cơ sở dữ liệu.");
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Đóng tài nguyên
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

    public static PublicKey getPublicKey() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
        // Kết nối đến cơ sở dữ liệu
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        // Câu lệnh SQL để lấy PublicKey
        String sql = "SELECT public_key FROM rsa_keys WHERE key_name = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "default_key"); 

        // Thực thi câu lệnh
        rs = pstmt.executeQuery();

        if (rs.next()) {
            // Lấy byte[] của PublicKey từ cơ sở dữ liệu
            byte[] keyBytes = rs.getBytes("public_key");

            // Tạo PublicKey từ byte[]
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Đóng tài nguyên
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return null;
    }

    private static File createKeyFile(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        return file;
    }

    public static PublicKey initPublicKey(byte[] modulusBytes, byte[] exponentBytes) {
        try {
            BigInteger modulus = new BigInteger(
                    1,
                    modulusBytes
            );
            BigInteger exponent = new BigInteger(
                    1,
                    exponentBytes
            );

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pub = factory.generatePublic(spec);

            return pub;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(RSAData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean verify(PublicKey publicKey, byte[] sigBytes, byte[] data) {
        try {
            Signature signature1 = Signature.getInstance("SHA1withRSA");
            signature1.initVerify(publicKey);
            signature1.update(data);
            boolean result = signature1.verify(sigBytes);
            return result;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RSAData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(RSAData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(RSAData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
