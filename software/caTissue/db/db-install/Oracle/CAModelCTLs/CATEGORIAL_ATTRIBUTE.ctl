LOAD DATA INFILE 'H://caTissue//work//workspace//catissuecoreNew/SQL/DBUpgrade/Common/CAModelCSVs/CATEGORIAL_ATTRIBUTE.csv' 
APPEND 
INTO TABLE CATEGORIAL_ATTRIBUTE 
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
(ID NULLIF ID='\\N',CATEGORIAL_CLASS_ID NULLIF CATEGORIAL_CLASS_ID='\\N',DE_CATEGORY_ATTRIBUTE_ID NULLIF DE_CATEGORY_ATTRIBUTE_ID='\\N',DE_SOURCE_CLASS_ATTRIBUTE_ID NULLIF DE_SOURCE_CLASS_ATTRIBUTE_ID='\\N')