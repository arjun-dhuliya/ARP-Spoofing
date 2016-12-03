import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

/***
 *
 */
public class ChatBox {
	private final Object LOCK = new Object();
	private String Router_IP;
	private int Router_Port;
	private String allMessageText;
	private JFrame mainFrame;
	private JLabel headerLabel;
	private JLabel statusLabel;
	private JPanel controlPanel;
	private JTextArea textArea;
	private JTextArea list;
	private JPanel ipPanel;
	private JTextField ipText;
	private String ip;
	private int port;
	private DatagramSocket sendingSocket;


	/***
	 *
	 */
	ChatBox() {
		allMessageText = "";
		initGUI();
		setButtonsAndEvents();
		try {
			sendingSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		new Thread(new UserListeningThread()).start();
	}

	/***
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		// new ChatBox();
	}

	/***
	 *
	 * @param allMessageText
	 */
	private void updateMessagesText(String allMessageText) {
		synchronized (LOCK) {
			this.allMessageText += allMessageText;
			setListText(this.allMessageText);
		}
	}

	/***
	 *
	 * @param allMessageText
	 */
	private void setListText(String allMessageText) {
		synchronized (LOCK) {
			this.list.setText(allMessageText);
		}
	}

	/***
	 *
	 */
	private void initGUI() {
		mainFrame = new JFrame("Chat Box");
		mainFrame.setSize(400, 400);
		GridLayout gridLayout = new GridLayout(7, 1);
		gridLayout.setVgap(2);
		mainFrame.setLayout(gridLayout);

		headerLabel = new JLabel("", JLabel.CENTER);
		JLabel gapLabel = new JLabel("", JLabel.CENTER);
		statusLabel = new JLabel("", JLabel.CENTER);
		textArea = new JTextArea("Sample Text");
		list = new JTextArea();
		JScrollPane scroll = new JScrollPane(list);
		DefaultCaret caret = (DefaultCaret) list.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		gapLabel.setSize(320, 10);
		list.setSize(320, 400);
		textArea.setSize(320, 60);
		statusLabel.setSize(350, 100);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		ipPanel = new JPanel();
		ipPanel.setLayout(new FlowLayout());
		// ipPanel.setLayout(new GridLayout(1,3));

		mainFrame.add(ipPanel);
		mainFrame.add(headerLabel);
		mainFrame.add(scroll);
		mainFrame.add(gapLabel);
		mainFrame.add(textArea);
		mainFrame.add(controlPanel);
		mainFrame.add(statusLabel);
		mainFrame.setVisible(true);
	}

	/***
	 *
	 */
	private void setButtonsAndEvents() {
		headerLabel.setText("Messages");

		JButton sendButton = new JButton("Send");
		JButton refreshButton = new JButton("Refresh");

		sendButton.setActionCommand("send");
		refreshButton.setActionCommand("refresh");

		sendButton.addActionListener(new ButtonClickListener());
		refreshButton.addActionListener(new ButtonClickListener());
		controlPanel.add(sendButton);
		controlPanel.add(refreshButton);

		JLabel ipAndPort = new JLabel("IP:Port");
		ipText = new JTextField();
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		ipText.setText(ip + ":8888   ");
		ipText.setSize(100, 20);
		JButton updateIpPort = new JButton("Update");
		updateIpPort.setActionCommand("updateIp");
		updateIpPort.addActionListener(new ButtonClickListener());

		ipPanel.add(ipAndPort);
		ipPanel.add(ipText);
		ipPanel.add(updateIpPort);

		mainFrame.setVisible(true);
	}

	/***
	 *
	 * @param text
	 */
	private void sendMessage(String text) {
		byte[] bytes = new byte[text.length() + 1];
		// text.getBytes(0, text.length(), bytes, 1);
		bytes = text.getBytes();
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(ip);
			DatagramPacket p = new DatagramPacket(bytes, bytes.length, inetAddress, port);
			sendingSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 *
	 */
	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			switch (command) {
			case "send":
				if (ip != null && !Objects.equals(ip, "")) {
					if (textArea.getText().length() > 0) {
						statusLabel.setText("Sent Message: " + textArea.getText());
						updateMessagesText("\nyou:" + textArea.getText());
						sendMessage(textArea.getText());
						textArea.setText("");
					} else {
						statusLabel.setText("Type Something before hitting send");
					}
				} else {
					statusLabel.setText("Update the ip and port of the receiver");
				}
				break;
			case "refresh":
				setListText(allMessageText);
				textArea.setText("");
				break;
			case "updateIp":
				String ipPort = ipText.getText().trim();
				String[] split = ipPort.split(":");
				ip = split[0];
				port = Integer.parseInt(split[1]);
				statusLabel.setText("Updated the ip,port:" + ip + "," + port);
				break;
			}
		}
	}

	private class UserListeningThread implements Runnable {
		byte[] bytes;
		DatagramPacket p;
		DatagramSocket socket;

		UserListeningThread() {
			bytes = new byte[1024];
			p = new DatagramPacket(bytes, 0, bytes.length);
			try {
				socket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}

		/***
		 *
		 * @param text
		 */
		private void sendMessage(String text) {
			byte[] bytes = new byte[text.length() + 1];
			bytes = text.getBytes();
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(ip);
				DatagramPacket p = new DatagramPacket(bytes, bytes.length, inetAddress, port);
				sendingSocket.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/***
		 * 0:initial_msg 1:send_arp_msg 2:send_usr_msg
		 * 
		 * @param arp_init
		 * @throws Exception
		 */
		public void send_arp_init_msg(DatagramSocket arp_init) throws Exception {
			Router_IP = User.default_gateway.get("192.168.1.1").Router_IP;
			Router_Port = User.default_gateway.get("192.168.1.1").Router_Port;
			InetAddress inetAddress = InetAddress.getByName(Router_IP);
			ArpPacket init_pkt = new ArpPacket(
					NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
					InetAddress.getLocalHost().getAddress());
			byte[] byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 0);
			DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Router_Port);
			sendingSocket.send(p);
		}

		@Override
		public void run() {
			try {
				String s = "User Listening at Ip, Port :" + InetAddress.getLocalHost().getHostAddress() + ", "
						+ socket.getLocalPort();
				System.out.println(s);
				statusLabel.setText(s);
				try {
					send_arp_init_msg(socket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (true) {
					p = new DatagramPacket(bytes, 0, bytes.length);
					socket.receive(p);
					String msg = "\nfriend:";
					msg += new String(p.getData(), 0, p.getLength());
					updateMessagesText(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
