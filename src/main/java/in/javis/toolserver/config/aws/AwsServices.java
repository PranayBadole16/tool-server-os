package in.javis.toolserver.config.aws;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for interacting with Amazon S3.
 * <p>
 * This service provides methods to retrieve objects from an S3 bucket and to list all objects
 * within a bucket with a specified prefix.
 * </p>
 */
@Service
@Slf4j
public class AwsServices {

    /**
     * The AWS region where the S3 bucket is located.
     */
    @Value("${aws.S3.region}")
    private String s3Region;

    /**
     * Builder for creating instances of {@link AmazonS3ClientBuilder}.
     */
    @Autowired
    private AmazonS3ClientBuilder s3clientBuilder;

    /**
     * Retrieves an S3 object from the specified bucket with the given key.
     *
     * @param bucketName the name of the S3 bucket
     * @param key the key of the object within the bucket
     * @return the {@link S3Object} corresponding to the specified bucket and key
     */
    public S3Object getS3Object(String bucketName, String key) {
        S3Object s3object = s3clientBuilder.withRegion(s3Region).build().getObject(bucketName, key);
        return s3object;
    }

    /**
     * Lists all objects in the specified bucket that match the given prefix.
     * <p>
     * This method supports pagination, so if there are more objects than can be returned in a single request,
     * it will continue fetching objects until all have been retrieved.
     * </p>
     *
     * @param bucketName the name of the S3 bucket
     * @param prefix the prefix to filter objects by
     * @return a list of {@link S3ObjectSummary} objects representing the objects in the bucket with the specified prefix
     */
    public List<S3ObjectSummary> listAllObjectsOfBucketWithPrefix(String bucketName, String prefix) {

        List<S3ObjectSummary> s3ObjectSummaries = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(prefix)
                    .withContinuationToken(continuationToken);

            ListObjectsV2Result result = s3clientBuilder.withRegion(s3Region).build().listObjectsV2(listObjectsRequest);
            s3ObjectSummaries.addAll(result.getObjectSummaries());

            continuationToken = result.getNextContinuationToken();
        } while (continuationToken != null);

        return s3ObjectSummaries;
    }
}
