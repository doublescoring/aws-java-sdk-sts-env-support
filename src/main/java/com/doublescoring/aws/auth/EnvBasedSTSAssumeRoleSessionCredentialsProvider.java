package com.doublescoring.aws.auth;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

/**
 * Wrapper for com.doublescoring.aws.auth.STSAssumeRoleSessionCredentialsProvider. It works like
 * conditional proxy - it returns STSAssumeRoleSessionCredentialsProvider if env variable AWS_ASSUME_ROLE is set
 * and returns com.doublescoring.aws.auth.DefaultAWSCredentialsProviderChain otherwise.
 * See README.md for examples
 */
public class EnvBasedSTSAssumeRoleSessionCredentialsProvider implements AWSCredentialsProvider {
	private static final String ENV_ROLE_ARN = "AWS_ROLE_ARN";
	private static final String ENV_SESSION_NAME = "AWS_SESSION_NAME";
	private static final String ENV_SESSION_DEFAULT_NAME = "session";


	private final AWSCredentialsProvider delegate;

	public EnvBasedSTSAssumeRoleSessionCredentialsProvider() {
		this(new DefaultAWSCredentialsProviderChain(), ENV_ROLE_ARN, ENV_SESSION_NAME);
	}

	public EnvBasedSTSAssumeRoleSessionCredentialsProvider(AWSCredentialsProvider defaultProvider,
														   String roleArnEnvName,
														   String sessionNameEnvName) {
		String roleArn = System.getenv(roleArnEnvName);
		String sessionName = System.getenv(sessionNameEnvName);
		if (sessionName == null) {
			sessionName = ENV_SESSION_DEFAULT_NAME;
		}
		if (roleArn == null) {
			delegate = defaultProvider;
		} else {
			delegate = new STSAssumeRoleSessionCredentialsProvider(
					new DefaultAWSCredentialsProviderChain(),
					roleArn,
					sessionName);
		}
	}

	public AWSCredentials getCredentials() {
		return delegate.getCredentials();
	}

	public void refresh() {
		delegate.refresh();
	}
}
