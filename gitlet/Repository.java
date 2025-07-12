package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

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

    public static final File CURRENT_BRANCH = join(GITLET_DIR, "current branch");
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
     * Tracks whether the Gitlet repository has been initialized.
     */
    public boolean backupPerformed = false;

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

        if (index.exists())// ensure that index file exists cuz it can not be existent if there aren`t files staged before.
            stagingArea = readObject(index, StagingArea.class);
    }

    /**
     * Creates a new Gitlet version-control system in the current directory.
     */
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
        Commit initialCommit = new Commit("initial commit", null, null);
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
        writeContents(CURRENT_BRANCH, "master");// Save the current createBranch which is master in the CURRENT_BRANCH file.
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area
     * java git-let.Main add [file name].
     */
    public void add(String fileName) {

        validateInitialized();

        File addedFileForAddition = new File(CWD, fileName);

        // if there is no such a file in CWD => exit without changing anything.
        if (!addedFileForAddition.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        String head = readContentsAsString(HEAD);

        // If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there.
        String fileContentInCWD = readContentsAsString(addedFileForAddition);

        if (fileContentInCWD.equals(Commit.getCommit(head).getFileId(fileName))) {

            // Remove from staging area
            stagingArea.unStage(fileName);

            // Unstage it for removal.

            return;
        }

        stagingArea.stageForAddition(fileName);
        stagingArea.save();
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so that they can be restored at a later time.
     */
    public void commit(String message) {

        validateInitialized();

        if (stagingArea.getStagedForAddition().isEmpty() && stagingArea.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // Get the parent commit (the current commit)
        String headCommitId = readContentsAsString(HEAD);
        Commit parent = Commit.getCommit(headCommitId);

        // Create new commit with message, and parentId as its parent1 and null for parent2.
        Commit newCommit = new Commit(message, parent.getId(), null);
        newCommit.setFiles(parent.getFiles());

        // Apply added and removed files
        HashMap<String, String> addedFiles = stagingArea.getStagedForAddition();
        HashMap<String, String> removedFiles = stagingArea.getRemovedFiles();

        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            // File name as a key and file id as a value.
            newCommit.addFile(entry.getKey(), entry.getKey());
        }

        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            // File name as a key.
            newCommit.removeFile(entry.getKey());
        }

        // Save new commit
        newCommit.save();

        // Get the id of the new Commit.
        String newCommitId = newCommit.getId();

        // public static final File CURRENT_BRANCH = join(GITLET_DIR, "current createBranch");

        // set the new commit as the most recent commit in the current createBranch.
        writeContents(CURRENT_BRANCH, newCommitId);

        // Update HEAD
        writeContents(HEAD, newCommitId);

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
    public void removeFile(String fileName) {

        Commit headCommit = Commit.getCommit(readContentsAsString(HEAD));

        validateInitialized();
        boolean isTracked = headCommit.containsFile(fileName);
        boolean stagedForAddition = stagingArea.existentForAddition(fileName);

        if ((!stagedForAddition && !isTracked)) {
            System.out.println("No reason to remove the file.");
            return;
        }

        stagingArea.markForRemoval(fileName);
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

    /**
     * Prints out the commit log staring at the current head commit. java gitlet.Main log.
     */
    public void log() {

        validateInitialized();

        Stack<String> s = new Stack<String>();

        String head = readContentsAsString(HEAD);

        s.add(head);
        Set<String> visited = new HashSet<String>();

        while (!s.isEmpty()) {

            String curId = s.pop();

            if (!visited.contains(curId)) {
                Commit curCommit = Commit.getCommit(curId);
                System.out.println(curCommit);

                // push the id of the first parent.
                s.add(curCommit.getParent1());

                String secParent = curCommit.getParent2();

                // push the id of the second parent if it exists.
                if (secParent != null)
                    s.add(secParent);

                visited.add(curId);
            }
        }
    }

    /**
     * displays information about all commits ever made.
     */
    public void global_log() {

        validateInitialized();

        // retrieve the commit files
        List<String> commitFiles = Utils.plainFilenamesIn(COMMITS_DIR); // get all the commit file names from the COMMITS_DIR

        for (String commitId : commitFiles) {

            // get the commit object from the commit file.
            Commit commit = Commit.getCommit(commitId);
            System.out.println(commit);
        }
    }

    /**
     * Takes the version of the file as it exists in the head commit
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one. The new version of the file is not staged.
     */
    public void checkoutFile(String fileName) {

        validateInitialized();

        Commit curCommit = Commit.getCommit(readContentsAsString(HEAD)); // Retrieve the current commit (head commit).

        if (!curCommit.containsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        // get the file from the curCommit.
        String fileId = curCommit.getFileId(fileName);

        String fileContent = Blob.getBlob(fileId).getContent();// get the file content from it's blob.

        // Add the file in CWD if the file doesn't exist or overwrite the file if it exists.
        File fileInCWD = new File(CWD, fileName);
        writeContents(fileInCWD, fileContent);
    }

    /**
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one. The new version of the file is not staged.
     */
    public void checkoutFile(String commitID, String fileName) {

        validateInitialized();

        Commit commit = Commit.getCommit(commitID); // retrieve the commit by its Id.

        if (!Utils.plainFilenamesIn(COMMITS_DIR).contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }

        if (!commit.containsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String fileContent = commit.getFileContent(fileName);

        // Add the file in CWD if the file doesn't exist or overwrite it if it exists.
        File fileInCWD = new File(CWD, fileName);
        writeContents(fileInCWD, fileContent);
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * The [commit id] may be abbreviated as for checkout.
     * The staging area is cleared.
     * The command is essentially checkout of an arbitrary commit
     * that also changes the current branch head.
     */
    public void reset(String commitID) {

        // 1. Check if commit exists
        if (!Utils.plainFilenamesIn(COMMITS_DIR).contains(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit currentCommit = Commit.getCommit(readContentsAsString(HEAD));

        Commit givenCommit = Commit.getCommit(commitID);
        HashMap<String, String> givenCommitFiles = givenCommit.getFiles();

        List<String> CWDFiles = Utils.plainFilenamesIn(CWD); // get all the commit file names from the COMMITS_DIR

        for (String fileName : CWDFiles) {
            if (givenCommit.containsFile(fileName)
                    && !currentCommit.containsFile(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        // Checks out all the files tracked by the given commit.
        for (String fileNameInCommitToReset : givenCommitFiles.keySet()) {
            checkoutFile(commitID, fileNameInCommitToReset);
        }

        // Removes tracked files that are not present in that commit.
        for (String fileName : CWDFiles) {
            if (currentCommit.containsFile(fileName) && !givenCommit.containsFile(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }

        String givenCommitID = givenCommit.getId();

        writeContents(HEAD, givenCommitID);
        writeContents(CURRENT_BRANCH, givenCommitID);

        stagingArea.clear();
        stagingArea.save();

    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files
     * that are already there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted. The staging area is cleared,
     * unless the checked-out branch is the current branch.
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

            if (!branchHeadCommit.containsFile(fileName))
                restrictedDelete(fileName);

        }

        // Update the current branch file to the checked out branch.
        writeObject(CURRENT_BRANCH, branchName);

        // Clear the staging area.
        stagingArea.clear();
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * Like real Git, This command does NOT immediately switch to the newly created branch.
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
     * Delete Branch with the given name.
     * The deleted createBranch can not be the curren branch.
     * Be sure that The deleted createBranch exists before deletion.
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

    public void merge(String branchName) {

        validateInitialized();

        String curBranchId = readContentsAsString(HEAD); // current branch (head commit of the current branch)
        Commit curBranchHeadCommit = Commit.getCommit(curBranchId);
        File otherBranchFile = new File(BRANCHES_DIR, branchName);
        String otherBranchID = readContentsAsString(otherBranchFile);
        Commit otherBranchHeadCommit = Commit.getCommit(otherBranchID);
        String splitPoint = findSplitPoint(curBranchId, otherBranchID);
        Commit splitPointCommit = Commit.getCommit(splitPoint);

        /* Failure  cases :- */

        // Failure case 1- The given branch does ont exist
        if (!otherBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        // Failure case 2- The given branch is the current branch.
        String curBranchName = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(curBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // Failure case 3- the split point is the head commit of the current branch
        if (splitPoint.equals(curBranchId)) {
            System.out.println("Current branch fast-forwarded.");
        }

        // Failure case 4- The split point is the given branch head.
        if (splitPoint.equals(otherBranchID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        }

        if (!stagingArea.getRemovedFiles().isEmpty() || !stagingArea.getStagedForAddition().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        boolean conflicted = false;

        Set<String> allFiles = new HashSet<String>();

        allFiles.addAll(curBranchHeadCommit.getFiles().keySet());
        allFiles.addAll(otherBranchHeadCommit.getFiles().keySet());
        allFiles.addAll(splitPointCommit.getFiles().keySet());


        for (String fileName : allFiles) {

            String fileContentInCurBranch = curBranchHeadCommit.getFileContent(fileName);
            String fileContentInOtherBranch = otherBranchHeadCommit.getFileContent(fileName);
            String fileContentInSplitPoint = splitPointCommit.getFileContent(fileName);

            boolean fileExistsInCurBranch = curBranchHeadCommit.containsFile(fileName);
            boolean fileExistsInOtherBranch = otherBranchHeadCommit.containsFile(fileName);
            boolean fileExistsInSplitPoint = splitPointCommit.containsFile(fileName);

            /* Case 1- Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch. */
            if (fileExistsInSplitPoint && fileExistsInOtherBranch && fileExistsInCurBranch) {

                boolean fileModifiedInOtherBranch = !fileContentInOtherBranch.equals(fileContentInSplitPoint);
                boolean fileModifiedInCurBranch = !fileContentInCurBranch.equals(fileContentInSplitPoint);

                if (fileModifiedInOtherBranch && !fileModifiedInCurBranch) {

                    File fileInCWD = new File(CWD, fileName);
                    writeContents(fileInCWD, fileContentInOtherBranch);

                    stagingArea.stageForAddition(fileName);
                    stagingArea.save();
                }

                /* Case 2- Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are. */
                if (!fileModifiedInOtherBranch && fileModifiedInCurBranch) {

                    // Do nothing.
                }
            }
            /* Case 3- Any files that have been modified in both the current and given branch in the same way (both files now have the same content or were both removed) are left unchanged by the merge.*/

            // Both files were removed.
            if (fileExistsInSplitPoint && !fileExistsInCurBranch && !fileExistsInOtherBranch)
                // Ensure the file is unstaged.
                stagingArea.unStage(fileName);

            // Both files are not removed, but changed and have the same content.
            if (fileExistsInSplitPoint && fileExistsInCurBranch && fileExistsInOtherBranch) {

                boolean fileModifiedInOtherBranch = !fileContentInOtherBranch.equals(fileContentInSplitPoint);
                boolean fileModifiedInCurBranch = !fileContentInCurBranch.equals(fileContentInSplitPoint);

                if (fileModifiedInOtherBranch && fileModifiedInCurBranch)
                    // Ensure the file is unstaged.
                    stagingArea.unStage(fileName);
            }

            /* case 4- Any files that were not present at the split point and are present only in the current branch should remain as they are. */
            // Do nothing , file remains as they are.

            /* Case 5- Any files that were not present at the split point and are present only in the given branch should be checked out and staged. */
            if (!fileExistsInSplitPoint && !fileExistsInCurBranch && fileExistsInOtherBranch) {

                // Add the file if it does not exist in CWD, Overwrite the file in CWD if it exists.
                File fileInCWD = new File(CWD, fileName);
                writeContents(fileInCWD, fileContentInOtherBranch);

                // Stage changes.
                stagingArea.stageForAddition(fileName);
                stagingArea.save();
            }

            /* Case 6- Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).*/

            if (fileExistsInSplitPoint && !fileExistsInOtherBranch && fileExistsInCurBranch) {

                if (!fileContentInCurBranch.equals(fileContentInSplitPoint)) {

                    // stage it for removal
                    stagingArea.markForRemoval(fileName);
                    stagingArea.save();
                }
            }

            /* Case 7- Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.*/
            // Do nothing.

            /* Case 8- Any files modified in different ways in the current and given branches are in conflict. the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches. In this case, replace the contents of the conflicted file with */

            boolean fileHasDifferentContents = !fileContentInCurBranch.equals(fileContentInOtherBranch);
            if ((!fileExistsInSplitPoint &&
                    fileExistsInOtherBranch && fileExistsInCurBranch && fileHasDifferentContents) ||
                    (fileExistsInSplitPoint &&
                            (fileExistsInOtherBranch && !fileExistsInCurBranch) ||
                            (!fileExistsInOtherBranch && fileExistsInCurBranch) ||
                            (fileExistsInOtherBranch && fileExistsInCurBranch && !fileHasDifferentContents))) {

                if (!fileContentInCurBranch.endsWith("\n"))
                    fileContentInCurBranch = fileContentInCurBranch + "\n";
                if (!fileContentInOtherBranch.endsWith("\n"))
                    fileContentInOtherBranch = fileContentInOtherBranch + "\n";

                String mergedFileContent = "<<<<<<< HEAD\n" + fileContentInCurBranch + "=======" + fileContentInOtherBranch + ">>>>>>>";

                File fileInCWD = new File(CWD, fileName);
                writeContents(fileInCWD, mergedFileContent);

                stagingArea.stageForAddition(fileName);
                stagingArea.save();

                conflicted = true;
            }
        }

        commitWithMerge(otherBranchID);
        if (conflicted)
            System.out.println("Encountered a merge conflict.");
    }

    /**
     * just like commit, but it is for commits with two parents.
     */
    private void commitWithMerge(String otherParentId) {

        if (stagingArea.getStagedForAddition().isEmpty() && stagingArea.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        String head = readContentsAsString(HEAD);

        Commit mergeCommit = new Commit("Merged " + otherParentId + " into " + head + ".", head, otherParentId);

        mergeCommit.setFiles(Commit.getCommit(head).getFiles());

        // Apply added and removed files
        HashMap<String, String> addedFiles = stagingArea.getStagedForAddition();
        HashMap<String, String> removedFiles = stagingArea.getRemovedFiles();

        for (Map.Entry<String, String> entry : addedFiles.entrySet()) {
            // File name as a key and file id as a value.
            mergeCommit.addFile(entry.getKey(), entry.getKey());
        }

        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            // File name as a key.
            mergeCommit.removeFile(entry.getKey());
        }

        // Save the commit
        mergeCommit.save();

        // update the head of the current branch to the merge commit.
        writeContents(CURRENT_BRANCH, mergeCommit.getId());

        // Update HEAD
        writeContents(HEAD, mergeCommit.getId());

        // Clear and save the staging area
        stagingArea.clear();
        stagingArea.save();
    }

    private String findSplitPoint(String branch1HeadID, String branch2HeadID) {

        Queue<String> IDs = new LinkedList<String>();

        HashMap<String, Integer> visited = new HashMap<String, Integer>();
        IDs.add(branch1HeadID);
        IDs.add(branch2HeadID);

        while (!IDs.isEmpty()) {

            String commitID = IDs.poll();
            String parentId = Commit.getCommit(commitID).getParent1();

            if (visited.containsKey(commitID))
                return commitID;

            // the split point is the first commit is going to be added twice because it is common between the two branches.
            visited.put(commitID, 1); // Marked as visited

            if (parentId != null)
                IDs.add(parentId);
        }

        // Should never happen.
        return null;
    }
}
