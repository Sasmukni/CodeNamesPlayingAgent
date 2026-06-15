package utils;

//import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import entities.GameStatus;
import entities.Message;
import entities.Word;

public class GameGUI extends JFrame {

    private GameStatus gameState;
    private List<Message> messages = new ArrayList<>(); //we need a list of messages to write the chat
    public GameGUI( GameStatus gameState) throws BadLocationException {
        this.gameState = gameState;
        
        setTitle("Codenames Board");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);// if it's the current game we want to exit, but if it's a previous game no
        //setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new GridLayout(0,2));

        add(createBoardPanel());
        add(createChatPanel());
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 5, 5, 5));

        // Create empty slots
        JLabel[] slots = new JLabel[25];
        for (int i = 0; i < 25; i++) {
            slots[i] = new JLabel("", SwingConstants.CENTER);
            slots[i].setOpaque(true);
            slots[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panel.add(slots[i]);
        }

        // Populate words
        for (Word word : gameState.words) {
        	if(word.isGuessed) {
        		JLabel label = new JLabel(word.name, SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                label.setBackground(getColor(word.label));
                label.setForeground(Color.WHITE);

                slots[word.pos] = label;
                panel.remove(word.pos);
                panel.add(label, word.pos);
        	}else {
        		JLabel label = new JLabel(word.name, SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createLineBorder(getColor(word.label)));

                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);

                slots[word.pos] = label;
                panel.remove(word.pos);
                panel.add(label, word.pos);
        	}
            
        }

        return panel;
    }
    
    public void refreshBoard(GameStatus updatedState) {
        try {
            this.gameState = updatedState;

            // Refresh entire UI
            getContentPane().removeAll();

            add(createBoardPanel(), BorderLayout.CENTER);
            add(createChatPanel(), BorderLayout.EAST);

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addMessage(Message m) {
    	messages.add(m);
    	refreshBoard(gameState);
    }
    
    private JPanel createChatPanel() throws BadLocationException {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Chat History");
        panel.add(title, BorderLayout.NORTH);

        JTextPane historyPane = new JTextPane();
        historyPane.setEditable(false);
        StyledDocument doc = historyPane.getStyledDocument();
        
        Style baseStyle =  historyPane.addStyle("Base", null);
        StyleConstants.setForeground(baseStyle, Color.BLACK);

        // Define styles
        Style styleRed = historyPane.addStyle("Red", null);
        StyleConstants.setForeground(styleRed, Color.RED);

        Style styleBlue = historyPane.addStyle("Blue", null);
        StyleConstants.setForeground(styleBlue, Color.BLUE);
        if(messages != null && !messages.isEmpty()) {
        	for(Message m : messages) {
        		if(m.getSenderTeam().equals("blue"))
        			doc.insertString(doc.getLength(), m.getSender() + "("+m.getSenderRole()+")", styleBlue);
        		else
        			doc.insertString(doc.getLength(), m.getSender() + "("+m.getSenderRole()+")", styleRed);
        		doc.insertString(doc.getLength(),": " + m.getContent() + "\n" , baseStyle);
        		//historyPane.append(m.toString() + "\n");
        	}
        }
        JScrollPane jsp = new JScrollPane(historyPane);
        jsp.setAutoscrolls(true);
        panel.add(jsp, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 0));

        return panel;
    }
	
    private Color getColor(String label) {
        switch (label.toLowerCase()) {
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            case "black": return Color.BLACK;
            case "blank": return Color.LIGHT_GRAY;
            default: return Color.WHITE;
        }
    }
}
