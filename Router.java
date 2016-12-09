import java.io.IOException;
import java.net.*;
import java.util.LinkedHashMap;

/***
 * This is the router class which runs at the router and implements ARP
 */
public class Router {

    private static final LinkedHashMap<String, String[]> ARP_TABLE = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> ARP_IPs = new LinkedHashMap<>();
    private static String Port = "";
    private static byte[] IP_b = new byte[4];
    private static byte[] MAC_b = new byte[6];
    private static DatagramSocket Listen_socket;
    private static DatagramSocket Send_socket;

    /***
     * This is the main function of the class
     *
     * @param args,
     *            command line arguments ignored
     */
    public static void main(String[] args) throws Exception {
        Listen_socket = new DatagramSocket(7777);
        Send_socket = new DatagramSocket();
        String[] IP_MAC_Port = new String[3];
        System.out.println("Initializing Router..");
        try {
            MAC_b = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            IP_MAC_Port[0] = InetAddress.getLocalHost().getHostAddress();
            IP_MAC_Port[1] = ArpPacket.arrayToHexString(MAC_b, ':');
            IP_MAC_Port[2] = Listen_socket.getLocalPort() + "";
            System.out.println("IP, Port, MAC :" + IP_MAC_Port[0] + " " + IP_MAC_Port[2] + " " + IP_MAC_Port[1]);
            IP_b = InetAddress.getLocalHost().getAddress();
            Port = IP_MAC_Port[2];
            System.out.println("Router Ready!");
        } catch (UnknownHostException e) {
            Listen_socket.close();
            e.printStackTrace();
        }
        ARP_TABLE.put("192.168.1.1", IP_MAC_Port);
        ARP_IPs.put(IP_MAC_Port[0], "192.168.1.1");
        Communicator router = new Communicator();
        new Thread(router).start();
        new Thread(new Runnable() {// ARP CACHE printer

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ARP_TABLE.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        System.out.println("ARP CACHE: -");
                        for (String key : ARP_TABLE.keySet()) {
                            String[] value = ARP_TABLE.get(key);
                            sb.append(key).append(" -- ").append(value[0]).append(" ").append(value[1]).append(" ").append(value[2]).append("\n");
                        }
                        System.out.println(sb.toString() + "\n");
                    }
                }
            }
        }).start();

        Thread updater = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // updater.start();
    }

    /***
     * This is the Communicator thread which communicates with all
     */
    public static class Communicator implements Runnable {

        /***
         *  Constructor
         */
        Communicator() {
            System.out.println("Listening for users..");
            StringBuilder sb = new StringBuilder();
            System.out.println("ARP CACHE: -");
            for (String key : ARP_TABLE.keySet()) {
                String[] value = ARP_TABLE.get(key);
                sb.append(key).append(" -- ").append(value[0]).append(" ").append(value[1]).append(" ").append(value[2]).append("\n");
            }
            System.out.println(sb.toString() + "\n");
            // try {
            // Thread.sleep(5000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
        }

        /***
         * sends Arp message
         * @param pkt, ArpPacket to send
         * @param IP, Ip to send
         * @param Port, port number
         * @param mode, mode in which to send
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

        /***
         * converts ip to bytes
         * @param IP, string ip
         * @return byte array ip
         */
        private byte[] to_bytes(String IP, int length) {
            int ind = 0;
            byte[] b_arr = new byte[length];
            String split;
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
                        ArpPacket Arp_pkt = ArpPacketAnalyzer.analyzePacket(bytes);
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
                                StringBuilder sb = new StringBuilder();
                                System.out.println("ARP CACHE updated..");
                                for (String key : ARP_TABLE.keySet()) {
                                    String[] value = ARP_TABLE.get(key);
                                    sb.append(key).append(" -- ").append(value[0]).append(" ").append(value[1]).append(" ").append(value[2]).append("\n");
                                }
                                System.out.println(sb.toString() + "\n");
                                ArpPacket response = new ArpPacket();
                                response.THA = MAC_b;
                                response.TPA = IP_b;
                                response.SLPA = to_bytes(k, 4);
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
                            System.out.println("Cache updated");
                            System.out.println(ARP_IPs.toString());

                        } else if (data[0] == 2) {// Sending routing table
                            System.out.println("Sending ARP cache");
                            byte[] b = new byte[1];
                            b[0] = (byte) ARP_TABLE.size();
                            InetAddress inetAddress;
                            try {
                                inetAddress = InetAddress.getByName(IP_MAC_Port[0]);
                                Send_socket.send(
                                        new DatagramPacket(b, b.length, inetAddress, Integer.parseInt(IP_MAC_Port[2])));
                            } catch (Exception e) {
                                e.printStackTrace();
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
                        String sender = p.getAddress().getHostAddress();
                        String sender_IP = ARP_IPs.get(sender);
                        String receiver_Local_IP = (data[1] & 0xFF) + ".";
                        receiver_Local_IP += (data[2] & 0xFF) + ".";
                        receiver_Local_IP += (data[3] & 0xFF) + ".";
                        receiver_Local_IP += (data[4] & 0xFF) + "";
                        int ind = 1;
                        for (byte b : to_bytes(sender_IP, 4)) {
                            data[ind] = b;
                            ind++;
                        }
                        System.out.println("Received msg for " + receiver_Local_IP);
                        String[] entry = ARP_TABLE.get(receiver_Local_IP);
                        byte[] data_for_dest = new byte[data.length - 5];
                        System.arraycopy(data, 5, data_for_dest, 0, data_for_dest.length - 5);
                        try {
                            InetAddress Dest_IP = InetAddress.getByName(entry[0]);
                            Send_socket.send(new DatagramPacket(data, data.length, Dest_IP,
                                    Integer.parseInt(entry[2])));
                        } catch (Exception e) {
                            System.out.println(entry[0]);
                            e.printStackTrace();
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
