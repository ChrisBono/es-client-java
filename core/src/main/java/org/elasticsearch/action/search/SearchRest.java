package org.elasticsearch.action.search;

import com.bazaarvoice.elasticsearch.client.core.spi.HttpExecutor;
import com.bazaarvoice.elasticsearch.client.core.spi.HttpResponse;
import com.bazaarvoice.elasticsearch.client.core.util.InputStreams;
import com.bazaarvoice.elasticsearch.client.core.util.UrlBuilder;
import org.elasticsearch.action.AbstractRestClientAction;
import org.elasticsearch.action.XContentResponseTransform;
import org.elasticsearch.common.base.Function;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.util.concurrent.Futures;
import org.elasticsearch.common.util.concurrent.ListenableFuture;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.bazaarvoice.elasticsearch.client.core.util.StringFunctions.booleanToString;
import static com.bazaarvoice.elasticsearch.client.core.util.StringFunctions.scrollToString;
import static com.bazaarvoice.elasticsearch.client.core.util.StringFunctions.searchTypeToString;
import static org.elasticsearch.common.base.Optional.fromNullable;

/**
 * The inverse of {@link org.elasticsearch.rest.action.search.RestSearchAction}
 */
public class SearchRest<T> extends AbstractRestClientAction<SearchRequest, SearchResponse> {
    public SearchRest(final String protocol, final String host, final int port, final HttpExecutor executor, final Function<HttpResponse, SearchResponse> responseTransform) {
        super(protocol, host, port, executor, responseTransform);
    }

    @Override public ListenableFuture<SearchResponse> act(final SearchRequest request) {
        UrlBuilder url = UrlBuilder.create().protocol(protocol).host(host).port(port);

        if (request.indices() == null || request.indices().length == 0) {
            url = url.path("_search");
        } else {
            String indices = Joiner.on(',').skipNulls().join(request.indices());
            if (request.types() == null || request.types().length == 0) {
                url = url.path(indices, "_search");
            } else if (request.types() == null || request.types().length == 0) {
                String types = Joiner.on(',').skipNulls().join(request.types());
                url = url.path(indices, types, "_search");
            }
        }

        if (request.templateSource() != null) {
            throw new NotImplementedException();// TODO: implement. not bothering with this for now...
        }

        if (request.extraSource() != null) {
            throw new NotImplementedException();// TODO: implement. not bothering with this for now...
        }

        url = url
            .paramIfPresent("search_type", fromNullable(request.searchType()).transform(searchTypeToString))
            .paramIfPresent("query_cache", fromNullable(request.queryCache()).transform(booleanToString))
            .paramIfPresent("scroll", fromNullable(request.scroll()).transform(scrollToString))
            .paramIfPresent("routing", fromNullable(request.routing()))
            .paramIfPresent("preference", fromNullable(request.preference()))
        ;
        return Futures.transform(executor.post(url.url(), InputStreams.of(request.source())), new XContentResponseTransform<SearchResponse>(new SearchResponseHelper(request)));
    }
}
