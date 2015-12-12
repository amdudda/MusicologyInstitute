package com.amdudda;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by amdudda on 12/2/15.
 */
public class UpdateInstrument extends JFrame {
    private JPanel updateInstrumentRootPanel;
    private JTextField instrIDtextField;
    private JTextField instrNameTextField;
    private JTextField acquiredDateTextField;
    private JTextField acquiredFromTextField;
    private JComboBox locationComboBox;
    private JCheckBox isALoanCheckBox;
    private JTextField heightTextField;
    private JTextField widthTextField;
    private JTextField depthTextField;
    private JTextArea descriptionTextArea;
    private JComboBox classificationComboBox1;
    private JTextField subtypeTextField;
    private JTextField cultureTextField;
    private JComboBox tuningTypeComboBox;
    private JTextField lowNoteTextField;
    private JTextField highNoteTextField;
    private JButton exitButton;
    private JComboBox countryComboBox;
    private JButton updateDatabaseButton;
    private JComboBox regionComboBox;
    private JButton selectContactButton;
    private JLabel formDescriptionLabel;
    private JTextArea acquiredFromFullNameTextArea;
    private JTextArea locationSummaryTextArea;
    private Instrument selectedInstrument;
    private String acquiredFromName;

    public UpdateInstrument(String pkToUse) {// fetch the data for the instrument
        // instantiate an Instrument object and read its attributes.
        selectedInstrument = new Instrument(pkToUse);
        acquiredFromName = selectedInstrument.getAcquisitionInfo().getFullName();

        setContentPane(updateInstrumentRootPanel);
        if (pkToUse == null) {
            setTitle("Add Instrument");
            formDescriptionLabel.setText("Add Instrument");
        } else {
            setTitle("Update Database Record");
            formDescriptionLabel.setText("Update Instrument");
        }
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // then populate our fields with the data, IF we have data to use.
        populateFormFields(pkToUse != null);
        // then pack it into a window that fits all the data.
        pack();

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        updateDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pkToUse != null) {
                    // update the record
                    updateInstrument();
                } else {
                    // insert the record.
                    addInstrument();
                }
                dispose();
            }
        });

        heightTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                String fieldValue = heightTextField.getText().toString();
                if (!fieldValue.equals("") && !DataValidator.isDouble(fieldValue)) {
                    JOptionPane.showMessageDialog(updateInstrumentRootPanel, "Please enter a number.");
                }
            }
        });

        widthTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                String fieldValue = widthTextField.getText().toString();
                if (!fieldValue.equals("") && !DataValidator.isDouble(fieldValue)) {
                    JOptionPane.showMessageDialog(updateInstrumentRootPanel, "Please enter a number.");
                }
            }
        });

        depthTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                String fieldValue = depthTextField.getText().toString();
                if (!fieldValue.equals("") && !DataValidator.isDouble(fieldValue)) {
                    JOptionPane.showMessageDialog(updateInstrumentRootPanel, "Please enter a number.");
                }
            }
        });
        selectContactButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectContactScreen scs = new SelectContactScreen(Contact.getBrowsingData(), UpdateInstrument.this);
            }
        });

        // Arbin noticed that the data validation wasn't present for this field.
        acquiredDateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!DataValidator.isDate(acquiredDateTextField.getText())) {
                    JOptionPane.showMessageDialog(updateInstrumentRootPanel, "Please enter a date in YYYY-MM-DD format.");
                    acquiredDateTextField.grabFocus();
                }
            }
        });

        locationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stack overflow says this should detect when the selected value changes:
                // http://stackoverflow.com/questions/58939/jcombobox-selection-change-listener
                selectedInstrument.setLocation(locationComboBox.getSelectedItem().toString());
                selectedInstrument.createLocationInfo();
                locationSummaryTextArea.setText(selectedInstrument.getLocationInfo().toString());
            }
        });

        locationSummaryTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                LocationInfoForm lif = new LocationInfoForm(selectedInstrument, UpdateInstrument.this);
            }
        });
    }

    private void populateFormFields(boolean haveData) {
        // generate our comboboxes - we need these even if pkToUse is null
        DataValidator.generateComboBox(classificationComboBox1, DataValidator.INSTRUMENT_TYPES);
        DataValidator.generateCountryComboBox(countryComboBox);
        DataValidator.generateComboBox(locationComboBox, DataValidator.STORAGE_LOCATIONS);
        DataValidator.generateComboBox(tuningTypeComboBox, DataValidator.TUNING_TYPES);
        DataValidator.generateComboBox(regionComboBox, DataValidator.REGIONS);

        if (haveData) {
            // populate text fields and comboboxes
            instrIDtextField.setText("" + selectedInstrument.getInstID());
            instrNameTextField.setText(selectedInstrument.getInstName());
            acquiredDateTextField.setText(selectedInstrument.getAcquiredDate().toString());
            // TODO: now that we pull in acquiredFrom's human readable name, do we need the FK value?
            acquiredFromTextField.setText("" + selectedInstrument.getAcquiredFrom());
            acquiredFromFullNameTextArea.setText(acquiredFromName);
            heightTextField.setText("" + selectedInstrument.getHeight());
            widthTextField.setText("" + selectedInstrument.getWidth());
            depthTextField.setText("" + selectedInstrument.getDepth());
            // TODO: should subtype be some sort of combobox? if so, needs listener to listen for changes to classification.
            subtypeTextField.setText(selectedInstrument.getSubtype());
            cultureTextField.setText(selectedInstrument.getCulture());
            lowNoteTextField.setText(selectedInstrument.getLowNote());
            highNoteTextField.setText(selectedInstrument.getHighNote());
            descriptionTextArea.setText(selectedInstrument.getDescription());

            // ooh!  tickybox!
            isALoanCheckBox.setSelected(selectedInstrument.isALoan());

            // set combobox selections
            classificationComboBox1.setSelectedItem(selectedInstrument.getInstType());
            regionComboBox.setSelectedItem(selectedInstrument.getRegion());
            countryComboBox.setSelectedItem(selectedInstrument.getCountry());
            locationComboBox.setSelectedItem(selectedInstrument.getLocation());
            tuningTypeComboBox.setSelectedItem(selectedInstrument.getTuning());

            // turn off fields that should only be altered by accountant types
            acquiredDateTextField.setEditable(false);
            acquiredFromTextField.setEditable(false);
            regionComboBox.setEditable(false);
            countryComboBox.setEditable(false);
            isALoanCheckBox.setEnabled(false);

            locationSummaryTextArea.setText(selectedInstrument.getLocationInfo().toString());

            // and turn off some buttons that aren't used when browsing
            selectContactButton.setEnabled(false);
            selectContactButton.setVisible(false);
        }

        // oh, yeah, we don't want anyone touching the ID number field.
        instrIDtextField.setEditable(false);
    }


    public void updateInstrument() {
        // a method that will be called by the update button
        String preppedStatemt = "UPDATE " + Instrument.INSTRUMENT_TABLE_NAME + " SET " +
                Instrument.INSTNAME + "= ?, " +
                Instrument.INSTTYPE + "= ?, " +
                Instrument.SUBTYPE + "= ?, " +
                Instrument.LOCATION + "= ?, " +
                Instrument.HEIGHT + "= ?, " +
                Instrument.WIDTH + "= ?, " +
                Instrument.DEPTH + "= ?, " +
                Instrument.REGION + "= ?, " +
                Instrument.CULTURE + "= ?, " +
                Instrument.TUNING + "= ?, " +
                Instrument.LOWNOTE + "= ?, " +
                Instrument.HIGHNOTE + "= ?, " +
                Instrument.DESCRIPTION + "= ?, " +
                Instrument.ISALOAN + "= ? " +
                "WHERE " + Instrument.INSTID + " = ? ";
        try {
            PreparedStatement updtInst = Database.conn.prepareStatement(preppedStatemt);
            int i = 1;
            updtInst.setString(i, instrNameTextField.getText());
            updtInst.setString(++i, classificationComboBox1.getSelectedItem().toString());
            updtInst.setString(++i, subtypeTextField.getText());
            updtInst.setString(++i, locationComboBox.getSelectedItem().toString());
            updtInst.setDouble(++i, Double.parseDouble(heightTextField.getText()));
            updtInst.setDouble(++i, Double.parseDouble(widthTextField.getText()));
            updtInst.setDouble(++i, Double.parseDouble(depthTextField.getText()));
            updtInst.setString(++i, regionComboBox.getSelectedItem().toString());
            updtInst.setString(++i, cultureTextField.getText());
            updtInst.setString(++i, tuningTypeComboBox.getSelectedItem().toString());
            updtInst.setString(++i, lowNoteTextField.getText());
            updtInst.setString(++i, highNoteTextField.getText());
            updtInst.setString(++i, descriptionTextArea.getText());
            updtInst.setBoolean(++i, isALoanCheckBox.isSelected());
            // don't forget to update only the selected instrument
            updtInst.setInt(++i, selectedInstrument.getInstID());
            updtInst.executeUpdate();
            updtInst.close();
        } catch (SQLException sqle) {
            System.out.println("Unable to update database record.\n" + sqle);
        }
        // also update its location information
        selectedInstrument.getLocationInfo().updateRecord();
    }

    // Method to insert a new instrument.
    public void addInstrument() {
        // a method that will be called by the add button
        // I considered making this a method in Instrument, but didn't want to create
        // listeners for every single field to update Instrument object with data.
        String prSt = "INSERT INTO " + Instrument.INSTRUMENT_TABLE_NAME + " (" +
                Instrument.INSTNAME + ", " +
                Instrument.INSTTYPE + ", " +
                Instrument.SUBTYPE + ", " +
                Instrument.ACQUIREDDATE + ", " +
                Instrument.ACQUIREDFROM + ", " +
                Instrument.LOCATION + ", " +
                Instrument.HEIGHT + ", ";
        // apparently IntelliJ sql parser doesn't like super-long strings, so I've broken it up here.
        // not unique to this bit of code, see this link Clara dug up:
        // http://stackoverflow.com/questions/29740639/how-to-ignore-cannot-resolve-query-parameter-error-in-intellij
        prSt += Instrument.WIDTH + ", " +
                Instrument.DEPTH + ", " +
                Instrument.REGION + ", " +
                Instrument.CULTURE + ", " +
                Instrument.TUNING + ", " +
                Instrument.LOWNOTE + ", " +
                Instrument.HIGHNOTE + ", " +
                Instrument.DESCRIPTION + ", " +
                Instrument.ISALOAN + ") " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        System.out.println(prSt);
        try {
            PreparedStatement addInst = Database.conn.prepareStatement(prSt);
            int i = 0;
            addInst.setString(i, instrNameTextField.getText());
            addInst.setString(++i, classificationComboBox1.getSelectedItem().toString());
            addInst.setString(++i, subtypeTextField.getText());
            addInst.setDate(++i, java.sql.Date.valueOf(acquiredDateTextField.getText()));
            addInst.setInt(++i, Integer.parseInt(acquiredFromTextField.getText()));
            addInst.setString(++i, locationComboBox.getSelectedItem().toString());
            addInst.setDouble(++i, Double.parseDouble(heightTextField.getText()));
            addInst.setDouble(++i, Double.parseDouble(widthTextField.getText()));
            addInst.setDouble(++i, Double.parseDouble(depthTextField.getText()));
            addInst.setString(++i, regionComboBox.getSelectedItem().toString());
            addInst.setString(++i, cultureTextField.getText());
            addInst.setString(++i, tuningTypeComboBox.getSelectedItem().toString());
            addInst.setString(++i, lowNoteTextField.getText());
            addInst.setString(++i, highNoteTextField.getText());
            addInst.setString(++i, descriptionTextArea.getText());
            addInst.setBoolean(++i, isALoanCheckBox.isSelected());
            addInst.executeUpdate();
            // don't forget to close my statement just in case it's still lingering...
            if (addInst != null) addInst.close();
        } catch (SQLException sqle) {
            System.out.println("Unable to update database record.\n" + sqle);
        }
    }

    public void setAcquiredFromTextField(String id) {
        this.acquiredFromTextField.setText(id);
    }

    public void setLocationInfo(HashMap<String,String> objAttribs) {
        // takes an arraylist and sets
        String locType = objAttribs.get("Type");
        if (locType == DataValidator.LOC_LOAN) {
            this.selectedInstrument.setLocationInfo(new Loan(selectedInstrument.getInstID(),selectedInstrument.getAcquiredFrom()));
            Loan loan = (Loan) this.selectedInstrument.getLocationInfo();
            loan.setContactID(Integer.parseInt(objAttribs.get(Loan.CONTACT_ID)));
            loan.setStartDate(Date.valueOf(objAttribs.get(Loan.START_DATE)));
            loan.setEndDate(Date.valueOf(objAttribs.get(Loan.END_DATE)));
        } else if (locType == DataValidator.LOC_EXHIBIT) {
            this.selectedInstrument.setLocationInfo(new OnExhibit(selectedInstrument.getInstID()));
            OnExhibit onEx = (OnExhibit) this.selectedInstrument.getLocationInfo();
            onEx.setExhibitID(Integer.parseInt(objAttribs.get(OnExhibit.EXHIBIT_ID)));
            onEx.setRoom(objAttribs.get(OnExhibit.ROOM));
            onEx.setLocationInRoom(objAttribs.get(OnExhibit.LOCATION_IN_ROOM));
        } else { // presumably storage or library
            this.selectedInstrument.setLocationInfo(new StorageLibrary(selectedInstrument.getInstID()));
            StorageLibrary storLib = (StorageLibrary) this.selectedInstrument.getLocationInfo();
            storLib.setStorageType(locType);
            storLib.setRoom(objAttribs.get(StorageLibrary.ROOM));
            storLib.setCabinet(objAttribs.get(StorageLibrary.CABINET));
            storLib.setShelf(Integer.parseInt(objAttribs.get(StorageLibrary.SHELF)));
        }
        //System.out.println(this.selectedInstrument.getLocationInfo().toString());
        this.locationSummaryTextArea.setText(this.selectedInstrument.getLocationInfo().toString());
    }
}
