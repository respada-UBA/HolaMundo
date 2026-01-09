package primer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PrimerDesignerGUI extends JFrame {
    private final JTextArea seqArea = new JTextArea(8, 60);
    private final JTextField orfStartField = new JTextField("19", 6);
    private final JTextField orfEndField = new JTextField("687", 6);
    private final JTextField overhang5Field = new JTextField("12", 4);
    private final JTextField overhang3Field = new JTextField("12", 4);
    private final JTextField prefixField = new JTextField("pr_", 10);
    private final JComboBox<String> mutTypeBox = new JComboBox<>(new String[] { "NNK", "fixed" });
    private final JCheckBox scanBox = new JCheckBox("Scan whole ORF", true);
    private final JTextField positionsField = new JTextField("1,2,3", 20); // comma separated
    private final JTextArea mutationsArea = new JTextArea(4, 40); // for fixed mutation list
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable resultTable = new JTable(tableModel);

    public PrimerDesignerGUI() {
        super("Primer Designer (NNK / fixed) - Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildGui();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildGui() {
        JPanel top = new JPanel(new BorderLayout(8, 8));

        // Sequence panel
        JPanel seqPanel = new JPanel(new BorderLayout(4,4));
        seqPanel.setBorder(BorderFactory.createTitledBorder("Input sequence (DNA)"));
        seqArea.setLineWrap(true);
        seqArea.setWrapStyleWord(true);
        seqArea.setText("tttaatgacccacgtcaacttgaagaagtcattgaccttcttgaggtatatcatgagaaaaagaatgtgattgaagagaaaattaaagctcgcttcattgcaaataaaaatactgtatttgaatggcttacgtggaatggcttcattattcttggaaatgctttagaatataaaaacaacttcgttattgatgaagagttacaaccagttactcatgccgcaggtaaccagcctgatatggaaattatatatgaagactttattgttcttggtgaagtaacaacttctaagggagcaacccagtttaagatggaatcagaaccagtaacaaggcattatttaaacaagaaaaaagaattagaaaagcaaggagtagagaaagaactatattgtttattcattgcgccagaaatcaataagaatacttttgaggagtttatgaaatacaatattgttcaaaacacaagaattatccctctctcattaaaacagtttaacatgctcctaatggtacagaagaaattaattgaaaaaggaagaaggttatcttcttatgatattaagaatctgatggtctcattatatcgaacaactatagagtgtgaaagaaaatatactcaaattaaagctggtttagaagaaactttaaataattgggttgttgacaaggaggtaaggtttccatggcaccaccaccaccaccac");
        seqPanel.add(new JScrollPane(seqArea), BorderLayout.CENTER);

        // Options panel
        JPanel opts = new JPanel(new GridBagLayout());
        opts.setBorder(BorderFactory.createTitledBorder("Options"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        opts.add(new JLabel("ORF start (0-based):"), c);
        c.gridx = 1; opts.add(orfStartField, c);
        c.gridx = 2; opts.add(new JLabel("ORF end (0-based):"), c);
        c.gridx = 3; opts.add(orfEndField, c);

        c.gridx = 0; c.gridy = 1; opts.add(new JLabel("5' overhang:"), c);
        c.gridx = 1; opts.add(overhang5Field, c);
        c.gridx = 2; opts.add(new JLabel("3' overhang:"), c);
        c.gridx = 3; opts.add(overhang3Field, c);

        c.gridx = 0; c.gridy = 2; opts.add(new JLabel("Primer name prefix:"), c);
        c.gridx = 1; opts.add(prefixField, c);
        c.gridx = 2; opts.add(new JLabel("Mutation type:"), c);
        c.gridx = 3; opts.add(mutTypeBox, c);

        c.gridx = 0; c.gridy = 3; opts.add(scanBox, c);
        c.gridx = 1; opts.add(new JLabel("Positions (comma sep, AA pos):"), c);
        c.gridx = 2; c.gridwidth = 2; opts.add(positionsField, c);
        c.gridwidth = 1;

        c.gridx = 0; c.gridy = 4; c.gridwidth = 4;
        JPanel mutPanel = new JPanel(new BorderLayout(4,4));
        mutPanel.setBorder(BorderFactory.createTitledBorder("Fixed mutations (one per line, e.g. K1M)"));
        mutationsArea.setText("K1M\nP2L");
        mutPanel.add(new JScrollPane(mutationsArea), BorderLayout.CENTER);
        opts.add(mutPanel, c);
        c.gridwidth = 1;

        top.add(seqPanel, BorderLayout.CENTER);
        top.add(opts, BorderLayout.SOUTH);

        // Buttons
        JPanel buttons = new JPanel();
        JButton runBtn = new JButton("Design primers");
        JButton exportBtn = new JButton("Export CSV");
        JButton copyBtn = new JButton("Copy selected seq to clipboard");
        buttons.add(runBtn);
        buttons.add(exportBtn);
        buttons.add(copyBtn);

        // Results table
        JPanel resultPanel = new JPanel(new BorderLayout(4,4));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Designed primers"));
        tableModel.setColumnIdentifiers(new String[] { "Name", "Sequence", "Direction", "AApos", "Tm", "GC%" });
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(8,8));
        cp.add(top, BorderLayout.NORTH);
        cp.add(buttons, BorderLayout.CENTER);
        cp.add(resultPanel, BorderLayout.SOUTH);

        // Action handlers
        runBtn.addActionListener((ActionEvent e) -> runDesign());
        exportBtn.addActionListener((ActionEvent e) -> exportCsv());
        copyBtn.addActionListener((ActionEvent e) -> copySelectedSequence());
        mutTypeBox.addActionListener((ActionEvent e) -> {
            boolean fixed = "fixed".equalsIgnoreCase((String)mutTypeBox.getSelectedItem());
            mutationsArea.setEnabled(fixed);
            positionsField.setEnabled(!scanBox.isSelected());
        });
        scanBox.addActionListener((ActionEvent e) -> positionsField.setEnabled(!scanBox.isSelected()));
    }

    private void runDesign() {
        tableModel.setRowCount(0);
        PrimerDesignConfig cfg = new PrimerDesignConfig();
        try {
            cfg.inputSequence = seqArea.getText().replaceAll("\\s+", "").toUpperCase();
            cfg.orfStartZeroBased = Integer.parseInt(orfStartField.getText().trim());
            cfg.orfEndZeroBased = Integer.parseInt(orfEndField.getText().trim());
            cfg.primerOverhang5 = Integer.parseInt(overhang5Field.getText().trim());
            cfg.primerOverhang3 = Integer.parseInt(overhang3Field.getText().trim());
            cfg.primerNamePrefix = prefixField.getText().trim();
            cfg.mutationType = (String) mutTypeBox.getSelectedItem();
            cfg.scan = scanBox.isSelected();
            if (!cfg.scan) {
                cfg.positionsList = parsePositions(positionsField.getText());
            }
            if ("fixed".equalsIgnoreCase(cfg.mutationType)) {
                cfg.mutationsList = parseMutations(mutationsArea.getText());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Run in background
        SwingWorker<List<Primer>, Void> worker = new SwingWorker<List<Primer>, Void>() {
            @Override
            protected List<Primer> doInBackground() throws Exception {
                PrimerDesigner designer = new PrimerDesigner(cfg);
                return designer.design();
            }

            @Override
            protected void done() {
                try {
                    List<Primer> primers = get();
                    for (Primer p : primers) {
                        tableModel.addRow(new Object[] {
                                p.name, p.sequence, p.direction.toString(), p.positionAA,
                                String.format("%.1f", p.tm), String.format("%.1f", p.gcPercent)
                        });
                    }
                    JOptionPane.showMessageDialog(PrimerDesignerGUI.this, "Designed " + primers.size() + " primers.", "Done", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PrimerDesignerGUI.this, "Design failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private List<Integer> parsePositions(String text) {
        if (text == null || text.trim().isEmpty()) return new ArrayList<>();
        String[] parts = text.split("[,;\\s]+");
        return Arrays.stream(parts)
                .filter(s -> !s.trim().isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<String> parseMutations(String text) {
        if (text == null || text.trim().isEmpty()) return new ArrayList<>();
        String[] parts = text.split("[\\r\\n]+");
        return Arrays.stream(parts).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No results to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("primers.csv"));
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try (FileWriter fw = new FileWriter(f)) {
            // header
            fw.write("Name,Sequence,Direction,AApos,Tm,GC%\n");
            for (int r = 0; r < tableModel.getRowCount(); ++r) {
                fw.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        tableModel.getValueAt(r,0),
                        tableModel.getValueAt(r,1),
                        tableModel.getValueAt(r,2),
                        tableModel.getValueAt(r,3),
                        tableModel.getValueAt(r,4),
                        tableModel.getValueAt(r,5)
                ));
            }
            JOptionPane.showMessageDialog(this, "Exported to " + f.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copySelectedSequence() {
        int r = resultTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a primer row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = resultTable.convertRowIndexToModel(r);
        String seq = (String) tableModel.getValueAt(modelRow, 1);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(seq), null);
        JOptionPane.showMessageDialog(this, "Sequence copied to clipboard.", "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PrimerDesignerGUI().setVisible(true);
        });
    }
}