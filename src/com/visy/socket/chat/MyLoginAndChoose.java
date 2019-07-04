package com.visy.socket.chat;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MyLoginAndChoose extends JFrame{
    private static final long serialVersionUID = 1L;

    private String myPwd="unlock@visy";

    private JLabel jlbPwd;
    private JPasswordField jpfPwd;
    private JButton jbtLogin;

    private JLabel jlbCopy;

    MyLoginAndChoose(){
        jlbPwd=new JLabel("口令:");
        jpfPwd=new JPasswordField(8);
        jpfPwd.setEchoChar('#');
        jbtLogin=new JButton("验证");
        jbtLogin.setBackground(Color.YELLOW);

        jlbCopy=new JLabel("Copyright@Visy",JLabel.CENTER);

        JPanel panelCenter=new JPanel(new FlowLayout());
        panelCenter.add(jlbPwd);
        panelCenter.add(jpfPwd);
        panelCenter.add(jbtLogin);

        JPanel panelCopy=new JPanel(new FlowLayout());
        panelCopy.add(jlbCopy);

        setLayout(new BorderLayout());
        add((new JPanel()).add(new JLabel(" ")),BorderLayout.NORTH);
        add(panelCenter,BorderLayout.CENTER);
        add(panelCopy,BorderLayout.SOUTH);


        setTitle("Login");
        setSize(400,140);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        //-----------------------------------------------

        jbtLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                char[] values = jpfPwd.getPassword();
                String password = new String(values);

                if(myPwd.equals(password)){
                    new ChooseFrame();
                    setVisible(false);
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "口令错误!");
                }
            }
        });
    }

    public static void main(String[] args) {
        new MyLoginAndChoose();
    }
}

class ChooseFrame extends JFrame{
    private static final long serialVersionUID = 1L;

    private JButton jbtServer;
    private JButton jbtClient;

    ChooseFrame(){
        jbtServer=new JButton("以服务端运行");
        jbtServer.setBackground(Color.YELLOW);
        jbtClient=new JButton("以客户端运行");
        jbtClient.setBackground(Color.YELLOW);

        JPanel panel=new JPanel(new FlowLayout());
        panel.add(jbtServer);
        panel.add(jbtClient);

        JPanel panelMain=new JPanel(new BorderLayout());
        panelMain.add((new JPanel()).add(new JLabel(" ")),BorderLayout.NORTH);
        panelMain.add(panel,BorderLayout.CENTER);
        panelMain.setBorder(new TitledBorder("选择运行方式"));

        add(panelMain);

        setTitle("Choose");
        setSize(400,140);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        //---------------------------------------------------

        jbtServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MyServer();
                setVisible(false);
            }
        });

        jbtClient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new MyClient();
                setVisible(false);
            }
        });

    }
}





