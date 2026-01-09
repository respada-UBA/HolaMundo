package primer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrimerDesigner {

    private final PrimerDesignConfig cfg;

    public PrimerDesigner(PrimerDesignConfig cfg) {
        this.cfg = cfg;
        if (this.cfg.inputSequence != null) this.cfg.inputSequence = this.cfg.inputSequence.toUpperCase().replaceAll("\\s+", "");
    }

    // Main entry: return list of primers generated
    public List<Primer> design() {
        if ("NNK".equalsIgnoreCase(cfg.mutationType)) {
            if (cfg.scan) {
                int aaCount = computeORFAACount();
                List<Integer> posList = new ArrayList<>();
                for (int i = 1; i <= aaCount; ++i) posList.add(i);
                return designNNKForPositions(posList);
            } else {
                if (cfg.positionsList == null) throw new IllegalArgumentException("positionsList required when scan=false");
                return designNNKForPositions(cfg.positionsList);
            }
        } else if ("fixed".equalsIgnoreCase(cfg.mutationType)) {
            if (cfg.mutationsList == null) throw new IllegalArgumentException("mutationsList required for fixed mutations");
            return designFixedMutations(cfg.mutationsList);
        } else {
            throw new IllegalArgumentException("Unknown mutationType: " + cfg.mutationType);
        }
    }

    // Compute how many AA codons fit inside ORF bounds
    private int computeORFAACount() {
        int end = (cfg.orfEndZeroBased > 0 ? cfg.orfEndZeroBased : cfg.inputSequence.length()-1);
        int len = end - cfg.orfStartZeroBased + 1;
        return Math.max(0, len / 3);
    }

    // For NNK: create degenerate primer pair for each AA position in positions
    private List<Primer> designNNKForPositions(List<Integer> positions) {
        List<Primer> out = new ArrayList<>();
        for (int aaPos : positions) {
            int codonNucIndex = cfg.orfStartZeroBased + (aaPos - 1) * 3; // 0-based
            if (codonNucIndex < 0 || codonNucIndex + 3 > cfg.inputSequence.length()) continue;
            String left = substringSafe(cfg.inputSequence, codonNucIndex - cfg.primerOverhang5, codonNucIndex);
            String right = substringSafe(cfg.inputSequence, codonNucIndex + 3, codonNucIndex + 3 + cfg.primerOverhang3);
            String mutatedCodon = "NNK";
            String fwdPrimer = left + mutatedCodon + right;
            String revPrimer = Utils.revComp(fwdPrimer); // reverse primer = reverse-complement of forward oligo
            String fwdName = String.format("%s%03d_F", cfg.primerNamePrefix, aaPos);
            String revName = String.format("%s%03d_R", cfg.primerNamePrefix, aaPos);
            out.add(new Primer(fwdName, fwdPrimer, Primer.Direction.FORWARD, aaPos));
            out.add(new Primer(revName, revPrimer, Primer.Direction.REVERSE, aaPos));
        }
        return out;
    }

    // For fixed mutations: expect strings like "K1M" meaning originalAA position newAA
    // We'll parse position as digits in the middle and pick a representative codon for the new AA.
    private List<Primer> designFixedMutations(List<String> mutList) {
        List<Primer> out = new ArrayList<>();
        Pattern p = Pattern.compile("^([A-Za-z])(\\d+)([A-Za-z])$");
        for (String m : mutList) {
            Matcher mat = p.matcher(m.trim());
            if (!mat.matches()) continue;
            String orig = mat.group(1);
            int aaPos = Integer.parseInt(mat.group(2));
            String newAA = mat.group(3);
            int codonNucIndex = cfg.orfStartZeroBased + (aaPos - 1) * 3;
            if (codonNucIndex < 0 || codonNucIndex + 3 > cfg.inputSequence.length()) continue;
            // naive mapping newAA -> codon: pick one codon (here we pick a common codon; you may supply a codon table)
            String codon = aaToCodon(newAA.charAt(0));
            String left = substringSafe(cfg.inputSequence, codonNucIndex - cfg.primerOverhang5, codonNucIndex);
            String right = substringSafe(cfg.inputSequence, codonNucIndex + 3, codonNucIndex + 3 + cfg.primerOverhang3);
            String fwdPrimer = left + codon + right;
            String revPrimer = Utils.revComp(fwdPrimer);
            out.add(new Primer(cfg.primerNamePrefix + aaPos + "_" + orig + "to" + newAA + "_F", fwdPrimer, Primer.Direction.FORWARD, aaPos));
            out.add(new Primer(cfg.primerNamePrefix + aaPos + "_" + orig + "to" + newAA + "_R", revPrimer, Primer.Direction.REVERSE, aaPos));
        }
        return out;
    }

    // Very simple codon table mapping (single codon per AA). Replace or extend with full codon table if necessary.
    private String aaToCodon(char aa) {
        switch (Character.toUpperCase(aa)) {
            case 'A': return "GCT";
            case 'R': return "CGT";
            case 'N': return "AAT";
            case 'D': return "GAT";
            case 'C': return "TGT";
            case 'Q': return "CAA";
            case 'E': return "GAA";
            case 'G': return "GGT";
            case 'H': return "CAT";
            case 'I': return "ATT";
            case 'L': return "CTG";
            case 'K': return "AAA";
            case 'M': return "ATG";
            case 'F': return "TTT";
            case 'P': return "CCT";
            case 'S': return "TCT";
            case 'T': return "ACT";
            case 'W': return "TGG";
            case 'Y': return "TAT";
            case 'V': return "GTT";
            default: return "NNN";
        }
    }

    private String substringSafe(String s, int startInclusive, int endExclusive) {
        if (s == null) return "";
        if (startInclusive < 0) startInclusive = 0;
        if (endExclusive > s.length()) endExclusive = s.length();
        if (startInclusive >= endExclusive) return "";
        return s.substring(startInclusive, endExclusive);
    }
}