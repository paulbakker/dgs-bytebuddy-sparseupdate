This is an example of how a Bytebuddy agent can be used to proxy input object classes, so that it keeps track of fields being explicitly set.

In the `InputProxyApplication` `main` method a Bytebuddy agent is registered that does the following to the `SearchInput` class:
1. Make it implement the `FieldSetTracker` interface which has a `public boolean isSet(String field)` method.
2. Add a field `fieldsSet` of type `HashMap`.
3. Add a `public boolean isSet(String field)` method that checks the `fieldsSet` map. This is the implementation of the `FieldSetTracker` interface.
4. Proxy all `set*` methods, so that it tracks fields being set in the `fieldsSet` map.

In a data fetcher you can cast an input object to the `FieldSetTracker` interface, and check if a field was explicitly set.

```java
@DgsQuery
public String search(SearchInput searchInput) {

    var titleIsSet = ((FieldSetTracker) searchInput).isSet("title");
    var scoreIsSet = ((FieldSetTracker) searchInput).isSet("score");

    return "Title is set "  + titleIsSet + " and score is set " + scoreIsSet;
}
```

On the latest JDK builds you need to run with `-XX:+EnableDynamicAgentLoading` to allow dynamic agent loading.

