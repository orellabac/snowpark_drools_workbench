# Start from the Drools Workbench image
FROM jboss/drools-workbench-showcase:7.17.0.Final



# 
# Define build-time variables for SnowSQL configurations
ARG SNOWSQL_ACCOUNT
ARG SNOWSQL_USER
ARG SNOWSQL_PWD
ARG SNOWSQL_DATABASE
ARG SNOWSQL_SCHEMA
ARG SNOWSQL_ROLE
ARG SNOWSQL_WAREHOUSE
ARG SNOWSQL_PRIVATE_KEY_PASSPHRASE
COPY pom.xml /opt
# this is copied to enable the repositories
COPY CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo
# we need to copy the handler generator project
COPY  --chown=jboss handler-generator-plugin /opt/handler-generator-plugin
# Install Git and necessary tools
USER root
RUN yum install -y git wget zip maven && yum clean all

# Download and install the SnowSQL CLI tool
RUN wget -P /opt https://sfc-repo.snowflakecomputing.com/snowsql/bootstrap/1.3/linux_x86_64/snowsql-1.3.2-linux_x86_64.bash

# Install  Java11 this is needed for compiling the drools projects
RUN yum install -y java-11-openjdk-devel

# Set up environment variables for SnowSQL
ENV SNOWSQL_ACCOUNT=${SNOWSQL_ACCOUNT}
ENV SNOWSQL_USER=${SNOWSQL_USER}
# For local  testing you can use user/password but for production use a key pair
ENV SNOWSQL_PWD=${SNOWSQL_PWD}
ENV SNOWSQL_DATABASE=${SNOWSQL_DATABASE}
ENV SNOWSQL_SCHEMA=${SNOWSQL_SCHEMA}
ENV SNOWSQL_ROLE=${SNOWSQL_ROLE}
ENV SNOWSQL_WAREHOUSE=${SNOWSQL_WAREHOUSE}
# the recomended security approach is to use a key pair for authentication
# normally this will be a service account
# if SNOWSQL_PRIVATE_KEY_PATH is empty it will be assumed that you are using user/password
ENV SNOWSQL_PRIVATE_KEY_PASSPHRASE=${SNOWSQL_PRIVATE_KEY_PASSPHRASE}
ENV SNOWSQL_PRIVATE_KEY_PATH="${SNOWSQL_PRIVATE_KEY_PATH}"


# Switch back to the jboss user if needed
USER jboss
# install snowsql for jboss user
WORKDIR /opt/jboss/
RUN SNOWSQL_DEST=~/bin SNOWSQL_LOGIN_SHELL=/opt/jboss/.bash_profile bash /opt/snowsql-1.3.2-linux_x86_64.bash 
#&& rm snowsql-1.3.2-linux_x86_64.bash
# we build the generator and install it
WORKDIR /opt/handler-generator-plugin
RUN mvn install 
#RUN mvn dependency:go-offline

# Continue with the default CMD from the base image
WORKDIR /opt/jboss/wildfly/bin