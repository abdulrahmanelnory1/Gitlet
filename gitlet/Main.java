package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
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

            case "checkout":
                if (args.length == 2)
                    repository.checkoutBranch(args[1]);
                else if (args.length == 3 && args[1].equals("--"))
                    repository.checkout(args[2]);
                else if (args.length == 4 && args[2].equals("--"))
                    repository.checkout(args[1], args[3]);
                else
                    System.out.println("Unrecognized command");
                break;

            case "rm":
                if (args.length == 2) {
                    String fileName = args[1];
                    repository.rm(fileName);
                } else
                    System.out.println("enter the file name");
                break;

            case "find":
                if (args.length == 2) {
                    String message = args[1];
                    repository.find(message);
                } else
                    System.out.println("enter the commit message");
                break;

            case "log":
                repository.log();
                break;

            case "global-log":
                repository.global_log();
                break;

            case "branch":
                if (args.length == 2) {
                    String branchName = args[1];
                    repository.branch(branchName);
                } else
                    System.out.println("enter the branch name");
                break;

            case "rm-branch":
                if (args.length == 2) {
                    String branchName = args[1];
                    repository.rm_branch(branchName);
                } else
                    System.out.println("enter the branch name");
                break;


            default:break;

            // TODO: FILL THE REST IN
        }
    }
}
