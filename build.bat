docker build -t custom-drools-workbench-snowsql --build-arg SNOWSQL_ACCOUNT=your_account --build-arg SNOWSQL_USER=your_user  --build-arg SNOWSQL_PWD=your_password  --build-arg SNOWSQL_DATABASE=your_database  --build-arg SNOWSQL_SCHEMA=your_schema  --build-arg SNOWSQL_ROLE=your_role  --build-arg SNOWSQL_WAREHOUSE=your_warehouse  --build-arg SNOWSQL_PRIVATE_KEY_PASSPHRASE=key_passphrase  --build-arg SNOWSQL_PRIVATE_KEY="<path to key file or empty to use user-password" .