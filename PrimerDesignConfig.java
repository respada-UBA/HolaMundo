package primer;

import java.util.List;

public class PrimerDesignConfig {
    public String inputSequence; // lower or upper OK, will be uppercased
    public int orfStartZeroBased; // zero-based index into inputSequence where ORF starts
    public int orfEndZeroBased;   // zero-based inclusive orf end (optional; used for bounds)
    public int primerOverhang5 = 12;
    public int primerOverhang3 = 12;
    public String primerNamePrefix = "pr_";
    public String mutationType = "NNK"; // "NNK" or "fixed"
    public boolean scan = true; // if true, scan the whole ORF; otherwise use positionsList
    public List<Integer> positionsList = null; // amino-acid positions (1-based)
    public List<String> mutationsList = null; // for fixed: entries like "K1M" = originalK at pos1 -> M

    public int minPrimerLength = 18; // optional checks
    public int maxPrimerLength = 40;

    public PrimerDesignConfig() {}
}