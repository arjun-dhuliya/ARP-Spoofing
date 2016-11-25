import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/***
 *
 */
public class ChatBox {
    String messages;
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JTextArea textArea;
    private JTextArea list;

    /***
     *
     */
    public ChatBox() {
        messages = "";
        initGUI();
    }

    /***
     *
     * @param args
     */
    public static void main(String[] args) {
        ChatBox swingControlDemo = new ChatBox();
        swingControlDemo.showEventDemo();
    }

    /***
     *
     */
    private void initGUI() {
        mainFrame = new JFrame("Chat Box");
        mainFrame.setSize(400, 400);
        GridLayout gridLayout = new GridLayout(6, 1);
        gridLayout.setVgap(2);
        mainFrame.setLayout(gridLayout);

        headerLabel = new JLabel("", JLabel.CENTER);
        JLabel gapLabel = new JLabel("", JLabel.CENTER);
        statusLabel = new JLabel("", JLabel.CENTER);
        textArea = new JTextArea("Sample Text");
        list = new JTextArea();
        JScrollPane scroll = new JScrollPane(list);


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
    private void showEventDemo() {
        headerLabel.setText("Messages");

        JButton sendButton = new JButton("Send");
        JButton refreshButton = new JButton("Refresh");

        sendButton.setActionCommand("send");
        refreshButton.setActionCommand("refresh");

        sendButton.addActionListener(new ButtonClickListener());
        refreshButton.addActionListener(new ButtonClickListener());
        controlPanel.add(sendButton);
        controlPanel.add(refreshButton);

        mainFrame.setVisible(true);
    }

    /***
     *
     */
    private void sendMessage() {

    }

    /***
     *
     */
    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "send":
                    statusLabel.setText("Sent Message: " + textArea.getText());
                    messages += "\nyou:" + textArea.getText();
                    list.setText(messages);
                    textArea.setText("");
                    sendMessage();
                    break;
                case "refresh":
                    list.setText(messages);
                    textArea.setText("");
                    break;
            }
        }
    }
}
