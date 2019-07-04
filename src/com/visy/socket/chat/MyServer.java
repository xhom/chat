package  com.visy.socket.chat;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.Calendar;
//########################################本程序写于2014年10月31日#############################
public class MyServer extends JFrame{
	private static final long serialVersionUID = 1L;

	private static int port=8000;//默认端口

	static String displayInfo="Visy：本程序的聊天记录是保存在内存变量中的,所以注意："+
			"\n1、退出后内容会丢失，重要信息请复制到其他地方保存\n2、"+
			"聊天记录过多时请及时点击“清除记录”清除，以免内存不足\n----------------------------"+
			"------------------------------------------------------\n\n";
	private static JTextArea jtaContent=new JTextArea("");
	public static JButton jbtSend=new JButton("发送");
	private static JTextArea jtaChatRecord=new JTextArea("");
	private JLabel jlbHostIp=new JLabel("本机IP:");
	private JLabel jlbOpenPort=new JLabel("开放端口:");
	public static JLabel jlbIsStart;
	private JLabel jtfIp=new JLabel();
	private JTextField jtfPort=new JTextField(5);
	private static JButton jbtStart;
	private JButton jbtClear;

	static DataOutputStream outToClient;
	static DataInputStream inputFromClient;

	static PrintWriter pw;

	MyServer(){
		//界面布局----------------------------------------
		JPanel panelTop=new JPanel(new FlowLayout());
		panelTop.add(jlbHostIp);
		String hostIp;
		try {
			hostIp = InetAddress.getLocalHost().getHostAddress().toString();//本机IP
			jtfIp.setText(hostIp);
		} catch (UnknownHostException e) {
			jtfIp.setText("获取失败");
		}
		jtfIp.setForeground(Color.orange);
		panelTop.add(jtfIp);
		panelTop.add(jlbOpenPort);
		jtfPort.setText(""+port);jtfPort.setForeground(Color.orange);panelTop.add(jtfPort);
		jbtStart=new JButton("启动");jbtStart.setBackground(Color.green);panelTop.add(jbtStart);
		jlbIsStart=new JLabel("[尚未启动]");jlbIsStart.setForeground(Color.RED);panelTop.add(jlbIsStart);
		jbtClear=new JButton(" 清除记录");jbtClear.setBackground(Color.YELLOW);panelTop.add(jbtClear);
		panelTop.setBorder(new LineBorder(Color.BLACK));

		JPanel panel1=new JPanel(new BorderLayout());
		panel1.add(jtaContent,BorderLayout.CENTER);
		jbtSend.setEnabled(false);
		jbtSend.setBackground(Color.YELLOW);jbtSend.setMnemonic(KeyEvent.VK_ENTER);panel1.add(jbtSend,BorderLayout.EAST);
		panel1.setBorder(new LineBorder(Color.BLACK));

		JPanel panel2=new JPanel(new BorderLayout());
		//把定义的JTextArea放到JScrollPane里面去
		JScrollPane scrollPanel = new JScrollPane(jtaChatRecord);

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

		setTitle("服务端");
		setSize(500,390);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		//--------------------------------------------


		//启动监听服务
		jbtStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				port=Integer.parseInt(jtfPort.getText());//获取自定义端口号
				//由于服务线程中设计死循环，所以单独做一个线程处理
				if(port<1024){
					JOptionPane.showMessageDialog(null,"1~1024是系统预留端口，请选择1025~65535号端口！");
				}else{
					Runnable serverRun=new StartServer(jlbIsStart,port,inputFromClient,outToClient,jtaChatRecord);
					Thread serverThread=new Thread(serverRun);
					serverThread.start();
				}
			}
		});
		//发送消息事件
		jbtSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sendInfo=jtaContent.getText();//获取输入
				String sendMsg="我："+sendInfo+"    "+getCurrentTime()+"\n";//附加信息
				displayInfo=displayInfo+sendMsg;//拼接信息，用变量存储聊天记录
				jtaChatRecord.setText(displayInfo);//在窗口中显示聊天记录
				openSendThread(sendInfo);
				jtaContent.setText("");//清空输入框
			}
		});
		//清空变量的数据和窗口显示
		jbtClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayInfo="";
				jtaChatRecord.setText("");
			}
		});
	}

	//获取系统当前时间
	public static String getCurrentTime(){
		Calendar ca = Calendar.getInstance();
		int y= ca.get(Calendar.YEAR);//获取年份
		int m=ca.get(Calendar.MONTH)+1;//获取月份
		int d=ca.get(Calendar.DATE);//获取日
		int h=ca.get(Calendar.HOUR_OF_DAY);//小时
		int min=ca.get(Calendar.MINUTE);//分
		int s=ca.get(Calendar.SECOND);//秒
		return "["+y+"/"+m+"/"+d+" "+h+":"+min+":"+s+"]";
	}

	//启动发送消息的线程
	public static void openSendThread(String sendMsg){
		Runnable sendRun=new sendMsgToClient(sendMsg);
		Thread sendThread=new Thread(sendRun);
		sendThread.start();
	}

	//启动窗口程序
	public static void main(String[] args) {
		new MyServer();
	}
}


