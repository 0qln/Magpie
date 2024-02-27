package Interface.UCI;

import Interface.*;
import Misc.Ptr;

public class SetOptionCommand<TOptionValue> extends Command
{
    private String _optionName;
    private TOptionValue _newValue;

    public SetOptionCommand(Ptr<Engine.IBoard> board, String optionName, TOptionValue value)
    {
        super(board);
        _optionName = optionName;
        _newValue = value;
    }

    public void run() {
        Config.getOption(_optionName).ifPresent(
            opt -> opt.setValue(_newValue)
        );
    }
}
