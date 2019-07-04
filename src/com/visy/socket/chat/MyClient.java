package com.visy.socket.chat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


//########################################本程序写于2014年10月31日#############################
public class MyClient extends JFrame{
	private static final long serialVersionUID = 1L;

	private static String ipSite="127.0.0.1";
	private static int port=8000;

	static String displayInfo="Visy：本程序的聊天记录是保存在内存变量中的,所以注意："+
			"\n1、退出后内容会丢失，重要信息请复制到其他地方保存\n2、"+
			"聊天记录过多时请及时点击“清除记录”清除，以免内存不足\n----------------------------"+
			"------------------------------------------------------\n\n";

	private static JTextArea jtaContent=new JTextArea("");
	static JButton jbtSend=new JButton("发送");
	private static JTextArea jtaFromServer=new JTextArea("");
	private JLabel jlbIp=new JLabel("对方IP:");
	private JLabel jlbPort=new JLabel("端口:");
	public static JLabel jlbIsCon;
	private JTextField jtfIp=new JTextField(8);
	private JTextField jtfPort=new JTextField(5);
	public static JButton jbtCon;
	private JButton jbtClear;

	static DataOutputStream outputToServer;
	static DataInputStream inputFromServer;
	MyClient() {
		//界面布局----------------------------------------
		JPanel panelTop=new JPanel(new FlowLayout());
		panelTop.add(jlbIp);
		jtfIp.setText(ipSite);panelTop.add(jtfIp);
		panelTop.add(jlbPort);
		jtfPort.setText(""+port);panelTop.add(jtfPort);
		jbtCon=new JButton("连接");jbtCon.setBackground(Color.green);panelTop.add(jbtCon);
		jlbIsCon=new JLabel("  [尚未连接]");jlbIsCon.setForeground(Color.RED);panelTop.add(jlbIsCon);
		jbtClear=new JButton(" 清除记录");jbtClear.setBackground(Color.YELLOW);panelTop.add(jbtClear);
		panelTop.setBorder(new LineBorder(Color.BLACK));

		JPanel panel1=new JPanel(new BorderLayout());
		panel1.add(jtaContent,BorderLayout.CENTER);
		jbtSend.setEnabled(false);
		jbtSend.setBackground(Color.YELLOW);jbtSend.setMnemonic(KeyEvent.VK_ENTER);panel1.add(jbtSend,BorderLayout.EAST);
		panel1.setBorder(new LineBorder(Color.BLACK));

		JPanel panel2=new JPanel(new BorderLayout());
		//把定义的JTextArea放到JScrollPane里面去
		JScrollPane scrollPanel = new JScrollPane(jtaFromServer);

		//分别设置水平和垂直滚动条自动出现
		scrollPanel.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel2.add(scrollPanel,BorderLayout.CENTER);
		panel2.setBorder(new TitledBorder("聊天记录"));

		JPanel panelDown=new JPanel(new BorderLayout());
		panelDown.add(panel1,BorderLayout.SOUTH);
		panelDown.add(panel2,BorderLayout.CENTER);

		add(panelTop,BorderLayout.NORTH);
		add(panelDown,BorderLayout.CENTER);

		setTitle("客户端");
		setSize(500,390);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		//--------------------------------------------
		//启动和服务端的连接
		jbtCon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ipSite=jtfIp.getText();
				port=Integer.parseInt(jtfPort.getText());
				connectServer();
			}
		});
		//发送消息
		jbtSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sendInfo=jtaContent.getText();
				String sendMsg="我："+sendInfo+"    "+getCurrentTime()+"\n";
				displayInfo=displayInfo+sendMsg;
				jtaFromServer.setText(displayInfo);
				openSendThread(sendInfo);
				jtaContent.setText("");
			}
		});
		//清屏和聊天数据
		jbtClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayInfo="";
				jtaFromServer.setText("");
			}
		});
	}
	//获取系统当前时间
	public static  String getCurrentTime(){
		Calendar ca = Calendar.getInstance();
		int y= ca.get(Calendar.YEAR);//获取年份
		int m=ca.get(Calendar.MONTH)+1;//获取月份
		int d=ca.get(Calendar.DATE);//获取日
		int h=ca.get(Calendar.HOUR_OF_DAY);//小时
		int min=ca.get(Calendar.MINUTE);//分
		int s=ca.get(Calendar.SECOND);//秒
		return "["+y+"/"+m+"/"+d+" "+h+":"+min+":"+s+"]";
	}

	public static void connectServer(){
		try {
			jlbIsCon.setForeground(Color.BLUE);
			jlbIsCon.setText("  [尝试连接]");

			//通过IP地址和端口号获得一个socket
			@SuppressWarnings("resource")
			Socket socket=new Socket(ipSite,port);
			jlbIsCon.setForeground(Color.GREEN);
			jlbIsCon.setText("  [连接成功]");
			jbtSend.setEnabled(true);
			jbtCon.setText("刷新");
			//System.out.println("客户端已启动###");

			//获得数据输入流
			inputFromServer=new DataInputStream(socket.getInputStream());

			//获得数据输出流
			outputToServer=new DataOutputStream(socket.getOutputStream());

			Runnable getRun=new GetMsgFromServer(inputFromServer,jtaFromServer);
			Thread getThread=new Thread(getRun);
			getThread.start();

		}catch (Exception e) {
			JOptionPane.showMessageDialog(null, "连接服务器失败!");
			jbtSend.setEnabled(false);
			jlbIsCon.setForeground(Color.RED);
			jlbIsCon.setText("  [连接失败]");
			jbtCon.setText("连接");
		}
	}

	public void openSendThread(String sendMsg){
		Runnable sendRun=new SendMsgToServer(outputToServer,sendMsg);
		Thread sendThread=new Thread(sendRun);
		sendThread.start();
	}

	//启动客户端窗口
	public static void main(String[] args) {
		new MyClient();
	}
}


//接收服务端信息的线程
class GetMsgFromServer implements Runnable{
	private DataInputStream in;
	private JTextArea jtaInput;

	public GetMsgFromServer(DataInputStream in,JTextArea jtaFromServer){
		this.in=in;
		this.jtaInput=jtaFromServer;
	}

	@Override
	public void run() {
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(in));
			String msg="";
			while ((msg = br.readLine()) != null) {
				String getMsg="对方："+msg+"    "+MyClient.getCurrentTime()+"\n";
				MyClient.displayInfo=MyClient.displayInfo+getMsg;
				jtaInput.setText(MyClient.displayInfo);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"连接断开!");
			MyClient.jbtSend.setEnabled(false);
			MyClient.jlbIsCon.setForeground(Color.RED);
			MyClient.jlbIsCon.setText("  [连接掉线]");
			MyClient.jbtCon.setText("连接");
		}
	}
}

//发送信息到服务端的线程
class SendMsgToServer implements Runnable{
	private DataOutputStream out;
	private String  sendMsg;

	public SendMsgToServer(DataOutputStream out,String sendMsg){
		this.out=out;
		this.sendMsg=sendMsg;
	}

	public void run() {
		PrintWriter pw=new PrintWriter(out, true);
		pw.println(sendMsg);
	}
}
