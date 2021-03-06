package org.elasticsearch.search.aggregations.bucket.range;

import com.bazaarvoice.elasticsearch.client.core.util.aggs.AggregationsManifest;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.support.format.ValueFormatter;

import java.util.List;
import java.util.Map;

import static com.bazaarvoice.elasticsearch.client.core.util.MapFunctions.nodeListValue;
import static com.bazaarvoice.elasticsearch.client.core.util.MapFunctions.nodeMapValue;

public class RangeHelper {
    public static InternalAggregation fromXContent(final String name, final Map<String, Object> map, final AggregationsManifest manifest) {
        final Object bucketsObj = map.get("buckets");
        if (bucketsObj instanceof Map) {
            final Map<String, Object> bucketMap = nodeMapValue(bucketsObj, String.class, Object.class);
            return internalKeyedRange(name, bucketMap, manifest);
        } else {
            final List<Object> bucketList = nodeListValue(bucketsObj, Object.class);
            return internalAnonRange(name, bucketList, manifest);
        }
    }

    private static InternalRange<InternalRange.Bucket> internalAnonRange(final String name, final List<Object> list, final AggregationsManifest manifest) {
        final ImmutableList.Builder<InternalRange.Bucket> buckets = ImmutableList.builder();
        for (Object bucketObj : list) {
            final Map<String, Object> bucketMap = nodeMapValue(bucketObj, String.class, Object.class);
            buckets.add(RangeBucketHelper.fromXContent(null, bucketMap, manifest));
        }
        // In the current implementation, the bucket will always have a key (because it generates one if it's missing).
        // Also, possibly because the Java api cannot ask for a "keyed" range agg, ES is always giving back
        // a list, even when we specify keys.
        // I'm thinking there's no downside to always saying the range is keyed, since you can still just do getBuckets if you don't know the keys.
        return new InternalRange<InternalRange.Bucket>(name, buckets.build(), ValueFormatter.RAW, true);
    }

    private static InternalRange<InternalRange.Bucket> internalKeyedRange(final String name, final Map<String, Object> map, final AggregationsManifest manifest) {
        final ImmutableList.Builder<InternalRange.Bucket> buckets = ImmutableList.builder();
        for (Map.Entry<String, Object> bucketObj : map.entrySet()) {
            final String bucketName = bucketObj.getKey();
            final Map<String, Object> bucketMap = nodeMapValue(bucketObj.getValue(), String.class, Object.class);
            buckets.add(RangeBucketHelper.fromXContent(bucketName, bucketMap, manifest));
        }

        return new InternalRange<InternalRange.Bucket>(name, buckets.build(), ValueFormatter.RAW, true);
    }
}
