package gitlet;

public class Main {

    public static void main(String[] args) {
        // TODO: what if args is empty?

        if (args.length == 0) {
            System.out.println("Please enter a command line argument");
            return;
        }

        Repository repository = new Repository();
        repository.backup();

        String firstArg = args[0];

        switch (firstArg) {

            case "init":
                repository.init();
                break;

            case "add":
                if (args.length == 2) {
                    String fileName = args[1];
                    repository.add(fileName);
                } else
                    System.out.println("Please enter the file name");
                break;

            case "commit":
                if (args.length == 2) {
                    String message = args[1];
                    repository.commit(message);
                } else
                    System.out.println("enter the commit message");
                break;

            case "checkoutFile":
                if (args.length == 2)
                    repository.checkoutBranch(args[1]);
                else if (args.length == 3 && args[1].equals("--"))
                    repository.checkoutFile(args[2]);
                else if (args.length == 4 && args[2].equals("--"))
                    repository.checkoutFile(args[1], args[3]);
                else
                    System.out.println("Unrecognized command");
                break;

            case "removeFile":
                if (args.length == 2) {
                    String fileName = args[1];
                    repository.removeFile(fileName);
                } else
                    System.out.println("enter the file name");
                break;

            case "findCommit":
                if (args.length == 2) {
                    String message = args[1];
                    repository.findCommit(message);
                } else
                    System.out.println("enter the commit message");
                break;

            case "log":
                repository.log();
                break;

            case "global-log":
                repository.global_log();
                break;

            case "createBranch":
                if (args.length == 2) {
                    String branchName = args[1];
                    repository.createBranch(branchName);
                } else
                    System.out.println("enter the createBranch name");
                break;

            case "removeFile-createBranch":
                if (args.length == 2) {
                    String branchName = args[1];
                    repository.removeBranch(branchName);
                } else
                    System.out.println("enter the createBranch name");
                break;

            case "merge":
                if (args.length == 2) {
                    String branchName = args[1];
                    repository.merge(branchName);
                } else
                    System.out.println("enter the branch name");
                break;

            case "reset":
                if (args.length == 2) {
                    String commitID = args[1];
                    repository.reset(commitID);
                } else
                    System.out.println("enter commit ID");
                break;

            default:
                break;

            // TODO: FILL THE REST IN
        }
    }
}
