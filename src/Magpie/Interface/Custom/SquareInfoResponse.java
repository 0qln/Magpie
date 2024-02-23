package Interface.Custom;

import Interface.Response;
import Misc.Utils;

public class SquareInfoResponse extends Response {

    private int _square;
    private String _info;

    public SquareInfoResponse(int square, String info) {
        _square = square;
        _info = info;
    }

    @Override
    protected void executeSend() {
        String square = Utils.fromSquareIndex(_square);
        System.out.println(square + ": " + _info);
    }

}
