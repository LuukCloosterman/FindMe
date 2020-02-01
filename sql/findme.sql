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



DROP PROCEDURE IF EXISTS `calcDist`;
DELIMITER ;;
CREATE PROCEDURE `calcDist`(lat1 decimal(9,6), lon1 decimal(9,6), lat2 decimal(9,6), lon2 decimal(9,6),out dist decimal(9,6))
BEGIN 
	

set @a = 0.5 - cos((lat2 - lat1) * (pi() / 180))/2 + 
          cos(lat1 * (pi() / 180)) * cos(lat2 * (pi() / 180)) * 
          (1 - cos((lon2 - lon1) * (pi() / 180)))/2;

set dist = 12742 * asin(sqrt(@a));

END ;;
DELIMITER ;


DROP PROCEDURE IF EXISTS `setMeeting`;
DELIMITER ;;
CREATE PROCEDURE `setMeeting`(UID1 int,lat1 decimal(9,6), lon1 decimal(9,6), UID2 int, lat2 decimal(9,6), lon2 decimal(9,6))
BEGIN 

set @lat = (lat1 + lat2) / 2;
set @lon = (lon1 + lon2) / 2;


insert into gemLoc(uid,latitude,longitude)
values (uid1,ROUND(@lat,9),ROUND(@lon,9)),
	   (uid2,ROUND(@lat,9),ROUND(@lon,9));



END ;;
DELIMITER ;
