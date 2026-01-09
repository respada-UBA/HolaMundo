package primer;

import java.util.Arrays;
import java.util.List;

public class MainExample {
    public static void main(String[] args) {
        PrimerDesignConfig cfg = new PrimerDesignConfig();
        cfg.inputSequence = "tttaatgacccacgtcaacttgaagaagtcattgaccttcttgaggtatatcatgagaaaaagaatgtgattgaagagaaaattaaagctcgcttcattgcaaataaaaatactgtatttgaatggcttacgtggaatggcttcattattcttggaaatgctttagaatataaaaacaacttcgttattgatgaagagttacaaccagttactcatgccgcaggtaaccagcctgatatggaaattatatatgaagactttattgttcttggtgaagtaacaacttctaagggagcaacccagtttaagatggaatcagaaccagtaacaaggcattatttaaacaagaaaaaagaattagaaaagcaaggagtagagaaagaactatattgtttattcattgcgccagaaatcaataagaatacttttgaggagtttatgaaatacaatattgttcaaaacacaagaattatccctctctcattaaaacagtttaacatgctcctaatggtacagaagaaattaattgaaaaaggaagaaggttatcttcttatgatattaagaatctgatggtctcattatatcgaacaactatagagtgtgaaagaaaatatactcaaattaaagctggtttagaagaaactttaaataattgggttgttgacaaggaggtaaggtttccatggcaccaccaccaccaccac";
        cfg.orfStartZeroBased = 7 + 12; // matches your example; compute before calling
        cfg.orfEndZeroBased = 675 + 12;
        cfg.primerOverhang5 = 12;
        cfg.primerOverhang3 = 12;
        cfg.primerNamePrefix = "pr_";
        cfg.mutationType = "NNK";
        cfg.scan = true;
        // if scan=false you can set cfg.positionsList = Arrays.asList(1,2,3);
        PrimerDesigner designer = new PrimerDesigner(cfg);
        List<Primer> primers = designer.design();
        // Print the first 10 primers as example
        for (int i = 0; i < Math.min(primers.size(), 20); ++i) {
            System.out.println(primers.get(i));
        }
        System.out.println("Designed total primers: " + primers.size());
    }
}