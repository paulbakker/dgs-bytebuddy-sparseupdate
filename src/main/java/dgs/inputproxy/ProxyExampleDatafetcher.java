package dgs.inputproxy;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import dgs.inputproxy.codegen.types.SearchInput;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class ProxyExampleDatafetcher {
    @DgsQuery
    public String search(SearchInput searchInput, DataFetchingEnvironment dfe) {

        var titleIsSet = ((FieldSetTracker) searchInput).isSet("title");
        var scoreIsSet = ((FieldSetTracker) searchInput).isSet("score");

        return "Title is set "  + titleIsSet + " and score is set " + scoreIsSet;

//        return "test";
    }
}