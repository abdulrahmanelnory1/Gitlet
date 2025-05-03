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
     * points to the most recent commit.
     */
    String master;
    /**
     * points to the current commit.
     */
    String head;
    /**
     * represents the added files in staging area before they are commited.
     */
    StagingArea stagingArea;
    /**
     * Data structure(map) represents all branches with the branch name as a key and branch id(latest commit in the branch) as value.
     */
    HashMap<String, String> branches;
    /**
     * Represents the current branch.
     */
    String curBranch;
    /**
     * Tracks whether the Gitlet repository has been initialized.
     */

    public boolean backupPerformed  = false; // to backup GITLET_DIR state each time when the program terminates

<<<<<<< HEAD
=======
    public void mapInitializations(){
        stagingArea = new StagingArea();
        commits = new HashMap<String, Commit>();
        branches = new HashMap<String, String>();
        blobs = new HashMap<String, Blob>();
    }

>>>>>>> master
    public void validateInitialized(){

        if(!GITLET_DIR.exists()){
            System.out.println("GITLET_DIR does not exist");
            System.exit(0);
        }
    }

    public void backup() {

        if (!GITLET_DIR.exists() || backupPerformed)
            return;

<<<<<<< HEAD
        // retrieve the commit files into the commits map
        List<String> commitFiles = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR
        for (String commitFileName : commitFiles) {
=======
        mapInitializations();

        // retrieve the commit files into the commits map
        List<String> commitFileNames = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR
        for (String commitFileName : commitFileNames) {
>>>>>>> master

            // get the commit object from the commit file.
            File commitFile = new File(COMMITS_DIR, commitFileName);
            Commit commit = readObject(commitFile, Commit.class);

            commits.put(commitFileName, commit);
        }

        // retrieve the blobs files into the blobs map
        List<String> blobFiles = Utils.plainFilenamesIn(BLOBS_DIR); // get all the commit file names from the BLOBS_DIR
        for (String blobFileName : blobFiles) {

            // get the commit object from the commit file.
            File blobFile = new File(BLOBS_DIR, blobFileName);
            Blob blob = readObject(blobFile, Blob.class);

            blobs.put(blobFileName, blob);
        }

        // get all the branch files from the BRANCHES_DIR
        List<String> branchNames = Utils.plainFilenamesIn(BRANCHES_DIR); // get all the branch file names from the BRANCHES_DIR
        for (String branchName : branchNames) {

            // get the branch object from the commit file.
            File branchFile = new File(BRANCHES_DIR, branchName);
<<<<<<< HEAD
            Branch branch = readObject(branchFile, Branch.class);

            branches.put(branchName, branch.getId());
=======
            String branchId = readContentsAsString(branchFile);

            branches.put(branchName, branchId);
>>>>>>> master
        }

        master = readContentsAsString(MASTER);
        head = readContentsAsString(HEAD);
        curBranch = readContentsAsString(CURRENT_BRANCH);
<<<<<<< HEAD
        stagingArea = readObject(index, StagingArea.class);
=======
        if(index.exists())
           stagingArea = readObject(index, StagingArea.class);

>>>>>>> master
    }

    public void init() {

        if (GITLET_DIR.exists()) {
            System.out.println("GITLET_DIR already exists");
            return;
        }

        GITLET_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
<<<<<<< HEAD

        stagingArea = new StagingArea();
        commits = new HashMap<String, Commit>();
        branches = new HashMap<String, String>();
        blobs = new HashMap<String, Blob>();

        // create initial commit has no parent and no files
        Commit initialCommit = new Commit("initial commit", null);
        // save the initialCommit object in the file named by its SHA1 id
        File commitFile = new File(COMMITS_DIR, "initialCommitId");
        writeObject(commitFile, initialCommit);

        // Add the initial commit in the commits map with its id as a key.
        String initialCommitId = initialCommit.getId();
        commits.put(initialCommitId, initialCommit);// add the initial commit in the commits map

        // By default the current branch is master.
        curBranch = "master";
        master = initialCommitId;

        //save the curBranch in a file in BRANCHES_DIR.
        File curBranchFile = new File(BRANCHES_DIR, curBranch);
        writeObject(curBranchFile, curBranch);

        // save the head state in HEAD file.
        head = initialCommitId;
        writeObject(HEAD, initialCommitId);

        // update the master to most recent commit.
        writeObject(MASTER, initialCommitId);// save the initial commit (most recent commit) id in the master file

        branches.put(curBranch, initialCommitId);// Add the current branch in the branches map.

=======
        BRANCHES_DIR.mkdirs();

        mapInitializations();

        // create initial commit has no parent and no files
        Commit initialCommit = new Commit("initial commit", null);

        // save the initialCommit object in the COMMITS_DIR and its name is its SHA1 id
        String initialCommitId = initialCommit.getId();
        File commitFile = new File(COMMITS_DIR, initialCommitId);
        writeObject(commitFile, initialCommit);

        commits.put(initialCommitId, initialCommit);// add the initial commit in the commits map

        // By default the current branch is the master.
        curBranch = "master";
        master = initialCommitId;
        writeContents(MASTER, master);// update the master file.

        //save the curBranch in a file in BRANCHES_DIR.
        File curBranchFile = new File(BRANCHES_DIR, curBranch);// access the branch file by the branch name.
        writeContents(curBranchFile, master);// the branch file content is the commit id which the branch references it.
        writeContents(CURRENT_BRANCH, "master");//save the current branch in the CURRENT_BRANCH file.

        branches.put("master", initialCommitId);// Add the master branch in the branches map.

        // save the head state in HEAD file.
        head = initialCommitId;
        writeContents(HEAD, head);
>>>>>>> master
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
<<<<<<< HEAD
        // check if the addedFile version/content is already existent in the last commit blobs
=======
        // check if the addedFile version/content is already existent in the last commit blobs of the current branch(master)
>>>>>>> master
        boolean existsInLastCommit = commits.get(master).containsFile(fileName);

        // add the file if it is not existent in the last commit so it needs to be added.
        if (!existsInLastCommit) {

            // check if there is already blob for the addedFile content
            boolean blobExsits = blobs.containsKey(fileId);

            if (!blobExsits) {
                // create new blob for the new version of the addedFile
                Blob blob = new Blob(addedFileForAddition, fileName);

                // Create a new file for the new blob in BLOBS_DIR
                File blobFile = new File(BLOBS_DIR, blob.getId());
                writeObject(blobFile, blob);

                // add the new blob in blobs set
                blobs.put(blob.getId(), blob);
            }

            stagingArea.addForAddition(fileName, fileId);
            // write the new staged files in index file
            stagingArea.save(index);

<<<<<<< HEAD
        }
=======
        }// else => don`t add it again => do nothing

>>>>>>> master
    }

    public void commit(String message) {

        validateInitialized();

        if (message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
<<<<<<< HEAD
        if (stagingArea.getAddedFiles().isEmpty()) {
=======

        if (stagingArea.getAddedFiles().isEmpty() && stagingArea.getRemovedFiles().isEmpty()) {
>>>>>>> master
            System.out.println("No changes added to the commit.");
            return;
        }

<<<<<<< HEAD
        // get the master SHA1 id
        String parentId = Utils.sha1(master);

        // access the master parent file by It's id and get the parent commit
        File parentFile = new File(COMMITS_DIR, parentId);
        Commit parent = readObject(parentFile, Commit.class);

        // create the new commit with the given message and parent
=======
        // Read the parent commit from COMMITS_DIR
        String parentId = commits.get(head).getId(); // the parent of the newCommit is the head commit.
        File parentFile = new File(COMMITS_DIR, parentId);
        Commit parent = readObject(parentFile, Commit.class);

        // create the new commit with the given message and parent parentId
>>>>>>> master
        Commit newCommit = new Commit(message, parentId);

        // clone the parent files into the new commit
        newCommit.setFiles(parent.getFiles());

        // get the staged files from the staging area
        HashMap<String, String> addedFiles = stagingArea.getAddedFiles();
        HashMap<String, String> removedFiles = stagingArea.getRemovedFiles();

<<<<<<< HEAD
        //get the newCommit files
        HashMap<String, String> newCommitFiles = newCommit.getFiles();

        // add the staged files from the staging area to newCommit
=======
        // add the staged files for adding from the staging area to the newCommit
>>>>>>> master
        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileId = entry.getValue();

            newCommit.addFile(fileName, fileId);
<<<<<<< HEAD
        }
        // remove the files added for removal from the CWD.
        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            newCommit.removeFile(entry.getKey());

            // Remove the file from the CWD.
            File removedFile = new File(CWD, entry.getKey());
            Utils.restrictedDelete(removedFile);
=======

        }
        // remove the files added for removal from the CWD and from the new commit.
        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {

                newCommit.removeFile(entry.getKey());

                // Remove the file from the CWD.
                File removedFile = new File(CWD, entry.getKey());
                Utils.restrictedDelete(removedFile);
>>>>>>> master

            /* TODO: what if it was not deleted ?! */
        }

        String newCommitId = newCommit.getId(); // Find SHA1 hash(id) for the newCommit.
        commits.put(newCommitId, newCommit);// add the new commit in the commits set.

        // save the new commit in a new file in the COMMITS_DIR
        File newCommitFile = new File(COMMITS_DIR, newCommitId);
        writeObject(newCommitFile, newCommit);

<<<<<<< HEAD
        branches.put(curBranch , newCommitId); // update the curBranch id to the new commit id.
        writeObject(CURRENT_BRANCH, curBranch); // Update/Overwrite the file saves the curBranch object.
=======
        branches.put(curBranch , newCommitId); // Update/Overwrite the curBranch id to the new commit id.
        writeContents(CURRENT_BRANCH, curBranch); // Update/Overwrite the file saves the curBranch object.
>>>>>>> master

        stagingArea.clear();// Clear the staging area after commiting the changes.

        stagingArea.save(index);// save the staging area.
<<<<<<< HEAD
=======

        // update the head pointer to reference to the new commit.
        head = newCommitId;
        writeContents(HEAD, head);
>>>>>>> master
    }

    public void rm(String fileMarkedForRemoval_Name) {

        validateInitialized();

        // Unstage the file if it is currently staged for addition.
<<<<<<< HEAD
        if (stagingArea.existentForEdition(fileMarkedForRemoval_Name)) {
            stagingArea.unStage(fileMarkedForRemoval_Name);
        }

        // If the file is tracked in the current commit => stage it for removal from the CWD.
        else if (commits.get(head).containsFile(fileMarkedForRemoval_Name)) {

            File fileMarkedForRemoval = new File(CWD, fileMarkedForRemoval_Name);

            // find SHA1 id for the addedFile
            String fileMarkedForRemoval_Content = Utils.readContentsAsString(fileMarkedForRemoval);
            String fileMarkedForRemoval_Id = Utils.sha1(fileMarkedForRemoval_Content);

            // check if there is already blob for the addedFile content
            if (!blobs.containsKey(fileMarkedForRemoval_Id)) {

                // create new blob for the addedFile
                Blob fileMarkedForRemoval_Blob = new Blob(fileMarkedForRemoval, fileMarkedForRemoval_Name);

                // save the new blob in a new file in BLOBS_DIR
                File blobFile = new File(BLOBS_DIR, fileMarkedForRemoval_Blob.getId());
                writeObject(blobFile, fileMarkedForRemoval_Blob);

                // add the new blob in blobs set
                blobs.put(fileMarkedForRemoval_Blob.getId(), fileMarkedForRemoval_Blob);
            }

            stagingArea.markForRemoval(fileMarkedForRemoval_Name, fileMarkedForRemoval_Id);
            stagingArea.save(index);
=======
        if (stagingArea.existentForAddition(fileMarkedForRemoval_Name)) {
            System.out.println("the file unStaged");
            stagingArea.unStage(fileMarkedForRemoval_Name);
        }

        // If the file is tracked in the current commit => stage it for removal from the head commit and delete it from CWD.
        else if (commits.get(head).containsFile(fileMarkedForRemoval_Name)) {

            // remove the file from CWD if it exists.
            File fileMarkedForRemoval = new File(CWD, fileMarkedForRemoval_Name);
            Utils.restrictedDelete(fileMarkedForRemoval);

            // Mark the file for removal from the current commit.
            String fileForRemoval_Content = readContentsAsString(fileMarkedForRemoval);
            String fileMarkedForRemoval_Id =Utils.sha1(fileForRemoval_Content);
            stagingArea.markForRemoval(fileMarkedForRemoval_Name, fileMarkedForRemoval_Id);
            System.out.println("marked for removal");
            stagingArea.save(index);

>>>>>>> master
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
            String fileId = curCommit.getFile(fileName);

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


                String fileId = commit.getFile(fileName); // get the file from the curCommit.

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
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        // get the branch.
        String branchHeadId = branches.get(branchName);

        // if i am already in the branch branchName => then i go nowhere .
        if (head.equals(branchHeadId)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        // Retrieve all files in the commit at the head of the given branch.
        Commit branchHead = commits.get(branchHeadId);

        // iterate all the files in CWD and if there is a file exists in the branchHead => update it in the CWD, if not => remove it.
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD); // retrieve all the files from the cwd.
        Map<String, Integer> cwdFilesMap = new HashMap<String, Integer>();// create a map for constant time access.

        assert cwdFiles != null;
        for (String fileName : cwdFiles) { // iterate CWD files.

            cwdFilesMap.put(fileName, 0);

            File fileInCWD = new File(CWD, fileName);

            if (branchHead.containsFile(fileName)) { // check if file exists in the branch head commit to be updated to the branchHead version.

                // find the id of the fileInBranchHead
                String fileIdInBranchHead = branchHead.getFile(fileName);

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
                String newFileContent = blobs.get(branchHead.getFile(fileName)).getContent();
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
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }

<<<<<<< HEAD
        Branch newBranch = new Branch(branchName, head); // Create the new branch at the current commit.

        // Save the new branch in a file on desk.
        File newBranchFile = new File(BRANCHES_DIR, branchName);
        writeObject(newBranchFile, newBranch);

        branches.put(branchName, newBranch.getId()); // Track the new branch in the branch map.
=======
        // Save the new branch in a file on desk.
        File newBranchFile = new File(BRANCHES_DIR, branchName);
        writeContents(newBranchFile, head);

        branches.put(branchName, head); // Track the new branch in the branch map.
>>>>>>> master
    }

    public void rm_branch(String branchName) {

        validateInitialized();

        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (curBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        // delete the branch file from the BRANCHES_DIR.
        File branchFile = new File(BRANCHES_DIR, branchName);
        Utils.restrictedDelete(branchFile);

        // delete the file from branches map.
        branches.remove(branchName);
<<<<<<< HEAD

=======
>>>>>>> master
    }

    public void status(){
        //............
    }

    public void reset(){
        //.....
    }

    public void merge(){
        //.....
    }
}
