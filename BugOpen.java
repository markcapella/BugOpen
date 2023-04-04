 
import javafx.application.Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.event.EventHandler;;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.scene.text.Font;

import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Scanner;


// *********************************************************************
// *** BugOpen presents a UI of local user .git repos for selection. ***
// *** The users chosen repo is scanned and all fles in development  ***
// *** are opened in tabs in the Sublime editor for project dev.     ***
// *********************************************************************

public class BugOpen extends Application {
    static final String WINDOW_TITLE = "BugOpen";
    static final String WINDOW_ICON_PNG = "BugOpen.png";
    static final String TMP_DIR_FOR_THIS_APP = "/tmp/BugOpen";

    static final Integer APP_WINDOW_HEIGHT = 400;
    static final Integer APP_WINDOW_WIDTH = 380;

    static final Integer APP_MAX_SCAN_LEVELS = 3;

    static final ObservableList<String> mRepoListItems =
        FXCollections.observableArrayList();

    static final ListView<String> mListView = new ListView<>();

    static final StackPane mRootStackPane = new StackPane();

    // Helper stubs for "processUserSelection()" methods.
    static String mUserSelectedRepoPath;
    static String mUserSelectedRepoName;

    static String mTmpDirForApp;
    static String mTmpDirForAppAndRepo;

    static File mTmpDirForAppFile;
    static File mTmpDirForAppAndRepoFile;

    /**
     * Main loop.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Main loop, recurse through dirs, display selection dialog.
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle(WINDOW_TITLE);
        try {
            stage.getIcons().add(new Image(getClass().
                getResourceAsStream(WINDOW_ICON_PNG)));
        } catch (Exception e) { }

        // Populate mRepoListItems() with user choices of repo names.
        //    (Scan for all '.git' files from user home and down).
        scanDirAndRecurseThru(new File(System.getProperty("user.home")), 0);
        mRepoListItems.sort(String.CASE_INSENSITIVE_ORDER);

        // Set desired ListView row font size to 16.
        mListView.setCellFactory(cell -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item);
                        setFont(Font.font(16));
                    }
                }
            };
        });

        // Add click / process handler to ListView.
        mListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Process main user request, and exit.
                if (event.getClickCount() == 2) {
                    processUserSelection();
                    stage.close();
                }
            }
        });

        // Add repo items to ListView, add it to StackPane window, create scene for
        // stage and display the stupid thing.
        mListView.setItems(mRepoListItems);
        mRootStackPane.getChildren().add(mListView);
        stage.setScene(new Scene(mRootStackPane, APP_WINDOW_HEIGHT, APP_WINDOW_WIDTH));

        stage.show();
    }

    /**
     * Helper to recurse thru dirs and return results.
     *       returns : mRepoListItems() populated.
     */
    void scanDirAndRecurseThru(File currentScanDir, Integer scanLevel) {
        // Ensure valid mListView of files. Secure folders can return empty lists.
        final File[] listFiles = currentScanDir.listFiles();
        if (listFiles == null) {
            return;
        }

        // Scan mListView of files for ".git" directory candidates.
        for (final File fileEntry : listFiles) {
            if (fileEntry.isDirectory()) {
                String canonicalPathAndName = "";
                try {
                    canonicalPathAndName = fileEntry.getCanonicalPath();
                } catch (Exception e) { }

                if (canonicalPathAndName.endsWith("/.git")) {
                    mRepoListItems.add(canonicalPathAndName.
                        substring(0, canonicalPathAndName.lastIndexOf('/')));
                }

                // Sanity check - Limit scan down # levels.
                if (scanLevel < APP_MAX_SCAN_LEVELS) {
                    scanDirAndRecurseThru(fileEntry, scanLevel + 1);
                }
            }
        }
    }

    /**
     * Process main user request.
     *    Display: .git diff in sublime tab.
     *             each modified file in sublime tab.
     */
    public static void processUserSelection() {
        populateHelperStubs();
        ensureTmpFoldersExist();

        createRepoDiff();
        displayRepoDiff();

        createNameStatusDiff();
        displayNameStatusDiff();
        displayAllModifiedFiles();
    }

    /**
     * Ddefines temporary files used throughout.
     */
    public static void populateHelperStubs() {
        // Populate helper stubs.
        mUserSelectedRepoPath =                                 // --> /home/mark/gwenview
            mListView.getSelectionModel().getSelectedItem();
        mUserSelectedRepoName = mUserSelectedRepoPath.          // --> gwenview
            substring(mUserSelectedRepoPath.lastIndexOf('/') + 1);

        mTmpDirForApp = TMP_DIR_FOR_THIS_APP;                   // --> /tmp/Bugopen/
        mTmpDirForAppAndRepo = TMP_DIR_FOR_THIS_APP +           // --> /tmp/Bugopen/gwenview
            "/" + mUserSelectedRepoName;

        mTmpDirForAppFile = new File(String.valueOf(mTmpDirForApp));
        mTmpDirForAppAndRepoFile = new File(mTmpDirForAppAndRepo);
    }

