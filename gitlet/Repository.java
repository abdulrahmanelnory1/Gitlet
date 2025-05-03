package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 */
public class Repository {
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
    /**
     * The master file represents the most recent commit.
     */
    public static final File MASTER = join(GITLET_DIR, "master");

    public static final File CURRENT_BRANCH = join(BRANCHES_DIR, "current branch");
    /**
     * The head file represents the current commit.
     */
    public static final File HEAD = join(GITLET_DIR, "head");
    /**
     * The index file saves the staged files.
     */
    public static final File index = join(GITLET_DIR, "index");
    /**
     * Data structure(map) represents all blobs with the blob id(SHA1 for the file content) as a key and blob.fileName as value.
     */
    HashMap<String, Blob> blobs;
    /**
     * set(map) represents all commits with the id(SHA1 for the commit) as a key and commit object as value.
     */
    HashMap<String, Commit> commits;
    /**
     * points to the current commit.
     */
    String head;
    /**
     * represents the added files in staging area before they are commited.
     */
    StagingArea stagingArea;
    /**
     * Represents the current branch.
     */
    String curBranch;
    /**
     * Tracks whether the Gitlet repository has been initialized.
     */

    public boolean backupPerformed = false; // to backup GITLET_DIR state each time when the program terminates

    public void mapInitializations() {
        stagingArea = new StagingArea();
        commits = new HashMap<String, Commit>();
        blobs = new HashMap<String, Blob>();
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

        // retrieve the commit files into the commits map
        List<String> commitFileNames = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR
        for (String commitFileName : commitFileNames) {


            // get the commit object from the commit file.
            File commitFile = new File(COMMITS_DIR, commitFileName);
            Commit commit = readObject(commitFile, Commit.class);

            commits.put(commitFileName, commit);
        }

        // retrieve the blobs files into the blobs map
        List<String> blobFileNames = Utils.plainFilenamesIn(BLOBS_DIR); // get all the commit file names from the BLOBS_DIR
        for (String blobFileName : blobFileNames) {

            // get the blob object from the commit file.
            File blobFile = new File(BLOBS_DIR, blobFileName);
            Blob blob = readObject(blobFile, Blob.class);
            String blobId = blob.getId();
            blobs.put(blobId, blob);
        }

        head = readContentsAsString(HEAD);
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

        // Add the initial commit in the commits map with its id as a key.
        commits.put(initialCommitId, initialCommit);// add the initial commit in the commits map

        // save the head state in HEAD file.
        head = initialCommitId;
        writeContents(HEAD, head);

        // save the master branch in a file in BRANCHES_DIR.
        File masterBranchFile = new File(BRANCHES_DIR, "master");
        writeContents(masterBranchFile, initialCommitId); // master file content is the most recent commit in the master branch which is the initial commit id

        // by default the first branch and is current branch is the master branch.
        curBranch = "master";
        writeContents(CURRENT_BRANCH ,  curBranch);// Save the current branch which is master in the CURRENT_BRANCH file.
    }

