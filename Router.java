import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

/***
 *
 */
public class Router {

	static final HashMap<String, String> ARP_TABLE = new HashMap<>();

	/***
	 *
	 * @param args,
	 *            command line argument ignored
	 */
	public static void main(String[] args) {
		ListeningThread listeningThread = new ListeningThread();
		new Thread(listeningThread).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
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
				System.out.println("Router Listening at Ip, Port :" + InetAddress.getLocalHost().getHostAddress() + ", "
						+ socket.getLocalPort());
				while (true) {
					byte[] bytes = new byte[1024];
					DatagramPacket p = new DatagramPacket(bytes, bytes.length);
					socket.receive(p);
					System.out.println("Received initialization msg from: " + p.getAddress());
					byte[] data = p.getData();
					if (data[0] == 0) {
						ArpPacket Arp_pkt = ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
						ARP_TABLE.put(ArpPacket.arrayToDecimalString(Arp_pkt.SPA), ArpPacket.arrayToHexString(Arp_pkt.SHA, ':'));
						StringBuffer sb = new StringBuffer();
						for (String key : ARP_TABLE.keySet()) {
							sb.append( key + " -- " + ARP_TABLE.get(key) + "\n");
						}
						System.out.println(sb.toString() + "\n");
					} else if (data[0] == 1) {
						ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
					} else {
						forwardMessage(p.getData(), p.getLength());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/***
		 *
		 * @param data
		 * @param length
		 */
		private void forwardMessage(byte[] data, int length) {

		}
	}

}
