package Interface;

import java.util.Arrays;

public class SetOptionCommand extends Command {
    static {
        Signature.register("setoption", SetOptionCommand.class, new Builder<>(() -> new SetOptionCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 4) {
            return false;
        }

        final String name = args[1];
        params_put("name", name);

        Config.getOption(name).ifPresent(option -> 
            option
            .getValueParser()
            .parse(Arrays.copyOfRange(args, 3, args.length))
            .ifPresent(value -> params_put("value", value))
        );

        return true;
    }

    public void run() {
        Config.getOption(params_get("name")).ifPresent(
                opt -> opt.setValue(params_get("value")));
    }
}
