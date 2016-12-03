import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

/***
 *
 */
public class Router {

	static HashMap<String, String[]> ARP_TABLE = new HashMap<>();

	/***
	 *
	 * @param args,
	 *            command line argument ignored
	 */
	public static void main(String[] args) throws Exception {
		ListeningThread listeningThread = new ListeningThread();
		new Thread(listeningThread).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (ARP_TABLE.size() > 0) {
						StringBuffer sb = new StringBuffer();
						System.out.println("ROUTING TABLE: -");
						for (String key : ARP_TABLE.keySet()) {
							sb.append(key + " -- " + ARP_TABLE.get(key) + "\n");
						}
						System.out.println(sb.toString() + "\n");
					}
				}
			}
		}).start();
	}

	/***
	 *
	 */
	public static class ListeningThread implements Runnable {
		private DatagramSocket socket;

		ListeningThread() {
			try {
				socket = new DatagramSocket();
				String[] IP_MAC_Port = new String[3];
				try {
					IP_MAC_Port[0] = InetAddress.getLocalHost().getHostAddress();
					IP_MAC_Port[1] = ArpPacket.arrayToHexString(
							NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(), ':');
					IP_MAC_Port[2] = socket.getLocalPort() + "";
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ARP_TABLE.put("192.168.1.1", IP_MAC_Port);
				System.out.println("Router initialized!\nIP, Port, MAC :" + IP_MAC_Port[0] + " " + IP_MAC_Port[2] + " " + IP_MAC_Port[1]);
			} catch (SocketException e) {
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
					socket.receive(p);
					System.out.println("Received initialization msg from: " + p.getAddress());
					byte[] data = p.getData();
					if (data[0] == 0) {
						ArpPacket Arp_pkt = ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
						IP_MAC_Port[0] = ArpPacket.arrayToDecimalString(Arp_pkt.SPA);
						IP_MAC_Port[1] = ArpPacket.arrayToHexString(Arp_pkt.SHA, ':');
						IP_MAC_Port[2] = p.getPort() + "";
						ARP_TABLE.put("192.168.1." + (ARP_Table_size + 1), IP_MAC_Port);
						StringBuffer sb = new StringBuffer();
						for (String key : ARP_TABLE.keySet()) {
							sb.append(key + " -- " + ARP_TABLE.get(key) + "\n");
						}
						System.out.println(sb.toString() + "\n");
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
