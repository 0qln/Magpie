package Interface.UCI;

import Interface.*;

public class SetOptionCommand<TOptionValue> extends Command
{
    private String _optionName;
    private TOptionValue _newValue;

    public SetOptionCommand(Engine.IBoard board, String optionName, TOptionValue value)
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
