catissue_site;select name,type,user_id from catissue_site where name='Not Specified Site' and type='Not Specified' and user_id=1 
catissue_address;select street,city,state,country,zipcode,phone_number from catissue_address , catissue_site where catissue_address.identifier=catissue_site.address_id and name='Not Specified Site' and type='Not Specified' and user_id=1 