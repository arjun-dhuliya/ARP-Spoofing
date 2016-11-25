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
    private final Object LOCK = new Object();
    private String allMessageText;
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JTextArea textArea;
    private JTextArea list;
    /***
     *
     */
    ChatBox() {
        allMessageText = "";
        initGUI();
        setButtonsAndEvents();
    }

    /***
     *
     * @param args
     */
    public static void main(String[] args) {
        new ChatBox();
    }

    /***
     *
     * @param allMessageText
     */
    public void updateMessagesText(String allMessageText) {
        synchronized (LOCK) {
            this.allMessageText = allMessageText;
        }
    }

    public void setListText(String allMessageText) {
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
    public void setButtonsAndEvents() {
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
                    if (textArea.getText().length() > 0) {
                        statusLabel.setText("Sent Message: " + textArea.getText());
                        updateMessagesText("\nyou:" + textArea.getText());
                        setListText(allMessageText);
                        textArea.setText("");
                        sendMessage();
                    } else {
                        statusLabel.setText("Type Something before hitting send");
                    }
                    break;
                case "refresh":
                    setListText(allMessageText);
                    textArea.setText("");
                    break;
            }
        }
    }
}
