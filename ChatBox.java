import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.*;
import java.util.Objects;

/***
 * UI based chatting application, used by user to chat with desired person using the logical IP that is assigned to it
 * @author Arjun Dhuliya
 */
class ChatBox {
    private final Object LOCK = new Object();
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
    private DatagramSocket sendingSocket;


    /***
     * Constructor
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
     * updates text filed appending text as new line
     * @param allMessageText, text to append
     */
    private void updateMessagesText(String allMessageText) {
        synchronized (LOCK) {
            this.allMessageText += allMessageText + "\n";
            setListText(this.allMessageText);
        }
    }

    /***
     * set messages
     * @param allMessageText, message text
     */
    private void setListText(String allMessageText) {
        synchronized (LOCK) {
            this.list.setText(allMessageText);
        }
    }

    /***
     * init the GUI elements
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
     * sets buttons and event handlers
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

        JLabel ipAndPort = new JLabel("IP");
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
     * send text message
     * @param text, text message
     */
    private void sendMessage(String text) {
        byte[] bytes;
        // text.getBytes(0, text.length(), bytes, 1);
        bytes = text.getBytes();
        byte[] toSend = new byte[bytes.length + 5];
        toSend[0] = 3;
        System.arraycopy(ipToBytes(ip), 0, toSend, 1, 4);
        System.arraycopy(bytes, 0, toSend, 5, bytes.length);
        InetAddress inetAddress;
        try {
            User.Router_Info router_info = User.default_gateway.get("192.168.1.1");
            inetAddress = InetAddress.getByName(router_info.Router_IP);
            DatagramPacket p = new DatagramPacket(toSend, toSend.length, inetAddress, router_info.Router_Port);
            sendingSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Converts bytes to ip
     * @param data, byte of ip
     * @return string ip
     */
    private String bytesToIp(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (data[i] > 0) sb.append(data[i]);
            else sb.append(data[i] + 256);
            sb.append('.');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /***
     * converts ip to bytes
     * @param ip, string ip
     * @return byte array ip
     */
    private byte[] ipToBytes(String ip) {
        String[] splits = ip.split("\\.");
        byte[] data = new byte[splits.length];
        for (int i = 0; i < splits.length; i++) {
            int v = Integer.parseInt(splits[i]);
            if (v <= 127)
                data[i] = (byte) v;
            else
                data[i] = (byte) (v - 256);

        }
        return data;
    }

    /***
     * button listener
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
//				port = Integer.parseInt(split[1]);
                    statusLabel.setText("Updated the ip,port:" + ip);
                    break;
            }
        }
    }

    /***
     * User listening thread enables user to listen for packets
     */
    private class UserListeningThread implements Runnable {
        final byte[] bytes;
        DatagramPacket p;
        DatagramSocket socket;

        /***
         * Constructor
         */
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
         * 0:initial_msg 1:send_arp_msg 2:send_usr_msg
         *
         * @param socket, socket
         * @throws Exception, IO Exception
         */
        void send_arp_init_msg(DatagramSocket socket) throws Exception {
            String router_IP = User.default_gateway.get("192.168.1.1").Router_IP;
            int router_Port = User.default_gateway.get("192.168.1.1").Router_Port;
            InetAddress inetAddress = InetAddress.getByName(router_IP);
            ArpPacket init_pkt = new ArpPacket(
                    NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
                    InetAddress.getLocalHost().getAddress());
            init_pkt.PortNumber = socket.getLocalPort();
            byte[] byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 0);
            DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, router_Port);
            socket.send(p);
            socket.receive(p);
            ArpPacket pkt = ArpPacketAnalyzer.analyzePacket(p.getData());
            ipText.setText(bytesToIp(pkt.SLPA) + "");

            User.Router_Info router_info = User.default_gateway.get("192.168.1.1");
            router_info.Router_Port = pkt.PortNumber;
            router_info.Router_IP = ArpPacket.arrayToDecimalString(pkt.TPA);
            router_info.Router_Mac = ArpPacket.arrayToHexString(pkt.THA, ':');
            System.out.println("Received:\n" + pkt);
        }

        /***
         * run method of thread
         */
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
                    if (p.getData()[0] == 0) {
                        ArpPacket pkt = ArpPacketAnalyzer.analyzePacket(p.getData());
                        User.Router_Info router_info = User.default_gateway.get("192.168.1.1");
                        router_info.Router_Port = pkt.PortNumber;
                        router_info.Router_IP = ArpPacket.arrayToDecimalString(pkt.TPA);
                        router_info.Router_Mac = ArpPacket.arrayToHexString(pkt.THA, ':');
                        System.out.println("Received:\n" + pkt);
                    } else {
                        String msg;
//                        msg = bytesToIp(p.getData(), 1) + ": ";
                        msg = "Friend: ";
                        msg += new String(p.getData(), 5, p.getLength() - 5);
                        updateMessagesText(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
