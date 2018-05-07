package Bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BotGUI {

    private int width = 700;
    private int length = 700;

    private Bot bot;
    private JButton enterButton;
    private JTextField userInput;
    private JPanel chatbotWindow;
    private JScrollPane conversationAreaScrollPane;
    private JTextArea conversationArea;

    public BotGUI(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        bot = new Bot();

        chatbotWindow.setMinimumSize(new Dimension(width,length));
        chatbotWindow.setPreferredSize(new Dimension(width,length));

        chatbotWindow.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        userInput.setPreferredSize(new Dimension(100,30));

        conversationArea.append("Hello Welcome to the Chatbot \n");
        conversationArea.append("\n");
        ActionListener replyActionListener = e -> {
            String message = userInput.getText();
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please type something!");
            } else {
                conversationArea.append("[" + dateFormat.format(date) + "]" + "You: "+message + "\n");
                conversationArea.append("[" + dateFormat.format(date) + "]" + "Bot: " + bot.getReply(message) + "\n");
                userInput.setText("");
            }
        };

        enterButton.addActionListener(replyActionListener);
        userInput.addActionListener(replyActionListener);
    }
    public static void main(String[] args){
        JFrame frame = new JFrame("Chatbot");
        frame.setContentPane(new BotGUI().chatbotWindow);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}


