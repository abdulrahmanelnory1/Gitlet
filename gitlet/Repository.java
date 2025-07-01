package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

public class Repository implements Serializable {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The Commits directory.
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "Commits");

    /**
     * The Blobs directory.
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "Blobs");

    public static final File BRANCHES_DIR = join(GITLET_DIR, "Branches");

    public static final File CURRENT_BRANCH = join(GITLET_DIR, "current createBranch");
    /**
     * The head file represents the current commit.
     */
    public static final File HEAD = join(GITLET_DIR, "head");
    /**
     * The index file saves the staged files.
     */
    public static final File index = join(GITLET_DIR, "index");
    /**
     * represents the added files in staging area before they are commited.
     */
    StagingArea stagingArea;
    /**
     * Represents the current createBranch.
     */
    String curBranch;
    /**
     * Tracks whether the Gitlet repository has been initialized.
     */

    public boolean backupPerformed = false; // to backup GITLET_DIR state each time when the program terminates

    public void mapInitializations() {
        stagingArea = new StagingArea();
        //blobs = new HashMap<String, Blob>();
    }

    public void validateInitialized() {

        if (!GITLET_DIR.exists()) {
            System.out.println("GITLET_DIR does not exist");
            System.exit(0);
        }
    }

    public void backup() {

        if (!GITLET_DIR.exists() || backupPerformed)
            return;

        mapInitializations();

        curBranch = readContentsAsString(CURRENT_BRANCH);
        if (index.exists())// ensure that index file exists cuz it can not be existent if there aren`t files staged before.
            stagingArea = readObject(index, StagingArea.class);
    }

    public void init() {

        if (GITLET_DIR.exists()) {
            System.out.println("GITLET_DIR already exists");
            return;
        }

        GITLET_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();

        mapInitializations();

        // create initial commit has no parent and no files
        Commit initialCommit = new Commit("initial commit", null);
        // save the initialCommit object in the file named by its SHA1 id
        String initialCommitId = initialCommit.getId();
        File commitFile = new File(COMMITS_DIR, initialCommitId);
        writeObject(commitFile, initialCommit);

        // save the head state in HEAD file.
        writeContents(HEAD, initialCommitId);

        // save the master createBranch in a file in BRANCHES_DIR.
        File masterBranchFile = new File(BRANCHES_DIR, "master");
        writeContents(masterBranchFile, initialCommitId); // master file content is the most recent commit in the master createBranch which is the initial commit id

        // by default the first createBranch is the current createBranch is the master createBranch.
        curBranch = "master";
        writeContents(CURRENT_BRANCH, curBranch);// Save the current createBranch which is master in the CURRENT_BRANCH file.
    }

    public void add(String fileName) {

        validateInitialized();

        File addedFileForAddition = new File(CWD, fileName);

        // if there is no such a file in CWD => exit without changing anything.
        if (!addedFileForAddition.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        stagingArea.addForAddition(fileName);
        // write the new staged files in index file
        stagingArea.save();
    }

    public void commit(String message) {

        validateInitialized();

        if (message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }

        if (stagingArea.getAddedFiles().isEmpty() && stagingArea.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // Get the parent commit (the current commit)
        String headCommitId = readContentsAsString(HEAD);
        Commit parent = Commit.getCommit(headCommitId);
        String parentId = parent.getId();

        // Create new commit with message as its message and a parentId as its parent.
        Commit newCommit = new Commit(message, parentId);
        newCommit.setFiles(parent.getFiles());

        // Apply added and removed files
        HashMap<String, String> addedFiles = stagingArea.getAddedFiles();
        HashMap<String, String> removedFiles = stagingArea.getRemovedFiles();

        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            newCommit.addFile(entry.getKey());
        }

        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            newCommit.removeFile(entry.getKey());
        }

        // Save new commit
        newCommit.save();

        // Get the id of the new Commit.
        String newCommitId = newCommit.getId();

        // public static final File CURRENT_BRANCH = join(GITLET_DIR, "current createBranch");

        // set the new commit as the most recent commit in the current createBranch.
        File curBranchFile = new File(BRANCHES_DIR, curBranch);
        writeContents(curBranchFile, newCommitId);

        // Update HEAD
        writeContents(HEAD, headCommitId);

        // Clear and save the staging area
        stagingArea.clear();
        stagingArea.save();
    }

    /**
     * remove the file:
     * If the file is staged, unstage it
     * and if the file is tracked in the current commit, stage it for removal
     * and remove the file from the working directory if the user has not already done so.
     */
    public void removeFile(String fileMarkedForRemoval_Name) {

        Commit headCommit = Commit.getCommit(readContentsAsString(HEAD));

        validateInitialized();
        boolean isTracked = headCommit.containsFile(fileMarkedForRemoval_Name);
        //boolean stagedForRemoval = stagingArea.existentForRemoval(fileMarkedForRemoval_Name);
        boolean stagedForAddition = stagingArea.existentForAddition(fileMarkedForRemoval_Name);


        if (!stagedForAddition && !isTracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        // Unstage the file if it is currently staged for addition.
        if (stagedForAddition) {
            stagingArea.unStage(fileMarkedForRemoval_Name);
            stagingArea.save();
        }
        // If the file is tracked in the current commit => stage it for removal and delete it from the CWD.
        if (isTracked) {

            // mark the file for removal.
            stagingArea.markForRemoval(fileMarkedForRemoval_Name);
            stagingArea.save();

            // Delete the file from CWD.
            Utils.restrictedDelete(fileMarkedForRemoval_Name);
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines.
     */
    public void findCommit(String commitMessage) {

        validateInitialized();

        boolean foundCommits = false; // Becomes true if at least one commit found with the given message.

        // retrieve the commit files.
        List<String> commitNames = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR
        for (String commitName : commitNames) {

            // get the commit object from the commit file.
            Commit commit = Commit.getCommit(commitName);
            if (commit.getMessage().equals(commitMessage)) {
                System.out.println(commitName);

                foundCommits = true;
            }
        }

        if (!foundCommits)
            System.out.println("Found no commit with that message.");
    }

    public void log() {

        validateInitialized();

        Commit current = Commit.getCommit(readContentsAsString(HEAD));// start iterate from the most recent commit.

        while (current != null) {

            System.out.println(current);
            Commit parent = Commit.getCommit(current.getParent()); // git the parent by its id from the commits map.
            current = parent;
        }

        /*
        if( commit is merged ){

        .....

        }
        */
    }

    public void global_log() {

        validateInitialized();

        // retrieve the commit files
        List<String> commitFiles = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR

        for (String commitId : commitFiles) {

            // get the commit object from the commit file.
            Commit commit = Commit.getCommit(commitId);
            System.out.println(commit);
        }
        /*
        if( commit is merged ){

        .....

        }
        */
    }

    public void checkoutFile(String fileName) {

        validateInitialized();

        Commit curCommit = Commit.getCommit(readContentsAsString(HEAD)); // Retrieve the current commit (head commit).

        // Restore the file from the curCommit if it contains it.
        if (curCommit.containsFile(fileName)) {

            // get the file from the curCommit.
            String fileId = curCommit.getFileId(fileName);

            String fileContent = Blob.getBlob(fileId).getContent();// get the file content from it's blob.

            // Add the file in CWD if the file doesn't exist or overwrite the file if it exists.
            File fileInCWD = new File(CWD, fileName);
            writeContents(fileInCWD, fileContent);
        } else
            System.out.println("File does not exist in that commit.");
    }

    public void checkoutFile(String commitId, String fileName) {

        validateInitialized();

        Commit commit = Commit.getCommit(commitId); // retrieve the commit by its Id.

        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        if (!commit.containsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String fileId = commit.getFileId(fileName); // get the file from the curCommit.

        String fileContent = Blob.getBlob(fileId).getContent();// get the file content from it's blob.

        // Add the file in CWD if the file doesn't exist or overwrite the file if it exists.
        File fileInCWD = new File(CWD, fileName);
        writeContents(fileInCWD, fileContent);
    }

    /**
     * Checks out all files from the given createBranch into the working directory (CWD).
     * Behavior:-
     * 1. Validates the target createBranch exists and is not the current createBranch.
     * 2. Aborts if untracked files in CWD would be overwritten.
     * 3. For each file in the target createBranch:
     *    - Overwrites existing CWD version (if present)
     *    - Creates new files (if not present)
     * 4. Deletes files tracked in current createBranch but absent in target createBranch.
     */
    public void checkoutBranch(String branchName) {

        validateInitialized();

        // Check if the createBranch already exists
        File BranchFile = new File(BRANCHES_DIR, branchName);
        if (!BranchFile.exists()) {
            System.out.println("No such createBranch exists.");
            return;
        }
        // get the createBranch head id(the id of the most recent commit in that createBranch)
        String branchHeadId = readContentsAsString(BranchFile);

        // if that createBranch is the current createBranch, do nothing.
        if (readContentsAsString(CURRENT_BRANCH).equals(branchName)) {
            System.out.println("No need to checkoutFile the current createBranch.");
            return;
        }

        Commit branchHeadCommit = Commit.getCommit(branchHeadId);
        String headCommitID = readContentsAsString(HEAD);

        // If there are files in the createBranch head commit need to be fetched but un tracked, Report it and exit.
        for (String fileName : branchHeadCommit.getFiles().keySet()) {

            File fileInCWD = new File(CWD, fileName);
            if (fileInCWD.exists() && !Commit.getCommit(headCommitID).containsFile(fileName) && !stagingArea.existentForAddition(fileName)) {

                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        // Add the createBranch head to CWD, overwriting the versions of the files that are already there if they exist
        for (String fileName : branchHeadCommit.getFiles().keySet()) {

            File fileInCWD = new File(CWD, fileName);

            String FileContentInBranchHead = Blob.getBlob(branchHeadCommit.getFileId(fileName)).getContent();
            writeContents(fileInCWD, FileContentInBranchHead);
        }

        // Delete any files tracked in the current commit but not in the checked out createBranch.
        for (String fileName : Commit.getCommit(headCommitID).getFiles().keySet()) {

            if(!branchHeadCommit.containsFile(fileName))
                restrictedDelete(fileName);

        }

        // Update the current createBranch to the checked out createBranch.
        curBranch = branchName;
        writeObject(CURRENT_BRANCH, curBranch);

        // Clear the staging area.
        stagingArea.clear();
    }

    /**
     * Create new branch with the given name and points it as the current head commit.
     */
    public void createBranch(String branchName) {

        validateInitialized();

        // Check if the createBranch already exists
        File branchFile = new File(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A createBranch with that name already exists.");
            return;
        }

        // save the new createBranch in BRANCHES_DIR and its head commit is the current commit.
        writeContents(branchFile, readContentsAsString(HEAD));
    }

    /**
     *  Delete Branch with the given name.
     *  The deleted createBranch can not be the curren branch.
     *  Be sure that The deleted createBranch exists before deletion.
     */
    public void removeBranch(String branchName) {

        validateInitialized();

        File branchFile = new File(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A createBranch with that name does not exist.");
            return;
        }

        if (readContentsAsString(CURRENT_BRANCH).equals(branchName)) {
            System.out.println("Cannot remove the current createBranch.");
            return;
        }

        // delete the createBranch file from the BRANCHES_DIR.
        Utils.restrictedDelete(branchFile);


    }

    public void status() {
        //............
    }

    public void reset() {
        //.....
    }

    public void merge() {
        //.....
    }
}
