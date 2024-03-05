package Engine;

import static Engine.Utils.*;

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

        list._moveCount = Pawn.generator.generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Knight.generator.generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Rook.generator.generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Bishop.generator.generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Queen.generator.generate(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = King.generator.generate(list._moves, list._moveCount, board, board.getTurn());

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

        list._moveCount = Pawn.generator.resolves(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Knight.generator.resolves(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Rook.generator.resolves(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Bishop.generator.resolves(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = Queen.generator.resolves(list._moves, list._moveCount, board, board.getTurn());
        list._moveCount = King.generator.resolves(list._moves, list._moveCount, board, board.getTurn());

        return list;
    }

    /**
     * @param board
     * @return If the stm king is in double check, all pseudo legal moves that the
     *         king can do.
     */
    public static MoveList checkResolvesByKing(Board board) {
        MoveList list = new MoveList();

        list._moveCount = King.generator.resolves(list._moves, list._moveCount, board, board.getTurn());

        return list;
    }

    public static MoveList legal(Board board) {
        MoveList candidates, list = new MoveList();

        // 1. Generate pseudo legal moves.

        // // temporary
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
            final int movingP = board.getPieceID(from);
            final int us = board.getTurn(), nus = Color.NOT(us);
            final long kingBB = board.getBitboard(PieceType.King, us);
            final long nstm = board.getNstmAttacks();

            // 1. En passant
            if (flag == Move.EN_PASSANT_FLAG) {
                final int kingSquare = lsb(kingBB);
                final int captureSquare = to + (us * 2 - 1) * 8;
                final long occupationAfterEpCapture = (
                    (board.getOccupancy() ^ target(from) ^ target(captureSquare)) | target(to)
                );

                // From perspective of the king, check if he is attacked
                if (
                    (Rook.generator.attacks(kingSquare, occupationAfterEpCapture) & (board.getBitboard(PieceType.Rook, nus) |
                                                                                     board.getBitboard(PieceType.Queen, nus)))  != 0
                    ||
                    (Bishop.generator.attacks(kingSquare, occupationAfterEpCapture) & (board.getBitboard(PieceType.Bishop, nus) | 
                                                                                       board.getBitboard(PieceType.Queen, nus))) != 0
                )
                {
                    // King is exposed after en passant capture
                    continue;
                }
            }

            // 2. Castling through check is not legal
            if (flag == Move.KING_CASTLE_FLAG) {
                if (!Castling.hasNerve(Castling.KingSide, us, nstm)) {
                    continue;
                }
            }
            if (flag == Move.QUEEN_CASTLE_FLAG) {
                if (!Castling.hasNerve(Castling.QueenSide, us, nstm)) {
                    continue;
                }
            }

            // 3. King moving into a check is not legal
            if (PieceUtil.getType(movingP) == PieceType.King) {
                // When the king has moved and a sliding piece was a checker, the attacks of
                // that sliding piece will have changed
                final long occupationAfterKingMove = board.getOccupancy() ^ kingBB | target(to);
                // Only sliding piece attacks will have changed if the king has moved 
                long updatedNstm = nstm;
                long[] checkers = { board.getCheckers() };
                while (checkers[0] != 0) {
                    int square = popLsb(checkers);
                    Piece.MoveGenerator gen = Piece.fromID(board.getPieceID(square)).getGenerator();
                    if (gen instanceof SlidingPiece.MoveGenerator)
                        updatedNstm |= ((SlidingPiece.MoveGenerator)gen).attacks(square, occupationAfterKingMove);
                }

                // printBB(updatedNstm);

                if ((target(to) & updatedNstm) != 0) {
                    // King has moved into a check
                    continue;
                }
            }

            // 4. [ Non king move ] Pinned pieces movements are restricted
            else {
                // Is piece a blocker of a check? -> pinned!
                if ((board.getBlockers() & target(from)) != 0) {
                    // If the piece is pinned, it can only move along the ray of it's pin.
                    if ((Masks.ray(from, to) & kingBB) == 0) {
                        // The destination square of the pinned piece is not on the ray of
                        // it's pin and it's king.
                        continue;
                    }
                }
            }

            // No checks have failed, we can presume the move is legal.
            list._moves[list._moveCount++] = candidates._moves[i];
        }

        return list;
    }

    public short[] getMoves() {
        // TODO? Remove overhead caused by copying
        return Arrays.copyOfRange(_moves, 0, _moveCount);
    }

    public short get(int index) {
        return _moves[index];
    }

    public int length() {
        return _moveCount;
    }

    public void pushSort() {
        // partially sort this to get the best move to the top
    }

    public void sort(int[] keys) {
        // sort the whole list

    }
}
