
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.LinkOption;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.swing.*;

public class RecursiveLister extends JFrame {

    private JTextArea fileDisplay;
    private JButton startButton;
    private JButton quitButton;

    public RecursiveLister() {

        // Window settings
        setTitle("Recursive File Lister");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Title label
        JLabel titleLabel = new JLabel(
                "Recursive File Lister",
                SwingConstants.CENTER
        );

        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Text area
        fileDisplay = new JTextArea();
        fileDisplay.setEditable(false);
        fileDisplay.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(fileDisplay);

        // Buttons
        startButton = new JButton("Start");
        quitButton = new JButton("Quit");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(quitButton);

        // Layout
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Start button action
        startButton.addActionListener(e -> selectDirectory());

        // Quit button action
        quitButton.addActionListener(e -> System.exit(0));
    }

    // Open directory chooser
    private void selectDirectory() {

        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );

        chooser.setDialogTitle("Select a Directory");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            Path selectedDirectory =
                    chooser.getSelectedFile().toPath();

            fileDisplay.setText("");

            startButton.setEnabled(false);

            fileDisplay.setText(
                    "Listing files in: " + selectedDirectory + "\n\n"
            );

            // Run recursive listing in the background
            SwingWorker<String, Void> worker =
                    new SwingWorker<>() {

                @Override
                protected String doInBackground() {

                    StringBuilder output = new StringBuilder();

                    listFilesRecursively(
                            selectedDirectory,
                            output,
                            0
                    );

                    return output.toString();
                }

                @Override
                protected void done() {

                    try {
                        fileDisplay.append(get());

                    } catch (Exception ex) {

                        fileDisplay.append(
                                "\nError: " + ex.getMessage()
                        );

                    }

                    startButton.setEnabled(true);
                }
            };

            worker.execute();
        }
    }

    // Recursive method to list files and directories
    private void listFilesRecursively(
            Path directory,
            StringBuilder output,
            int depth
    ) {

        try (Stream<Path> paths = Files.list(directory)) {

            paths.sorted(Comparator.comparing(
                    path -> path.getFileName().toString(),
                    String.CASE_INSENSITIVE_ORDER
            )).forEach(path -> {

                // Indentation shows directory structure
                output.append("    ".repeat(depth));

                output.append(path.getFileName());

                if (Files.isDirectory(
                        path,
                        LinkOption.NOFOLLOW_LINKS
                )) {

                    output.append(" [Directory]\n");

                    // Recursive call
                    listFilesRecursively(
                            path,
                            output,
                            depth + 1
                    );

                } else {

                    output.append(" [File]\n");
                }
            });

        } catch (IOException ex) {

            output.append(
                    "    ".repeat(depth)
                    + "[Unable to access directory: "
                    + ex.getMessage()
                    + "]\n"
            );
        }
    }

    // Main method
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            RecursiveLister application =
                    new RecursiveLister();

            application.setVisible(true);
        });
    }
}
