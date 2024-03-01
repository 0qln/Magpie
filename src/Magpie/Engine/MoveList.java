package Engine;

import static Engine.Utils.target;

import java.util.Arrays;

public class MoveList {
    private final short[] _moves = new short[256];
    private int _moveCount = 0;

    /**
     * @param board
     * @return All pseudo legal moves.
     */
    public static MoveList pseudoLegal(Board board) {
        MoveList list = new MoveList();

        list._moveCount = new PawnMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = new KnightMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = new RookMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = new BishopMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = new QueenMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = new KingMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());

        return list;
    }

    /**
     * 
     * @param board
     * @return If the stm king is in check, all pseudo legal moves that will resolve
     *         that check.
     */
    public static MoveList checkResolves(Board board) {
        MoveList list = new MoveList();

        return list;
    }

    /**
     * @param board
     * @return If the stm king is in double check, all pseudo legal moves that the
     *         king can do.
     */
    public static MoveList checkResolvesByKing(Board board) {
        MoveList list = new MoveList();

        list._moveCount = new KingMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());

        return list;
    }

    public static MoveList legal(Board board) {
        MoveList candidates, list = new MoveList();

        // 1. Generate pseudo legal moves.

        // temporary
        // candidates = pseudoLegal(board);

        // 1.1 If in check, generate only pseudo legal moves that resolve the check
        if (board.isInSingleCheck()) {
            candidates = checkResolves(board);
        }
        // 1.2 If in double check, generate only pseudo legal king moves
        else if (board.isInDoubleCheck()) {
            candidates = checkResolvesByKing(board);
        }
        // 1.3 Generate all pseudo legal quiet moves and captures
        else {
            candidates = pseudoLegal(board);
        }

        // 2. Filter out pseudo legal moves that are not legal.
        for (int i = 0; i < candidates._moveCount; i++) {
            final short move = candidates._moves[i];
            final int from = Move.getFrom(move), to = Move.getTo(move), flag = Move.getFlag(move);
            final int movingP = board.getPiece(to);
            final long kingBB = board.getBitboard(PieceType.King, board.getTurn());
            final long nstm = board.getNstmAttacks();

            // 2.1 Castling through check is not legal
            if (flag == Move.KING_CASTLE_FLAG) {
                if ((nstm & 0x60) != 0) {
                    // continue;
                }
            }
            if (flag == Move.QUEEN_CASTLE_FLAG) {
                if ((nstm & 0xC) != 0) {
                    // continue;
                }
            }

            // 2.1 King moving into a check is not legal
            if (Piece.getType(movingP) == PieceType.King
                    && (target(to) & nstm) != 0) {
                // continue;
            }

            list._moves[list._moveCount++] = candidates._moves[i];
        }

        return list;
    }

    public short[] getMoves() {
        // TODO? Remove overhead caused by copying
        return Arrays.copyOfRange(_moves, 0, _moveCount);
    }
}