//用于接收信息的线程
class GetMsgFromClient implements Runnable{
	private DataInputStream in;
	private JTextArea jtaChatRecord;

	public GetMsgFromClient(DataInputStream in,JTextArea jtaChatRecord){
		this.in=in;
		this.jtaChatRecord=jtaChatRecord;
	}
	@Override
	public void run() {
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(in));
			String msg = "";
			while ((msg = br.readLine()) != null) {
				String getMsg="对方："+msg+"    "+MyServer.getCurrentTime()+"\n";
				MyServer.displayInfo=MyServer.displayInfo+getMsg;
				jtaChatRecord.setText(MyServer.displayInfo);
			}
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(null,"连接断开!");
			MyServer.jlbIsStart.setForeground(Color.RED);
			MyServer.jlbIsStart.setText("[连接断开]");
			MyServer.jbtSend.setEnabled(false);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
//用于发送信息的线程
class sendMsgToClient implements Runnable{
	//DataOutputStream out;
	String  sendMsg;

	public sendMsgToClient(String sendMsg){
		//this.out=out;
		this.sendMsg=sendMsg;
	}
	@Override
	public void run() {
		//System.out.println("S"+sendMsg);
		MyServer.pw.println(sendMsg);
		MyServer.pw.flush();
	}
}

//监听线程，避免阻死UI线程，所以这里单独作为一个线程
class StartServer implements Runnable{
	JLabel jlbIsStart;
	int port;
	DataInputStream inputFromClient;
	DataOutputStream outToClient;
	JTextArea jtaChatRecord;

	StartServer(JLabel jlbIsStart,int port,DataInputStream inputFromClient,
				DataOutputStream outToClient,JTextArea jtaChatRecord){
		this.jlbIsStart=jlbIsStart;
		this.port=port;
		this.inputFromClient=inputFromClient;
		this.outToClient=outToClient;
		this.jtaChatRecord=jtaChatRecord;
	}

	@Override
	public void run() {
		try {

			jlbIsStart.setForeground(Color.BLUE);
			jlbIsStart.setText("[正在启动]");
			@SuppressWarnings("resource")
			ServerSocket ssocket=new ServerSocket(port);
			jlbIsStart.setForeground(Color.BLACK);
			jlbIsStart.setText("[等待连接]");

			while(true){
				Socket socket=ssocket.accept();
				jlbIsStart.setForeground(Color.GREEN);
				jlbIsStart.setText("[连接成功]");
				MyServer.jbtSend.setEnabled(true);

				//获得数据输入流
				inputFromClient=new DataInputStream(socket.getInputStream());

				//获得数据输出流
				outToClient=new DataOutputStream(socket.getOutputStream());
				MyServer.pw=new PrintWriter(outToClient);

				Runnable getRun=new GetMsgFromClient(inputFromClient,jtaChatRecord);
				Thread getThread=new Thread(getRun);
				getThread.start();

			}

		}catch (BindException e) {
			JOptionPane.showMessageDialog(null,"端口被占用!请换个端口试试");
			jlbIsStart.setText("[端口占用]");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}