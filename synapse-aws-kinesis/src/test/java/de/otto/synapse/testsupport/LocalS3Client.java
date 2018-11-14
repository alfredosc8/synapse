package de.otto.synapse.testsupport;

import org.slf4j.Logger;
import shaded.org.apache.commons.io.FileUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.Abortable;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.otto.synapse.testsupport.BucketItem.bucketItemBuilder;
import static org.slf4j.LoggerFactory.getLogger;

public class LocalS3Client implements S3Client {

    private static final Logger LOG = getLogger(LocalS3Client.class);
    private static final Instant BUCKET_DEFAULT_CREATION_DATE = Instant.parse("2017-01-01T10:00:00.00Z");

    private Map<String, Map<String, BucketItem>> bucketsWithContents;

    public LocalS3Client() {
        this.bucketsWithContents = new HashMap<>();
    }

    @Override
    public ListObjectsV2Response listObjectsV2(final ListObjectsV2Request listObjectsV2Request) throws S3Exception {
        final Collection<S3Object> s3Objects = bucketsWithContents.get(listObjectsV2Request.bucket())
                .values()
                .stream()
                .map(bucketItem -> S3Object.builder()
                        .key(bucketItem.getName())
                        .size((int) bucketItem.getData().contentLength())
                        .lastModified(bucketItem.getLastModified())
                        .build())
                .collect(Collectors.toList());

        return ListObjectsV2Response.builder()
                .contents(s3Objects)
                .keyCount(s3Objects.size())
                .build();
    }

    @Override
    public CreateBucketResponse createBucket(final CreateBucketRequest createBucketRequest) throws S3Exception {
        bucketsWithContents.put(createBucketRequest.bucket(), new HashMap<>());
        return CreateBucketResponse.builder().build();
    }

    @Override
    public PutObjectResponse putObject(final PutObjectRequest putObjectRequest,
                                       final RequestBody requestBody) throws S3Exception {
        bucketsWithContents.get(putObjectRequest.bucket()).put(putObjectRequest.key(),
                bucketItemBuilder()
                        .withName(putObjectRequest.key())
                        .withData(requestBody)
                        .withLastModifiedNow()
                        .build());
        return PutObjectResponse.builder().build();
    }

    @Override
    public DeleteObjectsResponse deleteObjects(final DeleteObjectsRequest deleteObjectsRequest) throws S3Exception {
        final Map<String, BucketItem> bucketItemMap = bucketsWithContents.get(deleteObjectsRequest.bucket());
        deleteObjectsRequest.delete().objects()
                .stream()
                .map(ObjectIdentifier::key)
                .forEach(bucketItemMap::remove);
        return DeleteObjectsResponse.builder().build();
    }

    @Override
    public ListBucketsResponse listBuckets(final ListBucketsRequest listBucketsRequest) throws S3Exception {
        return ListBucketsResponse.builder()
                .buckets(bucketsWithContents.keySet().stream()
                        .map(name -> Bucket.builder()
                                .creationDate(BUCKET_DEFAULT_CREATION_DATE)
                                .name(name)
                                .build())
                        .collect(Collectors.toList())).build();
    }

    @Override
    public GetObjectResponse getObject(final GetObjectRequest getObjectRequest,
                                       final Path filePath) throws S3Exception {
        final Map<String, BucketItem> bucketItemMap = bucketsWithContents.get(getObjectRequest.bucket());
        final BucketItem bucketItem = bucketItemMap.get(getObjectRequest.key());

        try {
            FileUtils.copyInputStreamToFile(bucketItem.getData().contentStreamProvider().newStream(), filePath.toFile());
        } catch (IOException e) {
            throw SdkClientException.create("", e);
        }

        return GetObjectResponse.builder().build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ResponseInputStream<GetObjectResponse> getObject(final GetObjectRequest getObjectRequest) throws S3Exception {
        final Map<String, BucketItem> bucketItemMap = bucketsWithContents.get(getObjectRequest.bucket());
        final BucketItem bucketItem = bucketItemMap.get(getObjectRequest.key());
        try {
            return new ResponseInputStream<>(GetObjectResponse.builder().build(), toAbortableInputStream(bucketItem));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw SdkClientException.create("", e);
        }

    }

    private AbortableInputStream toAbortableInputStream(final BucketItem bucketItem) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor<AbortableInputStream> constructor = AbortableInputStream.class.getDeclaredConstructor(InputStream.class, Abortable.class);
        constructor.setAccessible(true);
        return constructor.newInstance(
                bucketItem.getData().contentStreamProvider().newStream(),
                (Abortable) () -> {
                }
        );
    }

    @Override
    public void close() {
        LOG.debug("s3 closing...");
    }

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }
}
