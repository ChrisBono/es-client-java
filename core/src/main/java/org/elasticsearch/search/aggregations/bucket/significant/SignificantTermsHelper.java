package org.elasticsearch.search.aggregations.bucket.significant;

import com.bazaarvoice.elasticsearch.client.core.util.aggs.AggregationsManifest;
import org.elasticsearch.action.search.helpers.AbstractInternalAggregation;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.search.aggregations.InternalAggregation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.bazaarvoice.elasticsearch.client.core.util.MapFunctions.nodeListValue;
import static com.bazaarvoice.elasticsearch.client.core.util.MapFunctions.nodeMapValue;
import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeLongValue;

public class SignificantTermsHelper {
    public static InternalAggregation fromXContent(final String name, final Map<String, Object> map, final AggregationsManifest manifest) {
        final long docCount = nodeLongValue(map.get("doc_count"));
        final List<Object> bucketObjs = nodeListValue(map.get("buckets"), Object.class);
        final ImmutableList.Builder<SignificantTerms.Bucket> buckets = ImmutableList.builder();
        for (Object bucketObj : bucketObjs) {
            final Map<String, Object> bucketMap = nodeMapValue(bucketObj, String.class, Object.class);
            buckets.add(SignificantTermsBucketHelper.fromXContent(docCount, bucketMap, manifest));
        }
        return new ComputedSignificantTerms(name, buckets.build());
    }

    private static class ComputedSignificantTerms extends AbstractInternalAggregation implements SignificantTerms {

        private final Collection<Bucket> buckets;
        private final Map<String, Bucket> bucketMap;

        private ComputedSignificantTerms(final String name, final Collection<SignificantTerms.Bucket> buckets) {
            super(name);
            this.buckets = buckets;
            final ImmutableMap.Builder<String, Bucket> builder = ImmutableMap.builder();
            for (Bucket bucket : buckets) {
                builder.put(bucket.getKey(), bucket);
            }
            this.bucketMap = builder.build();
        }

        @Override public Iterator<Bucket> iterator() {
            return getBuckets().iterator();
        }

        @Override public Collection<Bucket> getBuckets() {
            return buckets;
        }

        @Override public Bucket getBucketByKey(final String key) {
            return bucketMap.get(key);
        }
    }
}
