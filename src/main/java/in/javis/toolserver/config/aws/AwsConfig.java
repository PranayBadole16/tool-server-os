package in.javis.toolserver.config.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up AWS service clients.
 * <p>
 * This class configures and provides beans for AWS Lambda and S3 clients,
 * using credentials obtained from the application properties.
 * </p>
 */
@Configuration
public class AwsConfig {

    /**
     * AWS access key from application properties.
     */
    @Value("${aws.accessKey}")
    private String awsAccessKey;

    /**
     * AWS secret key from application properties.
     */
    @Value("${aws.secretKey}")
    private String awsSecretKey;

    /**
     * Creates an AWS credentials provider with the provided access key and secret key.
     *
     * @return an {@link AWSCredentialsProvider} instance configured with the provided credentials
     */
    private AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    /**
     * Provides a bean for {@link AWSLambdaAsyncClientBuilder} configured with the AWS credentials.
     *
     * @return an {@link AWSLambdaAsyncClientBuilder} instance with the credentials set
     */
    @Bean
    public AWSLambdaAsyncClientBuilder getAmazonLambdaClientBuilder() {

        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        AWSLambdaAsyncClientBuilder lambdaClient = AWSLambdaAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials));

        return lambdaClient;
    }

    /**
     * Provides a bean for {@link AmazonS3ClientBuilder} configured with the AWS credentials.
     *
     * @return an {@link AmazonS3ClientBuilder} instance with the credentials set
     */
    @Bean
    public AmazonS3ClientBuilder getAmazonS3Client() {

        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        AmazonS3ClientBuilder s3clientBuilder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials));
        return s3clientBuilder;
    }
}
