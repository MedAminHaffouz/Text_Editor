import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Main {
    private static TextArea TxtArea;
    private static Frame f;

    public static void main(String[] args) {
        f = new Frame("Advanced Text Editor");
        f.setSize(800, 600);
        f.setLayout(new GridLayout(2, 1));
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        // Top panel with buttons
        Panel p = new Panel();
        p.setBackground(Color.GREEN);
        f.add(p);

        Button B1 = new Button("New");
        Button B2 = new Button("Open");
        Button B3 = new Button("Save");
        Button B4 = new Button("Exit");
        Button B5 = new Button("Find");
        Button B6 = new Button("Concatenate");
        Button B7 = new Button("Cut");

        B1.setBackground(Color.WHITE);
        B2.setBackground(Color.WHITE);
        B3.setBackground(Color.WHITE);
        B4.setBackground(Color.WHITE);
        B5.setBackground(Color.WHITE);
        B6.setBackground(Color.WHITE);
        B7.setBackground(Color.WHITE);

        p.add(B1);
        p.add(B2);
        p.add(B3);
        p.add(B4);
        p.add(B5);
        p.add(B6);
        p.add(B7);

        // Bottom panel with text area
        Panel p2 = new Panel();
        p2.setBackground(Color.YELLOW);
        f.add(p2);

        TxtArea = new TextArea("", 10, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        TxtArea.setEditable(true);
        p2.add(TxtArea);

        f.setVisible(true);

        // Button Action Listeners
        B1.addActionListener(e -> TxtArea.setText("")); // New
        B2.addActionListener(e -> openFile()); // Open
        B3.addActionListener(e -> saveFile()); // Save
        B4.addActionListener(e -> System.exit(0)); // Exit
        B5.addActionListener(e -> findInFile()); // Find
        B6.addActionListener(e -> concatenateFiles()); // Concatenate
        B7.addActionListener(e -> cutColumns()); // Cut
    }

    private static void openFile() {
        FileDialog fd = new FileDialog(f, "Open File", FileDialog.LOAD);
        fd.setVisible(true);
        String fileName = fd.getFile();
        String dir = fd.getDirectory();
        if (fileName != null && dir != null) {
            String fullPath = dir + fileName;
            File file = new File(fullPath);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    TxtArea.setText(content.toString());
                } catch (IOException ex) {
                    TxtArea.setText("Error reading file: " + ex.getMessage());
                }
            } else {
                TxtArea.setText("File does not exist.");
            }
        }
    }

    private static void saveFile() {
        FileDialog fd = new FileDialog(f, "Save File", FileDialog.SAVE);
        fd.setVisible(true);
        String fileName = fd.getFile();
        String dir = fd.getDirectory();
        if (fileName != null && dir != null) {
            String fullPath = dir + fileName;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fullPath))) {
                bw.write(TxtArea.getText());
                new File(fullPath).setLastModified(System.currentTimeMillis());
                TxtArea.setText("File saved successfully: " + fullPath);
            } catch (IOException ex) {
                TxtArea.setText("Error saving file: " + ex.getMessage());
            }
        }
    }

    private static void findInFile() {
        Dialog dialog = new Dialog(f, "Find in File", true);
        dialog.setLayout(new FlowLayout());
        dialog.setSize(300, 150);

        Label dirLabel = new Label("Directory:");
        TextField dirField = new TextField(20);
        Label patternLabel = new Label("Pattern:");
        TextField patternField = new TextField(20);
        Button findButton = new Button("Search");

        dialog.add(dirLabel);
        dialog.add(dirField);
        dialog.add(patternLabel);
        dialog.add(patternField);
        dialog.add(findButton);

        findButton.addActionListener(e -> {
            String directory = dirField.getText();
            String pattern = patternField.getText();
            File dir = new File(directory);
            if (dir.exists() && dir.isDirectory()) {
                StringBuilder result = new StringBuilder();
                searchFiles(dir, pattern, result);
                TxtArea.setText(result.toString());
            } else {
                TxtArea.setText("Invalid directory: " + directory);
            }
            dialog.setVisible(false);
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dialog.setVisible(false);
            }
        });
        dialog.setVisible(true);
    }

    private static void searchFiles(File directory, String pattern, StringBuilder result) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchFiles(file, pattern, result);
                } else if (file.getName().contains(pattern)) {
                    result.append(file.getAbsolutePath()).append("\n");
                }
            }
        }
    }

    private static void concatenateFiles() {
        StringBuilder content = new StringBuilder();
        while (true) {
            FileDialog fd = new FileDialog(f, "Select File to Concatenate", FileDialog.LOAD);
            fd.setVisible(true);
            String fileName = fd.getFile();
            String dir = fd.getDirectory();

            if (fileName == null || dir == null) {
                break; // Exit if user cancels file selection
            }

            String fullPath = dir + fileName;
            File file = new File(fullPath);
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                } catch (IOException ex) {
                    TxtArea.setText("Error reading file: " + ex.getMessage());
                    return;
                }
            }

            Dialog confirm = new Dialog(f, "Continue?", true);
            confirm.setLayout(new FlowLayout());
            confirm.setSize(200, 100);
            Button yes = new Button("Yes");
            Button no = new Button("No");
            confirm.add(new Label("Add another file?"));
            confirm.add(yes);
            confirm.add(no);

            final boolean[] shouldContinue = {true}; // Array to make it effectively final
            yes.addActionListener(e -> confirm.setVisible(false));
            no.addActionListener(e -> {
                shouldContinue[0] = false;
                confirm.setVisible(false);
            });
            confirm.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    shouldContinue[0] = false;
                    confirm.setVisible(false);
                }
            });
            confirm.setVisible(true);

            if (!shouldContinue[0]) {
                break;
            }
        }
        TxtArea.setText(content.toString());
    }

    private static void cutColumns() {
        Dialog dialog = new Dialog(f, "Cut Columns", true);
        dialog.setLayout(new FlowLayout());
        dialog.setSize(300, 200);

        Label fileLabel = new Label("File:");
        TextField fileField = new TextField(20);
        Label delimLabel = new Label("Delimiter:");
        TextField delimField = new TextField(5);
        Label fieldsLabel = new Label("Fields (e.g., 1,3):");
        TextField fieldsField = new TextField(10);
        Button cutButton = new Button("Cut");

        dialog.add(fileLabel);
        dialog.add(fileField);
        dialog.add(delimLabel);
        dialog.add(delimField);
        dialog.add(fieldsLabel);
        dialog.add(fieldsField);
        dialog.add(cutButton);

        cutButton.addActionListener(e -> {
            String filePath = fileField.getText();
            String delimiter = delimField.getText();
            String fieldsArg = fieldsField.getText();

            if (filePath.isEmpty() || delimiter.isEmpty() || fieldsArg.isEmpty()) {
                TxtArea.setText("Please fill all fields.");
                return;
            }

            String[] fieldIndices = fieldsArg.split(",");
            int[] fields = new int[fieldIndices.length];
            try {
                for (int i = 0; i < fieldIndices.length; i++) {
                    fields[i] = Integer.parseInt(fieldIndices[i]) - 1;
                }
            } catch (NumberFormatException ex) {
                TxtArea.setText("Invalid field format. Use numbers separated by commas.");
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                TxtArea.setText("File does not exist: " + filePath);
                return;
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(delimiter);
                    StringBuilder lineOutput = new StringBuilder();
                    for (int field : fields) {
                        if (field < parts.length) {
                            lineOutput.append(parts[field]).append(" ");
                        }
                    }
                    output.append(lineOutput.toString().trim()).append("\n");
                }
                TxtArea.setText(output.toString());
            } catch (IOException ex) {
                TxtArea.setText("Error: " + ex.getMessage());
            }
            dialog.setVisible(false);
        });

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dialog.setVisible(false);
            }
        });
        dialog.setVisible(true);
    }
}