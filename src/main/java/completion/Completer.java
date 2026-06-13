package completion;

import completion.providers.CompletionProvider;
import java.util.ArrayList;
import java.util.Set;

public class Completer {

    // CommandNameProvider
    // DirectoryProvider
    // FileProvider
    // ExternalCommandProvider
    /*
    if matches is empty:
        beep

    if matches has one item:
        complete to that full item

    if matches has many items:
        complete only up to the longest shared prefix


        refix: "ex"
          candidates: exit, export
          matches: exit, export
          shared prefix: ex
          result: cannot add anything useful
        */
    ArrayList<CompletionProvider> providers;

    public Completer(ArrayList<CompletionProvider> providers) {
        this.providers = providers;
    }

    public String complete(String buffer, String currentDir) {
        return "";
    }
}
