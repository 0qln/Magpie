package Engine;

import static Engine.Utils.*;

public class Bishop extends SlidingPiece {

    public static final MoveGenerator generator = new Bishop.MoveGenerator();
    public static final int ID_Type = 3;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator implements IMoveLookup {
        
        /*
         * Generate in chunks
         * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
         */

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = captures(list, index, board, color);
            if (!capturesOnly)
                index = quiets(list, index, board, color);
            return index;
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(King.ID_Type, color));
            final long mask = Masks.squaresBetween(king, checker);
            // quiet
            if (!capturesOnly)
                index = generate(
                        list,
                        index,
                        ~board.getOccupancy() & mask,
                        board.getOccupancy(),
                        board.getBitboard(Bishop.ID_Type, color),
                        Move.QUIET_MOVE_FLAG);
            // captures
            return generate(
                    list,
                    index,
                    checkerBB,
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        public int quiets(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    ~board.getOccupancy(),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getOccupancy(),
                    board.getBitboard(Bishop.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public long attacks(int square, int color) {
            return attacks(square);
        }

        @Override
        public long attacks(int square, long occupied, int color) {
            return attacks(square, occupied);
        }

        @Override
        public long attacks(int square) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            return (a1h8BB | a8h1BB) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            var lookup = lookupAttacks(square, occupied);
            return lookup;
        }
        

        // https://www.chessprogramming.org/Magic_Bitboards
        
        private static final int[] _magicBits = {
          6, 5, 5, 5, 5, 5, 5, 6,
          5, 5, 5, 5, 5, 5, 5, 5,
          5, 5, 7, 7, 7, 7, 5, 5,
          5, 5, 7, 9, 9, 7, 5, 5,
          5, 5, 7, 9, 9, 7, 5, 5,
          5, 5, 7, 7, 7, 7, 5, 5,
          5, 5, 5, 5, 5, 5, 5, 5,
          6, 5, 5, 5, 5, 5, 5, 6
        };


        private static final long[] _magics = new long[] { 4538793193381920L, -9144275912662953856L, 2884601741927256073L, 91202292648247328L, 2450257539600826433L, -4467070509300840320L, 4596800715495425L, 1477359503105071136L, 4573981298493836L, 39409822138941952L, 651491430112436277L, -5764598546542097152L, 18018818099093760L, 4755822104204543104L, 432382538571320321L, 5188430446360578608L, 22588393205072960L, 9078036821524678L, -9194098553373064680L, 5633906339611136L, 140789038450714L, 4684307193913281025L, 637721060581408L, 2286986375727104L, 721706307325792256L, -9218864039146814944L, 4648928676300784131L, -4006936476779413486L, 291890031969189889L, 1225137979702403072L, 11260099122456960L, 1874659628780945536L, 780761839968256L, 218428988698462208L, 288799528038041608L, 1224981299833864320L, 936964502725067008L, 81078008907079808L, 292736177487876737L, 578713106704795136L, 158398528622596L, 36259703587672064L, 1157443830308538368L, 40533086273886208L, 585470153299330050L, 9016064144835136L, 2379616121932743682L, -9078532233031843328L, -8628325116483860984L, 4612297578846896642L, 141565364568064L, 19394723968L, 1409643165384708L, 4791865196518445056L, 306295360823959560L, 37016168191535744L, 590286320768139936L, 1261186019550765824L, 2458967600570863632L, 324558257526278147L, -9205287252152679936L, -9221084891318222336L, 4922445525405499808L, 4612673414363383892L, };
        private static final long[][] _attacks = new long[64][];


        @Override
        public long[] getMagics() {
            return _magics;
        }

        @Override
        public int[] getMagicBits() {
            return _magicBits;
        }

        @Override
        public long[][] getAttacks() {
            return _attacks;
        }


        public static void Initialize() {
            SlidingPiece.MoveGenerator.Initialize(Bishop.generator, Bishop.generator);
        }
        
        public long lookupAttacks(int square, long occupied) {
            // Get the magic key.
            int key = getKey(occupied & relevantOccupancy(square), _magics[square], _magicBits[square]);

            // Return the lookup.
            return _attacks[square][key];
        }

        public long relevantOccupancy(int square) {
            long relevantOccupancy =  
                // Pieces that aren't in the bishops diagonals aren't relevant.
                attacks(square) 
                // Pieces that are on the outer edges aren't relevant.
                & Masks.RelevantOccupancy;

            return relevantOccupancy;
        }

        public long computeAttacks(int square, long occupied) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            final long nortBB = Utils.splitBBNorth(square), soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South East
            ray = a8h1BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoEa) & ray;
            result |= moves;

            // South West
            ray = a1h8BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoWe) & ray;
            result |= moves;

            // North West
            ray = a8h1BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoWe) & ray;
            result |= moves;

            // North East
            ray = a1h8BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoEa) & ray;
            result |= moves;

            return result;
        }

    }

}
