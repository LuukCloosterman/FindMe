drop database if exists `findme`;

CREATE DATABASE  IF NOT EXISTS `findme` ;
USE `findme`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`uid`)
);


DROP TABLE IF EXISTS `gemloc`;
CREATE TABLE `gemloc` (
  `uid` int(11) NOT NULL,
  `longitude` decimal(9,6) NOT NULL,
  `latitude` decimal(9,6) NOT NULL,
  PRIMARY KEY (`uid`)
  
) ;


alter table gemloc
add constraint FK_gemLoc_ref_user
foreign key (uid) references user(uid);
  

DROP TABLE IF EXISTS `uloc`;

CREATE TABLE `uloc` (
  `uid` int(11) NOT NULL,
  `longitude` decimal(9,6) NOT NULL,
  `latitude` decimal(9,6) NOT NULL,
  PRIMARY KEY (`uid`)
  
) ;
alter table uloc
add constraint FK_uLoc_ref_user
foreign key (uid) references user(uid);


DROP PROCEDURE IF EXISTS `GETUID`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `GETUID`()
BEGIN 

INSERT INTO user
VALUES(); 

select LAST_INSERT_ID() AS uid; 
END ;;
DELIMITER ;
