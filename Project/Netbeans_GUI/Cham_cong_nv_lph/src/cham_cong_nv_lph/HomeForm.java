/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cham_cong_nv_lph;

import javacard.connect.ConnectCard;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javacard.connect.RSAAppletHelper;
import javacard.utils.RSAData;
import javacard.utils.RandomUtil;
import javax.imageio.ImageIO;
import javax.smartcardio.CardException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Bawcs
 */
public class HomeForm extends javax.swing.JFrame {

    /**
     * Creates new form HomeForm
     */
    private int CheckEnd = 0;
    private String startTime = "";
    // Khai báo JTextArea để hiển thị lịch sử điểm danh
    private JTextArea attendanceHistoryArea;


    public HomeForm() {
        initComponents();
        ConnectCard connect = new ConnectCard();
        if(connect.ReadInformation()){
            txtID.setText(connect.strID);
            txtName.setText(connect.strName);
            txtDate.setText(connect.strDate);
            txtPhone.setText(connect.strPhone);
        }
        jpnInfo.setVisible(true);
        jpnPIN.setVisible(false);
        getImage();
        jpanleAttendance.setVisible(false);
        txtTencoquan.setText("Học Viện Kỹ Thuật Mật Mã");
        txtTencoquan.setEnabled(false);
        txtName.setEnabled(false);
        txtDate.setEnabled(false);
        txtPhone.setEnabled(false);
        txtID.setEnabled(false);
        txtID.setText("001");
        TextAreaLog.setEditable(false);
        showDate();
        showTime();
       
    }
    void showDate(){
        Date date = new Date();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        lableDate.setText(s.format(date));
    }
    void showTime(){
        new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = new Date();
                SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss a");
                lableTime.setText(s.format(d));
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }).start();
    }
    void ResetCheckTime(){
        this.CheckEnd = 0;
    }
    
    public boolean hasObject(File f) {
        // thu doc xem co object nao chua
        FileInputStream fi;
        boolean check = true;
        try {
            fi = new FileInputStream(f);
            ObjectInputStream inStream = new ObjectInputStream(fi);
            if (inStream.readObject() == null) {
                check = false;
            }
            inStream.close();
 
        } catch (FileNotFoundException e) {
            check = false;
        } catch (IOException e) {
            check = false;
        } catch (ClassNotFoundException e) {
            check = false;
            e.printStackTrace();
        }
        return check;
    }
    void inputTime(String dateString,String startTimeString,String endTimeString){
        try {
 
            File f = new File("C:\\Users\\dell\\Documents\\NetBeansProjects\\Cham_cong_nv_lph\\smartcarddata.bin");
            FileOutputStream fo;
            ObjectOutputStream oStream = null;
            if (!f.exists()) {
                fo = new FileOutputStream(f);
                oStream = new ObjectOutputStream(fo);
            } else { 
                if (!hasObject(f)) {
                    fo = new FileOutputStream(f);
                    oStream = new ObjectOutputStream(fo);
                } else { // neu co roi thi ghi them vao
 
                    fo = new FileOutputStream(f, true);
 
                    oStream = new ObjectOutputStream(fo) {
                        protected void writeStreamHeader() throws IOException {
                            reset();
                        }
                    };
                }
            }
            StockFile s = new StockFile(dateString, startTimeString, endTimeString);
            oStream.writeObject(s);
            oStream.close();
 
        } catch (IOException e) {
            System.out.println("javacard.HomeForm.inputTime()" + e);
        }
    } 
    void outputTime() {
    try {
        File f = new File("C:\\Users\\dell\\Documents\\NetBeansProjects\\Cham_cong_nv_lph\\smartcarddata.bin");
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream inStream = new ObjectInputStream(fis);

        Object s;
        int i = 0;
        TextAreaLog.setText("");

        while (true) {
            try {
                s = inStream.readObject();
                String log = ++i + ":" + s.toString() + "\n";
                TextAreaLog.append(log);
            } catch (EOFException eof) {
                // Kết thúc file, thoát khỏi vòng lặp
                break;
            }
        }
        
        inStream.close();
        fis.close();

    } catch (ClassNotFoundException | IOException e) {
        System.out.println("javacard.HomeForm.outputTime() " + e);
    }
} 
    private boolean rsaAuthentication() {
         try {
        // Lấy public key từ cơ sở dữ liệu
        PublicKey publicKey = RSAData.getPublicKey(); // Đảm bảo phương thức này trả về public key từ DB

        if (publicKey == null) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy public key trong cơ sở dữ liệu!");
            return false;
        }

        System.out.println("Public Key: " + Arrays.toString(publicKey.getEncoded()));

        // Tạo dữ liệu ngẫu nhiên để ký
        byte[] data = RandomUtil.randomData(20);

        // Yêu cầu ký dữ liệu với smart card
        byte[] signed = RSAAppletHelper.getInstance(ConnectCard.getInstance().channel).requestSign(data);

        if (signed == null) {
            JOptionPane.showMessageDialog(null, "Lỗi trong quá trình ký!");
            return false;
        }

        System.out.println("Dữ liệu đã ký: " + Arrays.toString(signed));

        // Xác minh dữ liệu đã ký sử dụng public key
        return RSAData.verify(publicKey, signed, data);
    } catch (CardException ex) {
        JOptionPane.showMessageDialog(null, "Lỗi thẻ: " + ex.getMessage());
    }

    return false;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jpnInfor = new javax.swing.JPanel();
        jlbInfo = new javax.swing.JLabel();
        jPanelPIN = new javax.swing.JPanel();
        jlbPIN = new javax.swing.JLabel();
        jPanelConnect = new javax.swing.JPanel();
        jlbConnect = new javax.swing.JLabel();
        jlbPIN1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jpnInfo = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtID = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtTencoquan = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        txtDate = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        image = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jpnPIN = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jPasswordField3 = new javax.swing.JPasswordField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jpanleAttendance = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        btnAttendance = new javax.swing.JButton();
        lableDate = new javax.swing.JLabel();
        lableTime = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TextAreaLog = new javax.swing.JTextArea();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabel2.setText("Trang chủ");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, -1, -1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("X");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 10, 20, 30));

        jButton8.setForeground(new java.awt.Color(255, 102, 102));
        jButton8.setText("AdminForm");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 110));

        jPanel3.setForeground(new java.awt.Color(0, 204, 204));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Người dùng");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Điểm danh");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Tùy chọn");

        jpnInfor.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jlbInfo.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jlbInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-user-32.png"))); // NOI18N
        jlbInfo.setText("Thông tin");
        jlbInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlbInfoMouseClicked(evt);
            }
        });
        jpnInfor.add(jlbInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 190, 42));

        jPanelPIN.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jlbPIN.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jlbPIN.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbPIN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-pin-code-32.png"))); // NOI18N
        jlbPIN.setText("Mã PIN");
        jlbPIN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlbPINMouseClicked(evt);
            }
        });
        jPanelPIN.add(jlbPIN, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 189, 39));

        jPanelConnect.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jlbConnect.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jlbConnect.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-link-32.png"))); // NOI18N
        jlbConnect.setText("Ngắt kết nối");
        jlbConnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlbConnectMouseClicked(evt);
            }
        });
        jPanelConnect.add(jlbConnect, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 189, 36));

        jlbPIN1.setBackground(new java.awt.Color(51, 255, 51));
        jlbPIN1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jlbPIN1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbPIN1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-attendance-32.png"))); // NOI18N
        jlbPIN1.setText("Điểm danh");
        jlbPIN1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jlbPIN1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(114, 114, 114)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlbPIN1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(1, 1, 1))
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jpnInfor, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpnInfor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelPIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jlbPIN1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanelConnect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jpnInfo.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Mã số");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Họ tên");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Ngày sinh");

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Tên cơ quan");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Số điện thoại");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel14.setText("Ảnh cá nhân");

        txtID.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtID.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        txtName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtName.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        txtTencoquan.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        txtPhone.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        txtPhone.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jButton1.setBackground(new java.awt.Color(0, 102, 153));
        jButton1.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Cập nhật");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtDate.setToolTipText("");

        jButton5.setText("chọn ảnh");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(0, 102, 153));
        jButton6.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Sửa");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpnInfoLayout = new javax.swing.GroupLayout(jpnInfo);
        jpnInfo.setLayout(jpnInfoLayout);
        jpnInfoLayout.setHorizontalGroup(
            jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnInfoLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel12))
                .addGap(77, 77, 77)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpnInfoLayout.createSequentialGroup()
                        .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTencoquan)
                            .addComponent(txtPhone))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpnInfoLayout.createSequentialGroup()
                        .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtDate)
                            .addComponent(txtName)
                            .addComponent(txtID, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpnInfoLayout.createSequentialGroup()
                                .addComponent(image, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jpnInfoLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton5)
                                    .addComponent(jLabel14)))))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpnInfoLayout.createSequentialGroup()
                .addGap(160, 160, 160)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(206, 206, 206))
        );

        jpnInfoLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel9});

        jpnInfoLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtID, txtName, txtPhone, txtTencoquan});

        jpnInfoLayout.setVerticalGroup(
            jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnInfoLayout.createSequentialGroup()
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpnInfoLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpnInfoLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jpnInfoLayout.createSequentialGroup()
                                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtID, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)
                                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jpnInfoLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(image, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addGap(20, 20, 20)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtTencoquan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jpnInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpnInfoLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel9});

        jpnInfoLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {txtID, txtName, txtPhone, txtTencoquan});

        jpnInfoLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton1, jButton6});

        jPanel4.add(jpnInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jpnPIN.setBackground(new java.awt.Color(255, 255, 255));
        jpnPIN.setPreferredSize(new java.awt.Dimension(712, 465));

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Mã PIN cũ");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Mã PIN mới");

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Xác nhận mã PIN");

        jPasswordField1.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jPasswordField2.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jPasswordField3.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        jButton2.setBackground(new java.awt.Color(0, 102, 153));
        jButton2.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Cập nhật");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(0, 102, 153));
        jButton3.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Hủy bỏ");

        javax.swing.GroupLayout jpnPINLayout = new javax.swing.GroupLayout(jpnPIN);
        jpnPIN.setLayout(jpnPINLayout);
        jpnPINLayout.setHorizontalGroup(
            jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnPINLayout.createSequentialGroup()
                .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpnPINLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18))
                        .addGap(31, 31, 31)
                        .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jpnPINLayout.createSequentialGroup()
                        .addGap(157, 157, 157)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(104, 104, 104)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(103, Short.MAX_VALUE))
        );

        jpnPINLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel16, jLabel17, jLabel18});

        jpnPINLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPasswordField1, jPasswordField2, jPasswordField3});

        jpnPINLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton2, jButton3});

        jpnPINLayout.setVerticalGroup(
            jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnPINLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(70, 70, 70)
                .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(55, 55, 55)
                .addGroup(jpnPINLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        jpnPINLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel16, jLabel17, jLabel18});

        jpnPINLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPasswordField1, jPasswordField2, jPasswordField3});

        jpnPINLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton2, jButton3});

        jPanel4.add(jpnPIN, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jpanleAttendance.setBackground(new java.awt.Color(255, 255, 255));
        jpanleAttendance.setPreferredSize(new java.awt.Dimension(712, 465));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Điểm danh");

        btnAttendance.setBackground(new java.awt.Color(0, 102, 153));
        btnAttendance.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnAttendance.setForeground(new java.awt.Color(255, 255, 255));
        btnAttendance.setText("Điểm danh");
        btnAttendance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttendanceActionPerformed(evt);
            }
        });

        lableDate.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lableDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lableTime.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lableTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jButton7.setText("reset");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        TextAreaLog.setColumns(20);
        TextAreaLog.setRows(5);
        jScrollPane1.setViewportView(TextAreaLog);

        javax.swing.GroupLayout jpanleAttendanceLayout = new javax.swing.GroupLayout(jpanleAttendance);
        jpanleAttendance.setLayout(jpanleAttendanceLayout);
        jpanleAttendanceLayout.setHorizontalGroup(
            jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpanleAttendanceLayout.createSequentialGroup()
                .addGroup(jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpanleAttendanceLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAttendance, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(136, 136, 136)
                        .addComponent(jButton7))
                    .addGroup(jpanleAttendanceLayout.createSequentialGroup()
                        .addGroup(jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpanleAttendanceLayout.createSequentialGroup()
                                .addGap(241, 241, 241)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jpanleAttendanceLayout.createSequentialGroup()
                                .addGap(107, 107, 107)
                                .addComponent(lableDate, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(61, 61, 61)
                                .addComponent(lableTime, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpanleAttendanceLayout.createSequentialGroup()
                .addGap(0, 90, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );
        jpanleAttendanceLayout.setVerticalGroup(
            jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpanleAttendanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lableDate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lableTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpanleAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAttendance, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                .addContainerGap())
        );

        jpanleAttendanceLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lableDate, lableTime});

        jPanel4.add(jpanleAttendance, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jButton4.setBackground(new java.awt.Color(102, 102, 102));
        jButton4.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Thoát");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1100, 580));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jlbPINMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlbPINMouseClicked
        // TODO add your handling code here:
        jpnPIN.setVisible(true);
        jpnInfo.setVisible(false);
        jpanleAttendance.setVisible(false);
        jPanelPIN.setBackground(Color.white);
        //jpnInfor.setBackground(new Color(240,240,240));
        jlbInfo.setBackground(new Color(240,240,240));
        //jPanelConnect.setBackground(new Color(240,240,240));
    }//GEN-LAST:event_jlbPINMouseClicked

    private void jlbInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlbInfoMouseClicked
        // TODO add your handling code here:
        jpnInfo.setVisible(true);
        jpnPIN.setVisible(false);
        jpanleAttendance.setVisible(false);
        jpnInfor.setBackground(Color.white);
        //jPanelPIN.setBackground(new Color(240,240,240));
        //jPanelConnect.setBackground(new Color(240,240,240));
        
    }//GEN-LAST:event_jlbInfoMouseClicked

    private void jlbConnectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlbConnectMouseClicked
            // Đổi màu nền để phản hồi người dùng
        jPanelConnect.setBackground(Color.white);

        // Ngắt kết nối thẻ khi nhấn nút
        ConnectCard connectCard = ConnectCard.getInstance();
        connectCard.disconnect();

        // Cập nhật trạng thái giao diện
        jPanelPIN.setBackground(new Color(240, 240, 240)); // Ví dụ reset màu
        jpnInfor.setBackground(new Color(240, 240, 240)); // Reset màu cho panel thông tin
        
    }//GEN-LAST:event_jlbConnectMouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        LoginForm login = new LoginForm();
        login.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jlbPIN1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jlbPIN1MouseClicked
        // TODO add your handling code here:
        jpanleAttendance.setVisible(true);
        jpnInfo.setVisible(false);
        jpnPIN.setVisible(false);
        //outputTime();
    }//GEN-LAST:event_jlbPIN1MouseClicked

    private void btnAttendanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttendanceActionPerformed
        // TODO add your handling code here:
    if(rsaAuthentication()){
            String date = lableDate.getText();
            String time = lableTime.getText();
            switch (CheckEnd) {
                case 0:
                    this.startTime = time;
                    this.CheckEnd = 1;
                    JOptionPane.showMessageDialog(null, "Điểm danh đến thành công! Chúc bạn một ngày làm việc vui vẻ");
                    break;
                case 1:
                    inputTime(date, startTime, time);
                    outputTime();
                    this.CheckEnd = 2;
                    JOptionPane.showMessageDialog(null, "Điểm danh thành công!");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Bạn đã điểm danh ngày hôm nay! Vui lòng quay lại vào ngày mai");
                    break;
            }
            
            
        }
        else{
            System.out.println("RSA ERROR");
        }
    }//GEN-LAST:event_btnAttendanceActionPerformed
    
    
    
    
    
    
    
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        
        String strId = txtID.getText();
        String strName = txtName.getText();
        String strDate = txtDate.getText();
        String strPhone = txtPhone.getText();
        
        byte[] byteID = strId.getBytes();
        byte[] byteName = strName.getBytes();
        byte[] byteDate = strDate.getBytes();
        byte[] bytePhone = strPhone.getBytes();
        
        ConnectCard connect = new ConnectCard();
        byte[] data = new byte[byteID.length+byteName.length+byteDate.length+bytePhone.length+8];
        int offSet = 0;
        data[0] = (byte)0x02;
        offSet += 1;
        System.arraycopy(byteID, 0,data, offSet, byteID.length);
        offSet += byteID.length;
        data[offSet] = (byte)0x03;
        offSet += 1;
        data[offSet] = (byte)0x02;
        offSet += 1;
        System.arraycopy(byteName, 0,data, offSet, byteName.length);
        offSet += byteName.length;
        data[offSet] = (byte) 0x03;
        offSet += 1;
        data[offSet] = (byte) 0x02;
        offSet += 1;
        System.arraycopy(byteDate, 0, data, offSet, byteDate.length);
        offSet += byteDate.length;
        data[offSet] = (byte)0x03;
        offSet += 1;
        data[offSet] = (byte)0x02;
        offSet += 1;
        System.arraycopy(bytePhone, 0, data, offSet, bytePhone.length);
        offSet += bytePhone.length;
        data[offSet] = (byte)0x03;
        
        if(connect.EditInformation(data)){
            try {
                PublicKey publicKeys = RSAAppletHelper.getInstance(
                        ConnectCard.getInstance().channel).getPublicKey();
            } catch (CardException ex) {
                Logger.getLogger(HomeForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            HomeForm home = new HomeForm();
            home.setVisible(true);
            this.dispose();
            
            System.out.println("Success");
        }
        else{
            System.out.println("Sending Error");
        }
        
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        String strOld = jPasswordField1.getText();
        String strNew = jPasswordField2.getText();
        String strCofirm = jPasswordField3.getText();
        
        if(strNew.equals(strCofirm) && !strNew.equals(strOld)){
            ConnectCard cn = new ConnectCard();
            if(cn.ChangePIN(strOld, strNew)){
                System.out.println("PIN CHANGE SUCCESS");
            }
            else{
                System.out.println("PIN CHANGE ERROR");
            }
        }
        else{
            JOptionPane.showMessageDialog(null, "Kiểm tra mã PIN");
        }
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
            // TODO add your handling code here:
            JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new JPEGImageFileFilter());
        jfc.showOpenDialog(this);
        File file = jfc.getSelectedFile();

        if (file != null) {
            try {
            if (file.length() > 10000) {
                // Nén lại ảnh nếu kích thước lớn
                BufferedImage originalImage = ImageIO.read(file);
                int targetWidth = 300; 
                int targetHeight = 300; 

                // Thay đổi kích thước ảnh
                Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                BufferedImage resizedBufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resizedBufferedImage.createGraphics();
                g2d.drawImage(resizedImage, 0, 0, null);
                g2d.dispose();

                // Lưu lại ảnh nén tạm thời
                File tempFile = new File("resized_image.jpg");
                ImageIO.write(resizedBufferedImage, "jpg", tempFile);

                // Hiển thị giao diện xem trước với ảnh nén
                ReviewAvatarUI avatarUI = new ReviewAvatarUI(tempFile, this);
                avatarUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                avatarUI.setLocationRelativeTo(null);
                avatarUI.setVisible(true);
            }
            else {
                // Hiển thị giao diện xem trước với ảnh gốc
                ReviewAvatarUI avatarUI = new ReviewAvatarUI(file, this);
                avatarUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                avatarUI.setLocationRelativeTo(null);
                avatarUI.setVisible(true);
                
            }
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Lỗi xử lý ảnh: " + ex.getMessage());
        }
    }

    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
//        txtID.setEnabled(false);
//        txtID.setText("001");
        txtTencoquan.setEnabled(true);
        txtName.setEnabled(true);
        txtDate.setEnabled(true);
        txtPhone.setEnabled(true);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        ResetCheckTime();
        this.startTime = "";
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        new AdminForm().setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed
    public class JPEGImageFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".jpeg")) {
                return true;
            }
            if (f.getName().toLowerCase().endsWith(".jpg")) {
                return true;
            }
            return f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "JPEG files";
        }

    }
    public void getImage() {
        ConnectCard connect = new ConnectCard();
        BufferedImage imageBuf = connect.DownloadImage();
        if (imageBuf != null) {
            image.setIcon(new ImageIcon(imageBuf));
        } else {
            image.setHorizontalAlignment(JTextField.CENTER);
            image.setText("Chưa cập nhật");
        }

    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TextAreaLog;
    private javax.swing.JButton btnAttendance;
    private javax.swing.JLabel image;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelConnect;
    private javax.swing.JPanel jPanelPIN;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel jlbConnect;
    private javax.swing.JLabel jlbInfo;
    private javax.swing.JLabel jlbPIN;
    private javax.swing.JLabel jlbPIN1;
    private javax.swing.JPanel jpanleAttendance;
    private javax.swing.JPanel jpnInfo;
    private javax.swing.JPanel jpnInfor;
    private javax.swing.JPanel jpnPIN;
    private javax.swing.JLabel lableDate;
    private javax.swing.JLabel lableTime;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtID;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtTencoquan;
    // End of variables declaration//GEN-END:variables
}
