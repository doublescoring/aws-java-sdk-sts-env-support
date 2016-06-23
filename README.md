# aws-java-sdk-sts-env-support

[![Build Status](https://travis-ci.org/doublescoring/aws-java-sdk-sts-env-support.svg?branch=master)](https://travis-ci.org/doublescoring/aws-java-sdk-sts-env-support)

AWSCredentialsProvider implementation to simplify cross-account deployment
customization based on env variables. Use case:

* Production and development AWS accounts.
* You have some resource in production account (DynamoDB table for example) you have to share
with development account.
* Production and development application share the same code and configured with env variables
(ECS, docker-compose, etc..)

The best practice for such case proposed by AWS is to create IAM role in production
account for cross-account access to the shared resource. Afterwards development user could
assume the role and use the resource as described in [the tutorial](https://blogs.aws.amazon.com/security/post/TxC24FI9IDXTY1/Delegating-API-Access-to-AWS-Services-Using-IAM-Roles)
The tutorial propose to execute _AssumeRoleRequest_ and it leeds to changes in development code:
```
if (devEnv) {
    AssumeRoleRequest assumeRequest = new AssumeRoleRequest()
            .withRoleArn(ROLE_ARN)
            .withDurationSeconds(3600)
            .withRoleSessionName("demo");

        AssumeRoleResult assumeResult =
        stsClient.assumeRole(assumeRequest);
}
```

The other disadvantage is the need to refresh session token after expiration by yourself.

The other way possible is to use [STSAssumeRoleSessionCredentialsProvider](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/STSAssumeRoleSessionCredentialsProvider.html).
It handles session token expiration but it still requires to make changes in development code
in place where you access the shared resources.
Environment variable is a convenient way to override application behaviour, for example:
```

if (System.getenv("AWS_ROLE_ARN")) {
    return new STSAssumeRoleSessionCredentialsProvider(
           					new DefaultAWSCredentialsProviderChain(),
           					System.getenv("AWS_ROLE_ARN"), "session");
} else {
    return new DefaultAWSCredentialsProviderChain()
}
```

The module _aws-java-sdk-sts-env-support_ propose simple wrapper around code above.
You could use _EnvBasedSTSAssumeRoleSessionCredentialsProvider_ and is will use
_DefaultAWSCredentialsProviderChain_ on production account and _STSAssumeRoleSessionCredentialsProvider_
 on development account. So the final code snippet will be:
 ```
 AmazonDynamoDBClient dynamoDBClient =
 			new AmazonDynamoDBClient(new EnvBasedSTSAssumeRoleSessionCredentialsProvider());
 ```
