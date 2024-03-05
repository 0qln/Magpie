package Interface;

import Misc.Utils;

public class SquareInfoResponse extends TextResponse {

    public SquareInfoResponse(int square, String info) {
        super(square + ": " + info);
    }

}
