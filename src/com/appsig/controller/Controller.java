package com.appsig.controller;

import com.appsig.model.Invcla;
import com.appsig.model.InvTblModel;
import com.appsig.model.Lincla;
import com.appsig.model.LinTblModel;
import com.appsig.view.InvDialog;
import com.appsig.view.SIGAppFrame;
import com.appsig.view.LinDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Controller implements ActionListener, ListSelectionListener {

    private SIGAppFrame frame;
    private InvDialog invoiceDialog;
    private LinDialog lineDialog;

    public Controller(SIGAppFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        System.out.println("Action: " + actionCommand);
        switch (actionCommand) {
            case "Load File":
                loadFile();
                break;
            case "Save File":
                saveFile();
                break;
            case "Add New Invoice":
                addNewInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Add New Item":
                addNewItem();
                break;
            case "Delete Item":
                deleteItem();
                break;
            case "cancelInvoice":
                cancelInvoice();
                break;
            case "createInvoice":
                createInvoice();
                break;
            case "createLine":
                createLine();
                break;
            case "cancelLine":
                cancelLine();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = frame.getInvTable().getSelectedRow();
        if (selectedIndex != -1) {
            System.out.println("You have selected row: " + selectedIndex);
            Invcla currentInvoice = frame.getInvoices().get(selectedIndex);
            frame.getInvNumLabel().setText("" + currentInvoice.getNum());
            frame.getInvDateLabel().setText(currentInvoice.getDate());
            frame.getCuNameLabel().setText(currentInvoice.getCustomer());
            frame.getInvTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            LinTblModel linesTableModel = new LinTblModel(currentInvoice.getLines());
            frame.getLinTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        try {
            int result = fc.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                Path headerPath = Paths.get(headerFile.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(headerPath);
                System.out.println("Invoices have been read");
               
                ArrayList<Invcla> invoicesArray = new ArrayList<>();
                for (String headerLine : headerLines) {
                    try {
                        String[] headerParts = headerLine.split(",");
                        int invoiceNum = Integer.parseInt(headerParts[0]);
                        String invoiceDate = headerParts[1];
                        String customerName = headerParts[2];

                        Invcla invoice = new Invcla(invoiceNum, invoiceDate, customerName);
                        invoicesArray.add(invoice);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
               
                result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    Path linePath = Paths.get(lineFile.getAbsolutePath());
                    List<String> lineLines = Files.readAllLines(linePath);
                    System.out.println("Lines is read");
                    for (String lineLine : lineLines) {
                        try {
                            String lineParts[] = lineLine.split(",");
                            int invoiceNum = Integer.parseInt(lineParts[0]);
                            String itemName = lineParts[1];
                            double itemPrice = Double.parseDouble(lineParts[2]);
                            int count = Integer.parseInt(lineParts[3]);
                            Invcla inv = null;
                            for (Invcla invoice : invoicesArray) {
                                if (invoice.getNum() == invoiceNum) {
                                    inv = invoice;
                                    break;
                                }
                            }

                            Lincla line = new Lincla(itemName, itemPrice, count, inv);
                            inv.getLines().add(line);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Error line format", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    
                }
                frame.setInvoices(invoicesArray);
                InvTblModel invoicesTableModel = new InvTblModel(invoicesArray);
                frame.setInvoicesTableModel(invoicesTableModel);
                frame.getInvTable().setModel(invoicesTableModel);
                frame.getInvoicesTableModel().fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Can not read File", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFile() {
        ArrayList<Invcla> invoices = frame.getInvoices();
        String headers = "";
        String lines = "";
        for (Invcla invoice : invoices) {
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for (Lincla line : invoice.getLines()) {
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }
       
        try {
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                FileWriter hfw = new FileWriter(headerFile);
                hfw.write(headers);
                hfw.flush();
                hfw.close();
                result = fc.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    FileWriter lfw = new FileWriter(lineFile);
                    lfw.write(lines);
                    lfw.flush();
                    lfw.close();
                }
            }
        } catch (Exception ex) {

        }
    }

    private void addNewInvoice() {
        invoiceDialog = new InvDialog(frame);
        invoiceDialog.setVisible(true);
    }

    private void deleteInvoice() {
        int selectedRow = frame.getInvTable().getSelectedRow();
        if (selectedRow != -1) {
            frame.getInvoices().remove(selectedRow);
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void addNewItem() {
        lineDialog = new LinDialog(frame);
        lineDialog.setVisible(true);
    }

    private void deleteItem() {
        int selectedRow = frame.getLinTable().getSelectedRow();

        if (selectedRow != -1) {
            LinTblModel linesTableModel = (LinTblModel) frame.getLinTable().getModel();
            linesTableModel.getLines().remove(selectedRow);
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void cancelInvoice() {
        invoiceDialog.setVisible(false);
        invoiceDialog.dispose();
        invoiceDialog = null;
    }

    private void createInvoice() {
        String date = invoiceDialog.getInvDateField().getText();
        String customer = invoiceDialog.getCustNameField().getText();
        int num = frame.getNextInvoiceNum();
        try {
            String[] dateParts = date.split("-");  
            if (dateParts.length < 3) {
                JOptionPane.showMessageDialog(frame, "Wrong Date format", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
                if (day > 31 || month > 12) {
                    JOptionPane.showMessageDialog(frame, "Wrong Date format", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    Invcla invoice = new Invcla(num, date, customer);
                    frame.getInvoices().add(invoice);
                    frame.getInvoicesTableModel().fireTableDataChanged();
                    invoiceDialog.setVisible(false);
                    invoiceDialog.dispose();
                    invoiceDialog = null;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Wrong Date format", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void createLine() {
        String item = lineDialog.getItemNameField().getText();
        String countStr = lineDialog.getItemCountField().getText();
        String priceStr = lineDialog.getItemPriceField().getText();
        int count = Integer.parseInt(countStr);
        double price = Double.parseDouble(priceStr);
        int selectedInvoice = frame.getInvTable().getSelectedRow();
        if (selectedInvoice != -1) {
            Invcla invoice = frame.getInvoices().get(selectedInvoice);
            Lincla line = new Lincla(item, price, count, invoice);
            invoice.getLines().add(line);
            LinTblModel linesTableModel = (LinTblModel) frame.getLinTable().getModel();
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

    private void cancelLine() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

}
