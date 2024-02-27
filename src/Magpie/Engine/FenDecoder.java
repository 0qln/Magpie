package Engine;

public class FenDecoder
{
    public static IFenExecuter decode(String[] tokens) {
        return board -> {
            // Set up pieces
            int squareIdx = 63;
            for (int i = 0; i < tokens[0].length(); i++) {
                // skip slashes
                if (tokens[0].charAt(i) == '/') {
                    continue;
                }

                // handle digit
                if (Character.isDigit(tokens[0].charAt(i))) {
                    squareIdx -= tokens[0].charAt(i) - '0';
                    continue;
                }

                // get piece char as Piece Type
                int piece = Piece.fromChar(tokens[0].charAt(i));

                // evaluate
                board.addPiece(squareIdx ^ 7, piece);

                squareIdx--;
            }

            // turn
            if (tokens[1].contains("w")) {
                board.setTurn(Color.White);
            }
            else {
                board.setTurn(Color.Black);
            }

            // castling
            if (tokens[2].contains("K")) board.setCastlingRights(PieceType.King, Color.White, true);
            if (tokens[2].contains("Q")) board.setCastlingRights(PieceType.Queen, Color.White, true);
            if (tokens[2].contains("k")) board.setCastlingRights(PieceType.King, Color.Black, true);
            if (tokens[2].contains("q")) board.setCastlingRights(PieceType.Queen, Color.Black, true);

            // en passant
            if (!tokens[3].contains("-")) {
                board.setEnpassant(Misc.Utils.toSquareIndex(tokens[3]));
            }

            // plys for 50 move rule
            if (tokens.length > 4) {
                board.setPlys50(Integer.parseInt(tokens[4]));
            }

            if (tokens.length > 5) {
                // TODO? 
                // store move count that the fen gives
            }

        };
    }
}
