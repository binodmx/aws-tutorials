package org.wso2.test.aws.sts;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import java.nio.charset.StandardCharsets;

public class AssumeRoleTest {

    public static void main(String[] args) {
        System.out.println(invokeLambda());
        System.out.println(invokeLambdaWithSTSAssumeRole());
    }

    private static String invokeLambda() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Environment.ACCESS_KEY_A,
                Environment.SECRET_KEY_A);
        AWSCredentialsProvider lambdaCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                .withCredentials(lambdaCredentialsProvider)
                .withRegion(Environment.REGION)
                .build();
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(Environment.FUNCTION_NAME)
                .withPayload(Environment.PAYLOAD)
                .withInvocationType(InvocationType.RequestResponse)
                .withSdkClientExecutionTimeout(Environment.RESOURCE_TIMEOUT);
        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
        return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
    }

    private static String invokeLambdaWithSTSAssumeRole() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(Environment.ACCESS_KEY_B,
                Environment.SECRET_KEY_B);
        AWSCredentialsProvider stsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(stsCredentialsProvider)
                .withRegion(Environment.REGION)
                .build();
        AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                .withRoleArn(Environment.ROLE_ARN)
                .withRoleSessionName(Environment.ROLE_SESSION_NAME);
        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(roleRequest);
        Credentials sessionCredentials = assumeRoleResult.getCredentials();
        BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                sessionCredentials.getAccessKeyId(),
                sessionCredentials.getSecretAccessKey(),
                sessionCredentials.getSessionToken());
        AWSCredentialsProvider lambdaCredentialsProvider = new AWSStaticCredentialsProvider(basicSessionCredentials);
        AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                .withCredentials(lambdaCredentialsProvider)
                .withRegion(Environment.REGION)
                .build();
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(Environment.FUNCTION_NAME)
                .withPayload(Environment.PAYLOAD)
                .withInvocationType(InvocationType.RequestResponse)
                .withSdkClientExecutionTimeout(Environment.RESOURCE_TIMEOUT);
        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
        return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
    }
}
