import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashMap;

/***
 *
 */
public class Router {

	static HashMap<String, String[]> ARP_TABLE = new HashMap<>();
	static String IP = "";
	static String Port = "";
	static String MAC = "";
	static byte[] IP_b = new byte[4];
	static byte[] MAC_b = new byte[6];
	static String[] IP_MAC_Port;
	static DatagramSocket Listen_socket;
	static DatagramSocket Send_socket;

	/***
	 *
	 * @param args,
	 *            command line argument ignored
	 */
	public static void main(String[] args) throws Exception {
		Listen_socket = new DatagramSocket();
		Send_socket = new DatagramSocket();
		IP_MAC_Port = new String[3];
		try {
			IP_MAC_Port[0] = InetAddress.getLocalHost().getHostAddress();
			IP_b = InetAddress.getLocalHost().getAddress();
			IP = IP_MAC_Port[0];
			IP_MAC_Port[1] = ArpPacket.arrayToHexString(
					NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(), ':');
			MAC = IP_MAC_Port[1];
			MAC_b = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			IP_MAC_Port[2] = Listen_socket.getLocalPort() + "";
			Port = IP_MAC_Port[2];
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		ARP_TABLE.put("192.168.1.1", IP_MAC_Port);
		System.out.println("Router initialized!\nIP, Port, MAC :" + IP_MAC_Port[0] + " " + IP_MAC_Port[2] + " "
				+ IP_MAC_Port[1]);
		ListeningThread listeningThread = new ListeningThread();
		new Thread(listeningThread).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (ARP_TABLE.size() > 0) {
						StringBuffer sb = new StringBuffer();
						System.out.println("ROUTING TABLE: -");
						for (String key : ARP_TABLE.keySet()) {
							String[] value = ARP_TABLE.get(key);
							sb.append(key + " -- " + value[0] + " " + value[1] + " " + value[2] + "\n");
						}
						System.out.println(sb.toString() + "\n");
					}
				}
			}
		}).start();
		
		Thread updater = new Thread(new Runnable() {

			DatagramSocket updater_socket = new DatagramSocket();

			/***
			 *
			 * @param text
			 */
			private void sendMessage(ArpPacket pkt, String IP, String Port) {
				byte[] byte_stream = ArpPacketAnalyzer.toBytes(pkt, 0);
				InetAddress inetAddress;
				try {
					inetAddress = InetAddress.getByName(IP);
					DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Integer.parseInt(Port));
					updater_socket.send(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (ARP_TABLE.size() > 0) {
						ArpPacket init_pkt = new ArpPacket(MAC_b, IP_b);
					}
				}
			}
		});
//		updater.start();
	}

	/***
	 *
	 */
	public static class ListeningThread implements Runnable {

		ListeningThread() {
			System.out.println("Listening for users..");
		}

		/***
		 *
		 * @param text
		 */
		private void sendMessage(ArpPacket pkt, String IP, String Port) {
			byte[] byte_stream = ArpPacketAnalyzer.toBytes(pkt, 0);
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(IP);
				DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Integer.parseInt(Port));
				Send_socket.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		/***
		 *
		 */
		@Override
		public void run() {
			try {
				while (true) {
					int ARP_Table_size = ARP_TABLE.size();
					String[] IP_MAC_Port = new String[3];
					byte[] bytes = new byte[1024];
					DatagramPacket p = new DatagramPacket(bytes, bytes.length);
					Listen_socket.receive(p);
					byte[] data = p.getData();
					if (data[0] == 0) {						
						ArpPacket Arp_pkt = ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
						IP_MAC_Port[0] = ArpPacket.arrayToDecimalString(Arp_pkt.SPA);
						System.out.println("Received initialization msg from: " + IP_MAC_Port[0]);
						IP_MAC_Port[1] = ArpPacket.arrayToHexString(Arp_pkt.SHA, ':');
						IP_MAC_Port[2] = p.getPort() + "";
						ARP_TABLE.put("192.168.1." + (ARP_Table_size + 1), IP_MAC_Port);
						StringBuffer sb = new StringBuffer();
						System.out.println("Routing table updated..");
						for (String key : ARP_TABLE.keySet()) {
							String[] value = ARP_TABLE.get(key);
							sb.append(key + " -- " + value[0] + " " + value[1] + " " + value[2] + "\n");
						}
						System.out.println(sb.toString() + "\n");
						ArpPacket response = new ArpPacket();
						response.THA = MAC_b;
						response.TPA = IP_b;
						sendMessage(response, IP_MAC_Port[0], IP_MAC_Port[2]);
					} else if (data[0] == 1) {
						ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
					} else {

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