    public void add(String fileName) {

        validateInitialized();

        File addedFileForAddition = new File(CWD, fileName);

        // if there is no such a file in CWD => exit without changing anything.
        if (!addedFileForAddition.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // Get the added file content.
        String fileContent = Utils.readContentsAsString(addedFileForAddition);

        // find SHA1 id for the addedFile
        String fileId = Utils.sha1(fileContent);

        // check if the file is tracked in the head commit (current commit).
        boolean existsInLastCommit = commits.get(head).containsFile(fileName);

        // add the file if it is not existent in the last commit so it needs to be added.
        if (!existsInLastCommit) {

            // check if there is already blob for the addedFile content
            boolean blobExsits = blobs.containsKey(fileId);

            if (!blobExsits) {
                // create new blob for the new version of the addedFile
                Blob blob = new Blob(addedFileForAddition);

                // Create a new file for the new blob in BLOBS_DIR
                File blobFile = new File(BLOBS_DIR, blob.getId());
                writeObject(blobFile, blob);

                // add the new blob in blobs set
                blobs.put(blob.getId(), blob);
            }

            stagingArea.addForAddition(fileName, fileId);
            // write the new staged files in index file
            stagingArea.save(index);

        }

    }// else => don`t add it again => do nothing


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

        // Get the parent commit (head of current branch)
        String parentId = commits.get(head).getId();
        File parentFile = new File(COMMITS_DIR, parentId);
        Commit parent = readObject(parentFile, Commit.class);

        // Create new commit with message and parent
        Commit newCommit = new Commit(message, parentId);
        newCommit.setFiles(parent.getFiles());

        // Apply added and removed files
        HashMap<String, String> addedFiles = stagingArea.getAddedFiles();
        HashMap<String, String> removedFiles = stagingArea.getRemovedFiles();

        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            newCommit.addFile(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            newCommit.removeFile(entry.getKey());
            File removedFile = new File(CWD, entry.getKey());
            Utils.restrictedDelete(removedFile);// remove from the CWD
        }

        // Save new commit
        String newCommitId = newCommit.getId();
        commits.put(newCommitId, newCommit);
        File newCommitFile = new File(COMMITS_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);

        // update the most recent commit in the curBranch to the new commit.
        File curBranchFile = new File(BRANCHES_DIR, curBranch);
        writeContents(curBranchFile, newCommitId);

        // Update HEAD
        head = newCommitId;
        writeContents(HEAD, head);

        // Clear and save staging area
        stagingArea.clear();
        stagingArea.save(index);
    }


    public void rm(String fileMarkedForRemoval_Name) {

        validateInitialized();

        // Unstage the file if it is currently staged for addition.
        if (stagingArea.existentForAddition(fileMarkedForRemoval_Name)) {
            stagingArea.unStage(fileMarkedForRemoval_Name);
            return;
        }

        // If the file is tracked in the current commit => stage it for removal from the CWD.
        Commit headCommit = commits.get(head);
        if (headCommit.containsFile(fileMarkedForRemoval_Name)) {

            // mark the file for removal.
            String fileMarkedForRemoval_Id = headCommit.getFileId(fileMarkedForRemoval_Name);
            stagingArea.markForRemoval(fileMarkedForRemoval_Name, fileMarkedForRemoval_Id);
            stagingArea.save(index);

        } else
            System.out.println("No reason to remove the file.");
    }

    public void find(String message) {

        validateInitialized();

        boolean foundCommits = false; // Indicating that there is at least one commit found with the given message.

        for (Map.Entry<String, Commit> entry : commits.entrySet()) {

            String commitId = entry.getKey(); // The commit ID is the key in the map
            Commit commit = entry.getValue();

            if (commit.getMessage().equals(message)) {
                System.out.println(commitId);

                foundCommits = true;
            }
        }

        if (!foundCommits)
            System.out.println("Found no commit with that message.");
    }

