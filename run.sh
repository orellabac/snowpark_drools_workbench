# this command will mount the git folder to a local folder and run the container
docker run --volume=$HOME/wb_git:/opt/jboss/wildfly/bin/.niogit:Z   -p 8001:8001 -p 8080:8080 -d custom-drools-workbench-snowsql:latest