    /**
     * Ensure temporary directories exist.
     */
    public static void ensureTmpFoldersExist() {
        // Create /tmp/app directory.
        if (!mTmpDirForAppFile.exists()) {
            mTmpDirForAppFile.mkdir();
        }

        // Create /tmp/app/repo directory.
        if (!mTmpDirForAppAndRepoFile.exists()) {
            mTmpDirForAppAndRepoFile.mkdir();
        }
    }

    /**
     * Create a .git .diff of the repo.
     */
    public static void createRepoDiff() {
        // Create and execute shell command to diff the repo.
        final String diffCommand = String.format(
            "git diff --output=%s/%s", mTmpDirForAppAndRepo, mUserSelectedRepoName + ".diff");

        // System.out.println("Executing DIFF command: " + diffCommand);
        try {
            Process process = Runtime.getRuntime().
                exec(diffCommand, null, new File(mUserSelectedRepoPath));
            // printResults(process);
        } catch (Exception e) { System.out.println("DIFF COMMAND FAILED"); }
    }

    /**
     * Display the .git .diff for user review.
     */
    public static void displayRepoDiff() {
        // Create and execute shell command to open the diff in subl for review.
        final String sublDiffCommand = String.format(
            "subl %s/%s", mTmpDirForAppAndRepo, mUserSelectedRepoName + ".diff");

        // System.out.println("Executing SUBL DIFF command: " + sublDiffCommand);
        try {
            Process process = Runtime.getRuntime().
                exec(sublDiffCommand, null, new File(mUserSelectedRepoPath));
            // printResults(process);
        } catch (Exception e) { System.out.println("SUBL DIFF COMMAND FAILED"); }
    }

    /**
     * Create a .git nameStatus .diff of the repo.
     */
    public static void createNameStatusDiff() {
        // Create and execute shell command to diff the repo in NameStatus format.
        final String nameStatusDiffCommand = String.format(
            "git diff --name-status --output=%s/%s", mTmpDirForAppAndRepo,
                mUserSelectedRepoName + ".nameStatus");

        // System.out.println("Executing NAMESTATUS DIFF command: " + nameStatusDiffCommand);
        try {
            Process process = Runtime.getRuntime().
                exec(nameStatusDiffCommand, null, new File(mUserSelectedRepoPath));
            // printResults(process);
        } catch (Exception e) { System.out.println("NAMESTATUS DIFF COMMAND FAILED"); }
    }

    /**
     * Display the NameStatus .git .diff for user review.
     */
    public static void displayNameStatusDiff() {
        // Create and execute shell command to open the NameStatus diff in subl for review.
        final String sublNameStatusDiffCommand = String.format(
            "subl %s/%s", mTmpDirForAppAndRepo, mUserSelectedRepoName + ".nameStatus");

        // System.out.println("Executing SUBL DIFF NAMESTATUS command: " + sublNameStatusDiffCommand);
        try {
            Process process = Runtime.getRuntime().
                exec(sublNameStatusDiffCommand, null, new File(mUserSelectedRepoPath));
            // printResults(process);
        } catch (Exception e) { System.out.println("SUBL DIFF NAMESTATUS COMMAND FAILED"); }
    }

    /**
     * Display all the working files currently modified in the repo.
     */
    public static void displayAllModifiedFiles() {
        final String nameStatusDiffFile= String.format(
            "%s/%s", mTmpDirForAppAndRepo, mUserSelectedRepoName + ".nameStatus");

        // Open and read all in NameStatus diff.
        try {
            Scanner scanner = new Scanner(new File(nameStatusDiffFile));

            while (scanner.hasNextLine()) {
                final String nameStatusDiffInputLine = scanner.nextLine();

                final String nameStatusDiffInputFlag = nameStatusDiffInputLine.substring(0, 1);
                if (nameStatusDiffInputFlag.equals("M")) {
                    final String fullInputFileName = mUserSelectedRepoPath + "/" +
                        nameStatusDiffInputLine.substring(2);
                    // System.out.println("|" + nameStatusDiffInputFlag + "| |" + fullInputFileName + "|");

                    // Create and execute shell command to open the diff in subl for review.
                    final String sublFileCommand = String.format(
                        "subl %s", fullInputFileName);
                    // System.out.println("Executing SUBL FILE command: " + sublFileCommand);

                    try {
                        Process process = Runtime.getRuntime().exec(sublFileCommand);
                        // printResults(process);
                    } catch (Exception e) { System.out.println("SUBL FILE COMMAND FAILED"); }

                }
            }
            scanner.close();

        } catch (FileNotFoundException e) { }
    }

    /**
     * Helper to print results of failed system calls.
     */
    public static void printResults(Process process) throws IOException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
