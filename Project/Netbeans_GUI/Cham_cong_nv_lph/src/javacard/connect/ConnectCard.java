/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacard.connect;

import javacard.utils.ConvertData;
import com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import jdk.nashorn.internal.ir.Terminal;
import java.util.List;
import javax.smartcardio.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javacard.define.APPLET;
import javacard.define.RESPONS;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author Spark_Mac
 */
public class ConnectCard {
    public byte [] data;
    public String message;
    public String strID;
    public String strName;
    public String strDate;
    public String strPhone;
    
    private static final byte INS_SAVE_ATTENDANCE_LOG = (byte) 0x70;
    private Card card;
    private TerminalFactory factory;
    public CardChannel channel;
    private CardTerminal terminal;
    private List<CardTerminal> terminals;
    
    private static ConnectCard instance;
    private boolean isConnected = false;
    public static ConnectCard getInstance() {
        if (instance == null) {
            instance = new ConnectCard();
        }
        return instance;
    }
    public String connectapplet(){
        try{
            
            factory = TerminalFactory.getDefault();
            terminals = factory.terminals().list();
            
            terminal = terminals.get(0);
            
            card = terminal.connect("*");
            
            channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0x00,0xA4,0x04,0x00,APPLET.AID_APPLET));
            String kq = answer.toString();
            data = answer.getData();
            return kq;
            
        }
        catch(Exception ex){
            return "Error";
        }
    }
    public void disconnect() {
    try {
        if (card != null) {
            card.disconnect(true); // Ngắt kết nối và reset thẻ
            card = null; // Đặt về null để tránh tái sử dụng
            channel = null; // Reset channel
            JOptionPane.showMessageDialog(null, "Thẻ đã được thoát thành công!");
        } else {
            JOptionPane.showMessageDialog(null, "Không có thẻ nào đang được kết nối.");
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Lỗi khi thoát thẻ: " + ex.getMessage());
    }
 }
    
    
    public boolean verifyPin(String pin){
        connectapplet();
        byte[] pinbyte =  pin.getBytes();
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_VERIFY_PIN,0x00,0x00,pinbyte));
            message = Integer.toHexString(answer.getSW());
            switch (message.toUpperCase()) {
                case RESPONS.SW_NO_ERROR:
                    return true;
                case RESPONS.SW_AUTH_FAILED:
                    JOptionPane.showMessageDialog(null, "Bạn đã nhập sai PIN");
                    return false;
                case RESPONS.SW_IDENTITY_BLOCKED:
                    JOptionPane.showMessageDialog(null, "Bạn đã nhập sai quá số lần thử!Thẻ đã bị khoá");
                    return false;
                case RESPONS.SW_INVALID_PARAMETER:
                    JOptionPane.showMessageDialog(null, "Độ dài pin chưa hợp lệ");
                    return false;
                default:
                    return false;
            }
            
        }
        catch(Exception ex){
            return false;
        }
    }
    
    public boolean createPIN(String pin){
        
        byte[] pinbyte =  pin.getBytes();
        byte lengt = (byte) pinbyte.length;
        
        byte[] send = new byte[lengt+1];
        send[0] = lengt;
        for(int i =1;i<send.length;i++){
            send[i] = pinbyte[i-1];
        }
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_CREATE_PIN,0x00,0x03,send));
            
            message = answer.toString();
            switch (((message.split("="))[1]).toUpperCase()) {
                case RESPONS.SW_NO_ERROR:
                    return true;
                case RESPONS.SW_INVALID_PARAMETER:
                    JOptionPane.showMessageDialog(null, "Lỗi độ dài pin");
                    return false;
                case RESPONS.SW_WRONG_LENGTH:
                    JOptionPane.showMessageDialog(null, "Lỗi SW_WRONG_LENGTH");
                    return false;
                default:
                    return false;
            }
            
        }
        catch(Exception ex){
            return false;
        }
    }
    
    public boolean ChangePIN(String oldPin,String newPin){
        connectapplet();
        byte[] pinOldByte =  oldPin.getBytes();
        byte lengtOld = (byte) pinOldByte.length;
        
        byte[] pinNewByte =  newPin.getBytes();
        byte lengtNew = (byte) pinNewByte.length;
        
        byte[] send = new byte[lengtNew+lengtOld+2];
        int offSet = 0;
        send[offSet] = lengtOld;
        offSet+=1;
        System.arraycopy(pinOldByte, 0, send, offSet, lengtOld);
        offSet+=lengtOld;
        send[offSet] = lengtNew;
        offSet+=1;
        System.arraycopy(pinNewByte, 0, send, offSet, lengtNew);
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_CHANGE_PIN,0x00,0x00,send));
            
            message = answer.toString();
            switch (((message.split("="))[1]).toUpperCase()) {
                case RESPONS.SW_NO_ERROR:
                    JOptionPane.showMessageDialog(null, "Cập nhật PIN thành công!");
                    return true;
                case RESPONS.SW_AUTH_FAILED:
                    JOptionPane.showMessageDialog(null, "Bạn đã nhập sai PIN");
                    return false;
                case RESPONS.SW_IDENTITY_BLOCKED:
                    JOptionPane.showMessageDialog(null, "Bạn đã nhập sai quá số lần thử!Thẻ đã bị khoá");
                    return false;
                default:
                    return false;
            }
            
        }
        catch(Exception ex){
            return false;
        }
    }
    public boolean UnblockPin(byte [] aid){
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU selectBlockcard = channel.transmit(new CommandAPDU(0x00,0xA4,0x00,0x00,aid));
            
            String check = Integer.toHexString(selectBlockcard.getSW());
            
            if(check.equals(RESPONS.SW_NO_ERROR)){
                CardChannel channel2 = card.getBasicChannel();
            
            ResponseAPDU unblockCard = channel2.transmit(new CommandAPDU(APPLET.CLA,APPLET.INS_UNBLOCK_PIN,0x00,0x00));
                message = unblockCard.toString();
                switch (((message.split("="))[1]).toUpperCase()) {
                    case RESPONS.SW_NO_ERROR:
                        JOptionPane.showMessageDialog(null, "Mở khoá thẻ thành công");
                        return true;
                    case RESPONS.SW_OPERATION_NOT_ALLOWED:
                        JOptionPane.showMessageDialog(null, "Thẻ không bị khoá vui lòng kiểm tra lại!");
                        return false;
                    default:
                        return false;
                }
            }
            else{
                return false;
            }
        }
        catch(Exception ex){
            return false;
        }
    }
    
    public void setUp(){
        
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_SETUP,0x00,0x00));
            
        }
        catch(Exception ex){
            //return "Error";
        }
    
    }
    
    public boolean EditInformation(byte [] data){
        connectapplet();
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel0 = card.getBasicChannel();
            ResponseAPDU resetData = channel0.transmit(new CommandAPDU(0xB0,APPLET.INS_CHANGE_INFORMATION,0x00,0x00));
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answer = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_CREATE_INFORMATION,0x00,0x00,data));
            
            message = answer.toString();
            switch (((message.split("="))[1]).toUpperCase()) {
                case "9000":
                    JOptionPane.showMessageDialog(null, "Cập nhật thông tin thành công!");
                    return true;
                case RESPONS.SW_WRONG_LENGTH:
                    JOptionPane.showMessageDialog(null, "Dữ liệu quá lớn, vui lòng kiểm tra lại!");
                    return false;
                default:
                    return false;
            }
            
        }
        catch(Exception ex){
            return false;
        }
    }
    public boolean ReadInformation(){
        connectapplet();
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            ResponseAPDU answerID = channel.transmit(new CommandAPDU(0xB0,APPLET.INS_OUT_INFORMATION,APPLET.OUT_ID,0x00));
            strID = new String(answerID.getData());
            
            CardChannel channel1 = card.getBasicChannel();
            ResponseAPDU answerName = channel1.transmit(new CommandAPDU(0xB0,APPLET.INS_OUT_INFORMATION,APPLET.OUT_NAME,0x00));
            strName = new String(answerName.getData());
            
            CardChannel channel3 = card.getBasicChannel();
            ResponseAPDU answerDate = channel3.transmit(new CommandAPDU(0xB0,APPLET.INS_OUT_INFORMATION,APPLET.OUT_DATE,0x00));
            strDate = new String(answerDate.getData());
            
            CardChannel channel4 = card.getBasicChannel();
            ResponseAPDU answerPhone = channel4.transmit(new CommandAPDU(0xB0,APPLET.INS_OUT_INFORMATION,APPLET.OUT_PHONE,0x00));
            strPhone = new String(answerPhone.getData());
            return true;
        }
        catch(Exception ex){
            return false;
        }
    }   
    public boolean UploadImage(File file, String type){
        connectapplet();
        try{
            
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channel = card.getBasicChannel();
            
            BufferedImage bImage = ImageIO.read(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bImage, type, bos);
            
            byte[] imageData = bos.toByteArray();
            int totalChunks = imageData.length/249;
            int lastChunkSize = imageData.length % 249;
            
           
                // Gửi kích thước ảnh cho Applet
            byte[] sizeInfo = ByteBuffer.allocate(4).putInt(imageData.length).array();
            ResponseAPDU response = channel.transmit(new CommandAPDU(0xB0, APPLET.INS_CREATE_SIZEIMAGE, 0x00, 0x00, sizeInfo));

            String check = Integer.toHexString(response.getSW());
            
            if(check.equals(RESPONS.SW_NO_ERROR)){
                for(int i = 0;i<= totalChunks;i++){
                    int chunkSize = (i == totalChunks) ? lastChunkSize : 249;
                    byte[] chunk = Arrays.copyOfRange(imageData, i * 249, i * 249 + chunkSize);

                    response = channel.transmit(new CommandAPDU(0xB0, APPLET.INS_CREATE_IMAGE, i, 0x00, chunk));
                    String checkChunk = Integer.toHexString(response.getSW());
                    
                    if (!checkChunk.equals(RESPONS.SW_NO_ERROR)){
                        return false;
                    }
          
                }
                return true;
            }
            return false;
        }
        catch(Exception ex){
            return false;
        }
    }
    public BufferedImage DownloadImage(){
        connectapplet();
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            
            List<CardTerminal> terminals = factory.terminals().list();
            
            CardTerminal terminal = terminals.get(0);
            
            Card card = terminal.connect("*");
            
            CardChannel channelImage = card.getBasicChannel();
            
            // Lấy kích thước ảnh từ Applet
            ResponseAPDU response = channel.transmit(new CommandAPDU(0xB0, APPLET.INS_OUT_SIZEIMAGE, 0x00, 0x00));
            String check = Integer.toHexString(response.getSW());
            if(check.equals(RESPONS.SW_NO_ERROR)){
                byte[] sizeInfo = response.getData();
                int imageSize = ByteBuffer.wrap(sizeInfo).getInt();

                byte[] imageData = new byte[imageSize];
                int totalChunks = imageSize / 249;
                int lastChunkSize = imageSize % 249;
                
                 // Nhận từng gói dữ liệu từ Applet
                for (int i = 0; i <= totalChunks; i++) {
                    response = channel.transmit(new CommandAPDU(0xB0, APPLET.INS_OUT_IMAGE, i, 0x00));
                    String checkChunk = Integer.toHexString(response.getSW());
                    if (checkChunk.equals(RESPONS.SW_NO_ERROR)) {
                        int chunkSize = (i == totalChunks) ? lastChunkSize : 249;
                        byte[] chunk = response.getData();
                        System.arraycopy(chunk, 0, imageData, i * 249, chunkSize);
                    }
                }
                // Chuyển dữ liệu thành ảnh
                ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                return ImageIO.read(bais);
           }
        } catch (Exception e) {
            System.err.println("error dowloadimage");
        }
        return null;
    }
    
    

}