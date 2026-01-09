package primer;

public class Utils {
    public static String revComp(String seq) {
        seq = seq.toUpperCase();
        StringBuilder sb = new StringBuilder(seq.length());
        for (int i = seq.length()-1; i >= 0; --i) {
            char c = seq.charAt(i);
            sb.append(complementIUPAC(c));
        }
        return sb.toString();
    }

    // Complement that handles IUPAC degenerate bases used here (N,K)
    public static char complementIUPAC(char c) {
        switch (c) {
            case 'A': return 'T';
            case 'T': return 'A';
            case 'G': return 'C';
            case 'C': return 'G';
            case 'N': return 'N';
            case 'K': return 'M'; // complement of K(G/T) is M(A/C)
            case 'M': return 'K';
            default: return 'N';
        }
    }

    // Simple Wallace rule Tm = 2*(A+T) + 4*(G+C)
    // Degenerate bases: approximate by treating N as average (A/T/G/C => treat as 2)
    public static double wallaceTm(String seq) {
        seq = seq.toUpperCase();
        int at = 0, gc = 0;
        for (char c : seq.toCharArray()) {
            switch (c) {
                case 'A': case 'T':
                    at++; break;
                case 'G': case 'C':
                    gc++; break;
                case 'N':
                    at += 1; gc += 1; break; // average
                case 'K':
                    // K = G or T => treat as 0.5G + 0.5T: add 0.5 to GC and 0.5 to AT
                    at += 0.5; gc += 0.5; break;
                default:
                    at += 1; gc += 1; break;
            }
        }
        return 2.0 * at + 4.0 * gc;
    }

    public static double gcPercent(String seq) {
        seq = seq.toUpperCase();
        double gc = 0;
        for (char c : seq.toCharArray()) {
            if (c == 'G' || c == 'C') gc++;
            else if (c == 'N') gc += 0.5; // approximate
            else if (c == 'K') gc += 0.5;
        }
        return 100.0 * gc / seq.length();
    }
}