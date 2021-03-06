package com.amdudda;

import javax.swing.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by amdudda on 12/1/15.
 */
public class WhatsOnExhibitScreen extends JFrame {
    private JTable browseDataTable;
    private JButton returnToMainMenuButton;
    private JPanel whatsOnExhibitRootPanel;
    private JComboBox selectSearchColumnComboBox;
    private JTextField searchStringTextField;
    private JButton searchButton;
    private JButton clearResultsButton;
    private JTextArea exhibitTextArea;
    private JTextArea instrumentTextArea;
    private JScrollPane instrDetailsScrollPane;
    private JButton editSelectedExhibitButton;
    private JButton addNewExhibitButton;
    private BrowseListDataModel bldm;
    private static ResultSet dataToBrowse;
    protected static Statement browseTableStatement;

    public WhatsOnExhibitScreen(ResultSet dTB) {
        this.dataToBrowse = dTB;
        setContentPane(whatsOnExhibitRootPanel);
        pack();
        setTitle("Browse MI Database");
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // this screen takes different result sets based on whether instruments or contacts are being browsed.

        bldm = new BrowseListDataModel(dataToBrowse);
        browseDataTable.setModel(bldm);

        DataValidator.generateSearchFields(selectSearchColumnComboBox,bldm);

        returnToMainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (dataToBrowse != null) dataToBrowse.close();
                    if (browseTableStatement != null) browseTableStatement.close();
                } catch (SQLException sqle) {
                    System.out.println("Unable to close connections.\n" + sqle);
                }
                dispose();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selField = selectSearchColumnComboBox.getSelectedItem().toString();
                String searchString = searchStringTextField.getText();
                bldm.search(selField, searchString);
            }
        });

        clearResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bldm.clearSearch();
            }
        });

        browseDataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int exhColNum = bldm.getColumnNumber(Exhibit.EXHIBIT_ID.split("\\.")[1]);
                int instColNum = bldm.getColumnNumber(Instrument.INSTID.split("\\.")[1]);
                // refresh details at bottom of screen
                String selExh = bldm.getValueAt(browseDataTable.getSelectedRow(), exhColNum).toString();
                exhibitTextArea.setText(new Exhibit(selExh).toString());
                String selInstr = bldm.getValueAt(browseDataTable.getSelectedRow(), instColNum).toString();
                instrumentTextArea.setText(new Instrument(selInstr).toString());
            }
        });

        editSelectedExhibitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selExID = bldm.getValueAt(browseDataTable.getSelectedRow(), 0).toString();
                UpdateExhibit ue = new UpdateExhibit(selExID,bldm);
            }
        });

        addNewExhibitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateExhibit ue = new UpdateExhibit(null,bldm);
            }
        });
    }

}
