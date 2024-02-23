package UCI;

public class UciSetOptionCommand<TOptionValue> extends UciCommand
{
    private String _optionName;
    private TOptionValue _newValue;

    public UciSetOptionCommand(Engine.IBoard board, String optionName, TOptionValue value)
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
