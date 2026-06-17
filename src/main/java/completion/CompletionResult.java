package completion;

import java.util.ArrayList;

/*CompletionResult
  status:
    NO_MATCH
    SINGLE_MATCH
    AMBIGUOUS
    PARTIAL_COMMON_PREFIX

  replacementStart
  replacementEnd
  replacementText
  candidates
  shouldAppendSpace
*/
public class CompletionResult {

    ArrayList<String> candidates;

    public ArrayList<String> getCandidates() {
        return candidates;
    }

    public CompletionResult(ArrayList<String> candidates) {
        this.candidates = candidates;
    }
}
