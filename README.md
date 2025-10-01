# Gitlet - A Java-Based Version Control System

**Gitlet is a robust version control system inspired by Git and implemented in Java. It provides comprehensive version control functionality with efficient file tracking, branching, and merging capabilities.**

## Requirements
- Java 17 or higher
- Command line terminal
- File system permissions for repository operations

## How to Run
1. **Compile the source code:**
   ```sh
   javac gitlet/Main.java
   ```
2. **Execute Gitlet commands:**
   ```sh
   java gitlet.Main [command] [arguments]
   ```
   Example:  
   ```sh
   java gitlet.Main init
   ```

---

## Internal Structure
Gitlet simplifies Git's internal structure by using:
- **Blobs**: Each version of a file is saved as a separate blob.
- **Trees**: Directory structures that map file and folder names to their corresponding blobs.
- **Commits**: Snapshots that include commit messages, metadata, references to file trees, and links to parent commits.

Key Differences from Git:
- Using a **flat directory structure** (no subdirectories).
- Supporting only **two-parent merges**.
- Storing only **timestamps and log messages** in metadata.

---

## Commands
### **1. `init`**
- **Usage**:  
  ```sh
  java gitlet.Main init
  ```
- **Description**:  
  Initializes a new Gitlet repository in the current directory.

### **2. `add`**
- **Usage**:  
  ```sh
  java gitlet.Main add [file name]
  ```
- **Description**:  
  Stages the specified file for addition to the next commit.

### **3. `commit`**
- **Usage**:  
  ```sh
  java gitlet.Main commit [message]
  ```
- **Description**:  
  Saves a snapshot of the current staged changes with a descriptive message.

### **4. `rm`**
- **Usage**:  
  ```sh
  java gitlet.Main rm [file name]
  ```
- **Description**:  
  Removes a file from staging or stops tracking the file.

### **5. `log`**
- **Usage**:  
  ```sh
  java gitlet.Main log
  ```
- **Description**:  
  Displays the commit history of the current branch.

### **6. `global-log`**
- **Usage**:  
  ```sh
  java gitlet.Main global-log
  ```
- **Description**:  
  Displays all commits ever made in the repository.

### **7. `find`**
- **Usage**:  
  ```sh
  java gitlet.Main find [commit message]
  ```
- **Description**:  
  Finds and displays all commits with the specified commit message.

### **8. `checkout`**
- **Usages**:
  ```sh
  java gitlet.Main checkout -- [file name]
  java gitlet.Main checkout [commit id] -- [file name]
  java gitlet.Main checkout [branch name]
  ```
- **Description**:  
  Restores files from a commit or branch.

### **9. `branch`**
- **Usage**:  
  ```sh
  java gitlet.Main branch [branch name]
  ```
- **Description**:  
  Creates a new branch with the specified name.

### **10. `rm-branch`**
- **Usage**:  
  ```sh
  java gitlet.Main rm-branch [branch name]
  ```
- **Description**:  
  Deletes the specified branch (cannot delete the current branch).

### **11. `reset`**
- **Usage**:  
  ```sh
  java gitlet.Main reset [commit id]
  ```
- **Description**:  
  Moves the current branch head to the specified commit.

### **12. `merge`**
- **Usage**:  
  ```sh
  java gitlet.Main merge [branch name]
  ```
- **Description**:  
  Merges the specified branch into the current branch.

