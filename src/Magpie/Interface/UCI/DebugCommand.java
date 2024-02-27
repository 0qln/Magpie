package Interface.UCI;

import java.util.*;
import Interface.Config;
import Misc.Ptr;

/* UCI::
debug [ on | off ]
    switch the debug mode of the engine on and off.
    In debug mode the engine should send additional infos to the GUI, e.g. with the "info string" command,
    to help debugging, e.g. the commands that the engine has received etc.
    This mode should be switched off by default and this command can be sent
    any time, also when the engine is thinking.
*/
public class DebugCommand extends Command
{
    private boolean _on;
    
    public DebugCommand(Ptr<Engine.IBoard> board, boolean on)
    {
        super(board);
        _on = on;
    }
    
    public static Optional<Boolean> parseValue(String value) {
        switch (value) {
            case "on": return Optional.of(Boolean.valueOf(true));
            case "off": return Optional.of(Boolean.valueOf(false));
            default: return Optional.empty();
        }
    }
    
    public void run() {
        Config.Debug = _on;
    }
}
