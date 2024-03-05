package Interface;

import java.util.*;

import Misc.Ptr;

/* UCI::
debug [ on | off ]
    switch the debug mode of the engine on and off.
    In debug mode the engine should send additional infos to the GUI, e.g. with the "info string" command,
    to help debugging, e.g. the commands that the engine has received etc.
    This mode should be switched off by default and this command can be sent
    any time, also when the engine is thinking.
*/
public class DebugCommand extends Command {
    static {
        Signature.register("debug", DebugCommand.class, new Builder<>(() -> new DebugCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 1) 
            return false;
        params_put("value", args[0].equals("on") ? true : false);
        return true;
    }

    public void run() {
        Config.Debug = params_get("value");
    }
}
