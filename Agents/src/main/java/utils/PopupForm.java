package utils;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class PopupForm extends JFrame {

    private final JTextField textField;
    private final JSpinner numberField;
    private final FormListener listener;

    public PopupForm(FormListener listener, String _title) {
        //super(parent, "Input Form", true); // modal dialog
        this.listener = listener;
        setTitle(_title);
        setLayout(new GridLayout(3, 2, 10, 10));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        // Text input
        add(new JLabel("Clue Word:"));
        textField = new JTextField();
        add(textField);

        // Numeric input
        add(new JLabel("Number of targets:"));
        numberField = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
        add(numberField);

        // Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitForm());
        add(submitButton);

        setSize(350, 150);
    }

    private void submitForm() {
        String textValue = textField.getText();
        int numericValue = (Integer) numberField.getValue();
        if(textValue.length()==0){
            JOptionPane.showMessageDialog(this,
                "You cannot submit an empty word clue!");

        }else{
            if (listener != null) {
                listener.onFormSubmitted(textValue, numericValue);
            }
            dispose();
        }
    }
}