
CREATE OR REPLACE STAGE rules;

-- command to upload file to stage
PUT file://engine-tools/target/drools-utils-0.0.1-FAT.jar @rules AUTO_COMPRESS=FALSE OVERWRITE=TRUE;
