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



DROP function IF EXISTS `calcDist`;
DELIMITER ;;
CREATE function `calcDist`(lat1 decimal(9,6), lon1 decimal(9,6), lat2 decimal(9,6), lon2 decimal(9,6))
Returns decimal(9,6)
BEGIN 


set @a = 0.5 - cos((lat2 - lat1) * (pi() / 180))/2 + 
          cos(lat1 * (pi() / 180)) * cos(lat2 * (pi() / 180)) * 
          (1 - cos((lon2 - lon1) * (pi() / 180)))/2;

return 12742 * asin(sqrt(@a));

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

DROP PROCEDURE IF EXISTS `makeGemLoc`;
DELIMITER ;;
CREATE PROCEDURE `makeGemLoc`(uidIN int, lat decimal(9,6), lon decimal(9,6))
proc: BEGIN 
if exists(select  latitude,longitude
		from gemLoc 
		where calcDist(lat,lon,latitude,longitude)<15)	
	THEN
		select  latitude,longitude
		into @latitude, @longitude
		from gemLoc 
		where calcDist(lat,lon,latitude,longitude)<15
		order by calcDist(lat,lon,latitude,longitude) desc
		LIMIT 1 ;

		insert into gemloc(uid,latitude,longitude)
		values(uidIN,@latitude,@longitude);
        
        
        call getGemLoc(uidIn);
        
        
        leave proc;
	end IF;


if exists(select  latitude,longitude
		from Uloc 
		where calcDist(lat,lon,latitude,longitude)<15)
	THEN
		select  uloc.uid ,uloc.latitude,uloc.longitude
		into @uid, @lat2, @lon2
		from uloc left join gemloc
        	on uloc.uid = gemloc.uid
		where calcDist(lat,lon,uloc.latitude,uloc.longitude)<15
        and uloc.uid != uidIN
		order by calcDist(lat,lon,uloc.latitude,uloc.longitude) desc
		LIMIT 1 ;
        
        call `setMeeting`(uidIN, lat, lon, @uid, @lat2, @lon2);
        
        
        
        leave proc;
	end IF;

END ;;
DELIMITER ;


drop PROCEDURE if exists `getGemLoc`;

DELIMITER ;;
create procedure `getGemLoc`(userID int)
BEGIN 
    select * 
    from gemloc
    where userID = uid;
END ;;
DELIMITER ;


drop PROCEDURE if exists `logout`;

DELIMITER ;;
create procedure `logout`(userID int)

BEGIN 
    delete
    from gemloc
    where uid = userID;
    
    delete
    from Uloc
    where uid = userID;
    
    delete
    from findme.user
    where uid = userID;
END ;;
DELIMITER ;

