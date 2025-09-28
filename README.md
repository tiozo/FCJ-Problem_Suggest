# FCJ-Problem_Suggest
A project to help highschool student in study.

## IAM policy setup

```json
{
	"Version": "2012-10-17",
	"Statement": [
		{
			"Sid": "S3InputRead",
			"Effect": "Allow",
			"Action": [
				"s3:PutObject",
				"s3:GetObject",
				"s3:DeleteObject",
				"s3:ListBucket"
			],
			"Resource": [
				"arn:aws:s3:::fcj-problem-suggest",
				"arn:aws:s3:::fcj-problem-suggest/input/*"
			]
		},
		{
			"Sid": "S3OutputWrite",
			"Effect": "Allow",
			"Action": [
				"s3:PutObject"
			],
			"Resource": [
				"arn:aws:s3:::fcj-problem-suggest/output/*"
			]
		},
		{
			"Sid": "BedrockMetaMaverickOnly",
			"Effect": "Allow",
			"Action": [
				"bedrock:InvokeModel",
				"bedrock:InvokeModelWithResponseStream"
			],
			"Resource": [
				"arn:aws:bedrock:*:<Your AWS account ID>:inference-profile/us.meta.llama4-maverick-17b-instruct-v1:0",
				"arn:aws:bedrock:*::foundation-model/meta.llama4-maverick-17b-instruct-v1:0"
			]
		}
	]
}
```