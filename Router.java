import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedHashMap;

/***
 * This is the router class which runs at the router and implements ARP
 */
public class Router {

	static LinkedHashMap<String, String[]> ARP_TABLE = new LinkedHashMap<>();
	static LinkedHashMap<String, String> ARP_IPs = new LinkedHashMap<>();
	static String IP = "";
	static String Port = "";
	static String MAC = "";
	static byte[] IP_b = new byte[4];
	static byte[] MAC_b = new byte[6];
	static String[] IP_MAC_Port;
	static DatagramSocket Listen_socket;
	static DatagramSocket Send_socket;

	/***
	 * This is the main function of the class
	 * 
	 * @param args,
	 *            command line arguments ignored
	 */
	public static void main(String[] args) throws Exception {
		Listen_socket = new DatagramSocket(7777);
		Send_socket = new DatagramSocket();
		IP_MAC_Port = new String[3];
		System.out.println("Initializing Router!");
		try {
			MAC_b = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			IP_MAC_Port[0] = InetAddress.getLocalHost().getHostAddress();
			IP_MAC_Port[1] = ArpPacket.arrayToHexString(MAC_b, ':');
			IP_MAC_Port[2] = Listen_socket.getLocalPort() + "";
			System.out.println("\nIP, Port, MAC :" + IP_MAC_Port[0] + " " + IP_MAC_Port[2] + " " + IP_MAC_Port[1]);
			IP_b = InetAddress.getLocalHost().getAddress();
			IP = IP_MAC_Port[0];
			MAC = IP_MAC_Port[1];
			Port = IP_MAC_Port[2];
			System.out.println("Initialized");
		} catch (UnknownHostException e) {
			Listen_socket.close();
			e.printStackTrace();
		}
		ARP_TABLE.put("192.168.1.1", IP_MAC_Port);
		ARP_IPs.put(IP_MAC_Port[0], "192.168.1.1");
		Communicator router = new Communicator();
		new Thread(router).start();
		new Thread(new Runnable() {// Routing table printer

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (ARP_TABLE.size() > 1) {
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
					DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress,
							Integer.parseInt(Port));
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
		// updater.start();
	}

	/***
	 * This is the Listening
	 */
	public static class Communicator implements Runnable {

		Communicator() {
			System.out.println("Listening for users..");
			// try {
			// Thread.sleep(5000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		}

		/***
		 *
		 * @param text
		 */
		private void sendMessage(ArpPacket pkt, String IP, String Port, int mode) {
			byte[] byte_stream = ArpPacketAnalyzer.toBytes(pkt, mode);
			InetAddress inetAddress;
			try {
				inetAddress = InetAddress.getByName(IP);
				DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress,
						Integer.parseInt(Port));
				Send_socket.send(p);
				System.out.println("ARP packet sent to " + IP + " " + Port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private byte[] to_bytes(String IP, int length) {
			int ind = 0;
			byte[] b_arr = new byte[length];
			String split = "";
			split = (length == 4) ? "\\." : ":";
			int radix = (length == 4) ? 10 : 16;
			for (String i : IP.split(split)) {
				b_arr[ind] = (byte) Integer.parseInt(i, radix);
				// System.out.println(Integer.parseInt(i, radix));
				ind++;
			}
			return b_arr;
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
					if (data[0] >= 0 && data[0] < 3) {
						ArpPacket Arp_pkt = ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
						IP_MAC_Port[0] = ArpPacket.arrayToDecimalString(Arp_pkt.SPA);
						System.out.println("Received initialization msg from: " + IP_MAC_Port[0]);
						IP_MAC_Port[1] = ArpPacket.arrayToHexString(Arp_pkt.SHA, ':');
						IP_MAC_Port[2] = p.getPort() + "";
						String SLPA = ArpPacket.arrayToDecimalString(Arp_pkt.SLPA);
						if (data[0] == 0) {// ARP_msg request
							if (ARP_IPs.get(IP_MAC_Port[0]) == null) {
								String k = "192.168.1." + (ARP_Table_size + 1);
								ARP_TABLE.put(k, IP_MAC_Port);
								ARP_IPs.put(IP_MAC_Port[0], k);
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
								response.PortNumber = Integer.parseInt(Port);
								sendMessage(response, IP_MAC_Port[0], IP_MAC_Port[2], 0);
							} else {
								String local_IP = ARP_IPs.get(IP_MAC_Port[0]);
								ARP_TABLE.put(local_IP, IP_MAC_Port);
							}
						} else if (data[0] == 1) {// Received ARP_table update
													// message
							System.out.println("Received update request!");
							ARP_TABLE.put(SLPA, IP_MAC_Port);
							System.out.println("Table updated");
							System.out.println(ARP_IPs.toString());

						} else if (data[0] == 2) {// Sending routing table
							System.out.println("Sending routing table");
							byte[] b = new byte[1];
							b[0] = (byte) ARP_TABLE.size();
							InetAddress inetAddress;
							try {
								inetAddress = InetAddress.getByName(IP_MAC_Port[0]);
								Send_socket.send(
										new DatagramPacket(b, b.length, inetAddress, Integer.parseInt(IP_MAC_Port[2])));
							} catch (Exception e) {
								System.out.println(e);
							}
							System.out.println("Sent size");
							for (String key : ARP_TABLE.keySet()) {
								String[] value = ARP_TABLE.get(key);
								int PortNumber = Integer.parseInt(value[2]);
								ArpPacket response = new ArpPacket();
								response.SLPA = to_bytes(key, 4);
								response.TPA = to_bytes(value[0], 4);
								response.THA = to_bytes(value[1], 6);
								response.PortNumber = PortNumber;
								response.operation = 0;
								System.out.println("Sent entry " + key + " to " + IP_MAC_Port[0]);
								sendMessage(response, IP_MAC_Port[0], IP_MAC_Port[2], 2);
							}
						}
					} else if (data[0] == 3) { // 0th byte for mode, 1-4 for
												// receiver IP
						String receiver_Local_IP = (int) data[1] + ".";
						receiver_Local_IP = (int) data[2] + ".";
						receiver_Local_IP = (int) data[3] + ".";
						receiver_Local_IP = (int) data[4] + "";
						System.out.println("Received msg for " + receiver_Local_IP);
						String[] entry = ARP_TABLE.get(receiver_Local_IP);
						byte[] data_for_dest = new byte[data.length - 5];
						for (int i = 5; i < data_for_dest.length; i++) {
							data_for_dest[i - 5] = data[i];
						}
						try {
							InetAddress Dest_IP = InetAddress.getByName(entry[0]);
							Send_socket.send(new DatagramPacket(data_for_dest, data_for_dest.length, Dest_IP,
									Integer.parseInt(entry[2])));
						} catch (Exception e) {
							System.out.println(e);
						}
						System.out.println("Sent msg to " + receiver_Local_IP);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