    public void log() {

        validateInitialized();

        Commit current = commits.get(head);// start iterate from the most recent commit.

        while (current != null) {

            System.out.println(current);
            Commit parent = commits.get(current.getParent()); // git the parent by its id from the commits map.
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

        for (Map.Entry<String, Commit> entry : commits.entrySet()) {

            String commitId = entry.getKey(); // The commit ID is the key in the map
            Commit commit = entry.getValue();


            System.out.println(commit);
        }

        /*
        if( commit is merged ){

        .....

        }
        */
    }

    public void checkout(String fileName) {

        validateInitialized();

        Commit curCommit = commits.get(head); // retrieve the commit(current commit) head points to.

        // Restore the file from the curCommit IF it contains it.
        if (curCommit.containsFile(fileName)) {

            // get the file from the curCommit.
            String fileId = curCommit.getFileId(fileName);

            String fileContent = blobs.get(fileId).getContent();// get the file content from it's blob.

            // Add the file in CWD if the file doesn't exist or overwrite the file if it exists.
            File fileInCWD = new File(CWD, fileId);
            writeContents(fileInCWD, fileContent);
        } else
            System.out.println("File does not exist in that commit.");
    }

    public void checkout(String commitId, String fileName) {

        validateInitialized();

        if (commits.containsKey(commitId)) {

            Commit commit = commits.get(commitId); // retrieve the commit by its Id.

            // Restore the file from the commit IF it contains it.
            if (commit.containsFile(fileName)) {


                String fileId = commit.getFileId(fileName); // get the file from the curCommit.

                String fileContent = blobs.get(fileId).getContent();// get the file content from it's blob.

                // Add the file in CWD if the file doesn't exist or overwrite the file if it exists.
                File fileInCWD = new File(CWD, fileId);
                writeContents(fileInCWD, fileContent);
            } else // If no such a file in the commit has the given commitId.
                System.out.println("File does not exist in that commit.");
        } else // if no such a commit id.
            System.out.println("No commit with that id exists.");
    }

    public void checkoutBranch(String branchName) {

        validateInitialized();

        // Check if the branch already exists
        File BranchFile  = new File(BRANCHES_DIR, branchName);
        if (!BranchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        // get the branch (the most recent commit in that branch)
        String branchId = readContentsAsString(BranchFile);

        // if i am already in the branch branchName => then i go nowhere .
        if (head.equals(branchId)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        // Retrieve all files in the commit at the head of the given branch.
        Commit branchHead = commits.get(branchId);

        // iterate all the files in CWD and if there is a file exists in the branchHead => update it in the CWD, if not => remove it.
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD); // retrieve all the files from the cwd.
        Map<String, Integer> cwdFilesMap = new HashMap<String, Integer>();// create a map for constant time access.

        assert cwdFiles != null;
        for (String fileName : cwdFiles) { // iterate CWD files.

            cwdFilesMap.put(fileName, 0);

            File fileInCWD = new File(CWD, fileName);

            if (branchHead.containsFile(fileName)) { // check if file exists in the branch head commit to be updated to the branchHead version.

                // find the id of the fileInBranchHead
                String fileIdInBranchHead = branchHead.getFileId(fileName);

                // find the id of the same file in CWD.
                String fileIdInCWD = Utils.sha1(fileInCWD);

                // if they are not the same then => overwrite the file in CWD
                if (!fileIdInBranchHead.equals(fileIdInCWD)) {

                    // check if the file is tracked.
                    if (stagingArea.containsAddedFile(fileIdInCWD)) {
                        String FileContentInBranchHead = blobs.get(fileIdInBranchHead).getContent();
                        writeContents(fileInCWD, FileContentInBranchHead);
                    } else // the file is not tracked.
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            } else //delete the file from CWD.
                restrictedDelete(fileInCWD);
        }

        // Iterate the file in Branch head to add the files are not there in CWD.
        for (Map.Entry<String, String> entry : branchHead.getFiles().entrySet()) {

            String fileName = entry.getKey();

            // add the newFile if its not existent in CWD.
            if (!cwdFilesMap.containsKey(fileName)) {
                File newFile = new File(CWD, fileName);
                String newFileContent = blobs.get(branchHead.getFileId(fileName)).getContent();
                writeContents(newFile, newFileContent);
            }
        }

        // Update the current branch to the branchName.
        curBranch = branchName;

        // update the file saves the current branch.
        writeObject(CURRENT_BRANCH, curBranch);

        // Clear the staging area.
        stagingArea.clear();
    }

    public void branch(String branchName) {

        validateInitialized();

        // Check if the branch already exists
        File branchFile  = new File(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        // save the new branch in BRANCHES_DIR.
        writeContents(branchFile, head);
    }

    public void rm_branch(String branchName) {

        validateInitialized();
        
        File branchFile  = new File(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (curBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        // delete the branch file from the BRANCHES_DIR.
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
