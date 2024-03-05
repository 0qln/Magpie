package Interface;

public class GoCommand extends Command {

    static {
        Signature.register("go", GoCommand.class, new Builder<>(() -> new GoCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "searchmoves":
                    // _params.add()
                    break;

                // ...

                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public void run() {
        // execute according to `_params`
    }

}
