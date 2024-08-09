package src;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CurrencyConverterApp extends JFrame {
    private JTextField amountField;
    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JLabel resultLabel;

    public CurrencyConverterApp() {
        setTitle("Currency Converter");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.LIGHT_GRAY);
        Font font = new Font("Cooper", Font.PLAIN, 14);
        Font font1 = new Font("Cooper", Font.BOLD, 14);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding

        JLabel amountLabel = new JLabel("Enter Amount:");
        amountField = new JTextField(10);
        amountField.setFont(font);
        amountField.setPreferredSize(new Dimension(150, amountField.getPreferredSize().height));
        amountField.setMinimumSize(amountField.getPreferredSize());

        JLabel fromLabel = new JLabel("From Currency:");
        fromCurrency = new JComboBox<>(new String[]{"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "INR", "CNY", "RUB", "BRL"});
        fromCurrency.setFont(font);

        JLabel toLabel = new JLabel("To Currency:");
        toCurrency = new JComboBox<>(new String[]{"USD", "EUR", "GBP", "JPY", "AUD", "CAD", "INR", "CNY", "RUB", "BRL"});
        toCurrency.setFont(font);

        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });

        resultLabel = new JLabel("Result: ");
        resultLabel.setFont(font1);
        resultLabel.setForeground(Color.BLACK);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearResult();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(amountLabel, gbc);

        gbc.gridx = 1;
        add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(fromLabel, gbc);

        gbc.gridx = 1;
        add(fromCurrency, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(toLabel, gbc);

        gbc.gridx = 1;
        add(toCurrency, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(convertButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(resultLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(clearButton, gbc);
    }

    private void convertCurrency() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    String from = (String) fromCurrency.getSelectedItem();
                    String to = (String) toCurrency.getSelectedItem();
                    double amount = Double.parseDouble(amountField.getText());

                    @SuppressWarnings("deprecation")
                    URL url = new URL("https://api.exchangerate-api.com/v4/latest/" + from);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Simple JSON parsing without external libraries
                    String responseString = response.toString();
                    String exchangeRateString = extractRate(responseString, to);

                    if (exchangeRateString != null) {
                        double exchangeRate = Double.parseDouble(exchangeRateString);
                        double convertedAmount = amount * exchangeRate;
                        resultLabel.setText(String.format("Result: %.2f %s = %.2f %s", amount, from, convertedAmount, to));
                    } else {
                        resultLabel.setText("Error: Invalid currency code.");
                    }

                    reader.close();
                    connection.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    resultLabel.setText("Error in conversion.");
                }
                return null;
            }
        };

        worker.execute();
    }

    // Extract exchange rate from JSON response string
    private String extractRate(String jsonResponse, String currencyCode) {
        String searchString = "\"" + currencyCode + "\":";
        int startIndex = jsonResponse.indexOf(searchString);
        if (startIndex == -1) {
            return null;
        }

        startIndex += searchString.length();
        int endIndex = jsonResponse.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = jsonResponse.indexOf("}", startIndex);
        }

        if (endIndex == -1) {
            return null;
        }

        return jsonResponse.substring(startIndex, endIndex);
    }

    private void clearResult() {
        resultLabel.setText("Result: ");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CurrencyConverterApp app = new CurrencyConverterApp();
            app.setVisible(true);
        });
    }
}
