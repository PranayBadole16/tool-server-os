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

@Service
@Slf4j
public class AwsServices {

    @Value("${aws.S3.region}")
    private String s3Region;

    @Autowired
    private AmazonS3ClientBuilder s3clientBuilder;

    public S3Object getS3Object(String bucketName, String key) {

        S3Object s3object = s3clientBuilder.withRegion(s3Region).build().getObject(bucketName, key);
        return s3object;
    }


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