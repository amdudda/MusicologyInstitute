package com.amdudda;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Created by amdudda on 12/14/15.
 */
public class AccountingScreen extends ContactManager {
    protected static final String INST_ID = Instrument.INSTID.split("\\.")[1];
    protected static final String INST_NAME = Instrument.INSTNAME.split("\\.")[1];
    protected static final String AC_FROM = Instrument.ACQUIREDFROM.split("\\.")[1];
    protected static final String AC_DATE = Instrument.ACQUIREDDATE.split("\\.")[1];
    protected static final String PURCH_PRICE = Instrument.PURCHASEPRICE.split("\\.")[1];
    protected static final String INS_VAL = Instrument.INSURANCEVALUE.split("\\.")[1];

    private JTable acctDataTable;
    private JButton exitDiscardChangesButton;
    private JPanel acctgScreenRootPanel;
    private JTextField acquiredFromTextField;
    private JTextField acquiredDateTextField;
    private JTextField purchasePriceTextField;
    private JTextField insuranceValueTextField;
    private JButton selectButton;
    private JButton updateButton;
    private ResultSet acctData = null;
    private Statement s = null;
    private AccountingTableModel atm;
    private int selInst = 0;

    public AccountingScreen() {
        setContentPane(acctgScreenRootPanel);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Accounting and Acquisition Information");
        setVisible(true);

        try {
            Statement s = Database.conn.createStatement();
            acctData = s.executeQuery(sqlToUse());
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Unable to generate accounting data resultset.\n" + e);
        }

        atm = new AccountingTableModel(acctData);
        acctDataTable.setModel(atm);

        // now that we have our data table, now we can start packing the window.
        pack();

        exitDiscardChangesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (acctData != null) acctData.close();
                    if (s != null) s.close();
                } catch (SQLException sqle) {
                    System.out.println("Unable to close recordset and statement.\n" + sqle);
                }
                dispose();
            }
        });

        acctDataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                populateFields(acctDataTable.getSelectedRow());
            }
        });

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectContactScreen scs = new SelectContactScreen(Contact.getBrowsingData(),AccountingScreen.this);
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // updates the database and refreshes the dataset
                updateRecord(selInst);//atm.getColumnNumber(INST_ID));
                try {
                    s = Database.conn.createStatement();
                    acctData = s.executeQuery(sqlToUse());
                } catch (SQLException sqle) {
                    System.out.println("Unable to requery database to refresh data table.\n" + sqle);
                }
                atm.setAcctgTable(acctData);
                atm.refresh();
                clearFields();
            }
        });
        purchasePriceTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(DataValidator.isCurrency(purchasePriceTextField.getText()) || purchasePriceTextField.getText().equals(""))) {
                    JOptionPane.showMessageDialog(acctgScreenRootPanel,"Please enter a number less than 1 billion with no more than 2 decimal places.");
                    purchasePriceTextField.grabFocus();
                }
            }
        });

        insuranceValueTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(DataValidator.isCurrency(insuranceValueTextField.getText()) || insuranceValueTextField.getText().equals(""))) {
                    JOptionPane.showMessageDialog(acctgScreenRootPanel,"Please enter a number less than 1 billion with no more than 2 decimal places.");
                    insuranceValueTextField.grabFocus();
                }
            }
        });
        acquiredDateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!(DataValidator.isDate(acquiredDateTextField.getText()) || acquiredDateTextField.getText().equals(""))) {
                    JOptionPane.showMessageDialog(acctgScreenRootPanel,"Please enter a date in YYYY-MM-DD format.");
                    acquiredDateTextField.grabFocus();
                }
            }
        });
    }

    private void clearFields() {
        acquiredFromTextField.setText("");
        acquiredDateTextField.setText("");
        purchasePriceTextField.setText("");
        insuranceValueTextField.setText("");
    }

    private void populateFields(int selectedRow) {
        selInst = Integer.parseInt(atm.getValueAt(selectedRow, atm.getColumnNumber(INST_ID)).toString());
        String acFromVal = atm.getValueAt(selectedRow, atm.getColumnNumber(AC_FROM)).toString();
        acquiredFromTextField.setText(acFromVal);
        String acDateVal = atm.getValueAt(selectedRow, atm.getColumnNumber(AC_DATE)).toString();
        acquiredDateTextField.setText(acDateVal);
        String purchPrVal = atm.getValueAt(selectedRow, atm.getColumnNumber(PURCH_PRICE)).toString();
        purchasePriceTextField.setText(purchPrVal);
        String insVal = atm.getValueAt(selectedRow, atm.getColumnNumber(INS_VAL)).toString();
        insuranceValueTextField.setText(insVal);
    }

    private String sqlToUse() {
        return "SELECT " +
                Instrument.INSTID + ", " + Instrument.INSTNAME + ", " + Instrument.ACQUIREDFROM + ", " +
                Instrument.ACQUIREDDATE + ", " + Instrument.PURCHASEPRICE + ", " + Instrument.INSURANCEVALUE +
                " FROM " + Instrument.INSTRUMENT_TABLE_NAME;
    }


    @Override
    public void setAcquiredFromTextField(String id) {
        // doesn't do anything yet.
        acquiredFromTextField.setText(id);
    }

    public void updateRecord(int id) {
        double pprice = purchasePriceTextField.getText().equals("") ? 0 : Double.parseDouble(purchasePriceTextField.getText());
        double insval = insuranceValueTextField.getText().equals("") ? 0 : Double.parseDouble(insuranceValueTextField.getText());
        System.out.println("inst id: " + id);
        try {
            String sqlToUse = "UPDATE " + Instrument.INSTRUMENT_TABLE_NAME + " SET " +
                    Instrument.ACQUIREDFROM + " = ?, " +
                    Instrument.ACQUIREDDATE + " = ?, " +
                    Instrument.PURCHASEPRICE + " = ?, " +
                    Instrument.INSURANCEVALUE + " = ? " +
                    " WHERE " + Instrument.INSTID + " = ?";
            PreparedStatement ps = Database.conn.prepareStatement(sqlToUse);
            int i = 1;
            ps.setInt(i,Integer.parseInt(acquiredFromTextField.getText()));
            ps.setDate(++i,Date.valueOf(acquiredDateTextField.getText()));
            ps.setDouble(++i,pprice);
            ps.setDouble(++i,insval);
            ps.setInt(++i,id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            // e.printStackTrace();
            System.out.println("Unable to update accounting data." + e);
        }

    }
}
