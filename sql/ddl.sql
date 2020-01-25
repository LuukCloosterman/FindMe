use master;
START TRANSACTION;

CREATE DATABASE  findMe;

USE findMe;

CREATE TABLE user(
	uid INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (uid)
);

CREATE TABLE uLoc(
	uid INT NOT NULL,
	longitude DECIMAL(9,6) NOT NULL,
	latitude DECIMAL(9,6) NOT NULL,
	PRIMARY KEY(uid)
);

CREATE TABLE gemLoc(
	uid	INT NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    PRIMARY KEY(longitude, latitude)
);



ALTER TABLE uLoc
ADD CONSTRAINT FK_uLoc_ref_user
FOREIGN KEY (uid) REFERENCES user(uid);

ALTER TABLE gemLoc
ADD CONSTRAINT FK_gemLoc_ref_user
FOREIGN KEY (uid) REFERENCES user(uid);
    

commit;