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

@Configuration
public class AwsConfig {

    @Value("${aws.accessKey}")
    private String awsAccessKey;

    @Value("${aws.secretKey}")
    private String awsSecretKey;

    private AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey));
    }

    @Bean
    public AWSLambdaAsyncClientBuilder getAmazonLambdaClientBuiler() {

        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        AWSLambdaAsyncClientBuilder lambdaClient = AWSLambdaAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials));

        return lambdaClient;
    }

    @Bean
    public AmazonS3ClientBuilder getAmazonS3Client() {

        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        AmazonS3ClientBuilder s3clientBuilder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials));
        return s3clientBuilder;
    }

}