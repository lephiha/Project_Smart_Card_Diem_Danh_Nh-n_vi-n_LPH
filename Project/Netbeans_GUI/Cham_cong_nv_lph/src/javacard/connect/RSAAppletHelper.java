/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.connect;

import java.security.PublicKey;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javacard.define.APPLET;
import javacard.define.RESPONS;
import javacard.define.RSA;
import javacard.utils.RSAData;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author Admin
 */
public class RSAAppletHelper {

    private Card card;
    private TerminalFactory factory;
    private final CardChannel channel;
    private CardTerminal terminal;
    private List<CardTerminal> terminals;

    private static RSAAppletHelper instance;

    private RSAAppletHelper(CardChannel channel) {
        this.channel = channel;
    }

    public static RSAAppletHelper getInstance(CardChannel channel) {
        if (instance == null) {
            instance = new RSAAppletHelper(channel);
        }
        return instance;
    }
    
    public byte[] requestSign(byte[] data) throws CardException {
        try {
            if (selectRSAApplet()) {
                TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            ResponseAPDU response = channel.transmit(new CommandAPDU(APPLET.CLA,RSA.INS_SIGN,0x00,0x00,data));
                String check = Integer.toHexString(response.getSW());
                if (check.equals(RESPONS.SW_NO_ERROR)) {
                      return response.getData();
                }
            }
        } catch (CardException ex) {
            Logger.getLogger(RSAAppletHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return null;
    }

    public PublicKey getPublicKey() throws CardException {
        try {
            if (selectRSAApplet()) {
                TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
                ResponseAPDU response = channel.transmit(new CommandAPDU(APPLET.CLA,RSA.INS_GET_PUB_MODULUS,0x00,0x00));
                String check = Integer.toHexString(response.getSW());
                if (check.equals(RESPONS.SW_NO_ERROR)) {
                    byte[] modulusBytes = response.getData();
                    CardChannel channel1 = card.getBasicChannel();
                ResponseAPDU response1 = channel1.transmit(new CommandAPDU(APPLET.CLA,RSA.INS_GET_PUB_EXPONENT,0x00,0x00));
                    String check1 = Integer.toHexString(response1.getSW());
                    if (check1.equals(RESPONS.SW_NO_ERROR)) {
                        byte[] exponentBytes = response1.getData();
                        PublicKey publicKey= RSAData.initPublicKey(modulusBytes, exponentBytes);
                        if(publicKey!=null){
                            RSAData.savePublicKey(publicKey);
                            return publicKey;
                        }
                    }
                }
            }
        } catch (CardException ex) {
            Logger.getLogger(RSAAppletHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("GETPUBLISHKEY-ERROR");
            throw ex;
        }
        return null;
    }

    public boolean selectRSAApplet() throws CardException {
        try {
            ResponseAPDU response = selectApplet(APPLET.AID_RSA);
            String check = Integer.toHexString(response.getSW());

            if (check.equals(RESPONS.SW_NO_ERROR)) {
                
                return true;
            }
        } catch (CardException ex) {
            System.out.println("Lỗi select info applet");
            throw ex;
        }
        System.out.println("Lỗi khởi tạo length");
        return false;
    }

    public ResponseAPDU sendAPDU(
            int cla, int ins, int p1, int p2, byte[] data
    ) throws CardException {
        if (channel == null) {
            throw new CardException(RESPONS.SW_UNKNOWN);
        }
        return channel.transmit(new CommandAPDU(
                cla, ins, p1, p2, data));
    }

    // select
    private ResponseAPDU selectApplet(byte[] aid) throws CardException {
            
         TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
        ResponseAPDU response = channel.transmit(new CommandAPDU(
                0x00, 0xA4,
                0x04, 0x00,
                aid)
        );
        return response;
    }
}
