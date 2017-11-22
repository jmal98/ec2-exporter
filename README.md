# Amazon Elastic Compute Cloud (EC2) Exporter

A Prometheus exporter for Amazon EC2 metrics and conditions.  While there is the official Prometheus [Cloudwatch Exporter](https://github.com/prometheus/cloudwatch_exporter), 
this exporter is a simplified version collecting a subset of the information from EC2 with additional details, while not incurring the cost of using the Cloudwatch API.

### Build

```bash
docker build --tag ec2-exporter  .
```

### Configuration
The exporter makes use of the official Java sdk, and can be configured using a variety of methods:

* Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY, etc...
* Credentials delivered through the Amazon EC2 container service if AWS_CONTAINER_CREDENTIALS_RELATIVE_URI" environment variable is set and security manager has permission to access the variable,
* Instance profile credentials delivered through the Amazon EC2 metadata service if running within AWS

### Running

You can deploy this exporter using the [jmal98/ec2-exporter](https://hub.docker.com/r/jmal98/ec2-exporter/) Docker image.

For example if using an IAM profile:

```bash
docker run -d -p 9385:9385 jmal98/ec2-exporter
```

For example if supplying environment configuration:

```bash
docker run -d -p 9385:9385 -e AWS_ACCESS_KEY_ID=<access key> -e AWS_SECRET_ACCESS_KEY=<secret key> [other Java AWS SDK environment variables]  jmal98/ec2-exporter
```
