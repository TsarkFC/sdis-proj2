# If you choose to use RMI in the communication between the test application and the peer,
#you should use as access point the name of the remote object providing the "testing" service.
export CLASSPATH=
cd build


################# Simple ###############################
#../../scripts/test.sh access0 BACKUP ../files/321.txt 1
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access0 BACKUP ../files/321.txt 2
#../../scripts/test.sh access0 BACKUP ../files/test.txt 2
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access1 RECLAIM 20
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access2 STATE
#../../scripts/test.sh access1 STATE

#81 chunk Image TODO ver se nos da feup tb corre bem
#../../scripts/test.sh access0 BACKUP ../files/5mb.jpg 3
#../../scripts/test.sh access0 RESTORE ../files/5mb.jpg
#../../scripts/test.sh access1 RECLAIM 70
#../../scripts/test.sh access0 STATE
#../../scripts/test.sh access1 STATE
#../../scripts/test.sh access0 DELETE ../files/5mb.jpg
#../../scripts/test.sh access0 BACKUP ../files/1mb.jpeg 3
#../../scripts/test.sh access0 RESTORE ../files/1mb.jpeg
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 3
#../../scripts/test.sh access1 RECLAIM 0
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg



################# TEST MANY SAME TIME BACKUP ###################
#Run all backups at same time
#../../scripts/test.sh access0 BACKUP ../files/file.txt 1
#../../scripts/test.sh access0 BACKUP ../files/321.txt 1
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 1
#../../scripts/test.sh access0 BACKUP ../files/5mb.jpg 1
#../../scripts/test.sh access1 STATE


#../../scripts/test.sh access0 RESTORE ../files/file.txt
#../../scripts/test.sh access0 RESTORE ../files/321.txt
#../../scripts/test.sh access0 RESTORE ../files/bigimage.jpg
#../../scripts/test.sh access0 RESTORE ../files/5mb.jpg

#../../scripts/test.sh access0 DELETE ../files/file.txt
#../../scripts/test.sh access0 DELETE ../files/321.txt
#../../scripts/test.sh access0 DELETE ../files/bigimage.jpg
#../../scripts/test.sh access0 DELETE ../files/5mb.jpg

################# TEST RECLAIM ###################
#Backup and following reclaim
  #Run with 4 peers, so the perceived rep degree is 3
  #../../scripts/test.sh access0 BACKUP ../files/file.txt 4
  #../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
  #../../scripts/test.sh access1 RECLAIM 70
  # It should delete the image and keep the file since RepDgrImg(4) > PercDgrImg (3) > RepDgrFile(2)

#Test reclaim and following backup
  #Run backup with 3 peers
  #../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 2
  #Run reclaim with 4 peers
  #../../scripts/test.sh access1 RECLAIM 70
  #It should backup the other chunks in 3
  #Verify if peer 2 is NOT hosting anything
  #../../scripts/test.sh access2 STATE
  #Verify if peer 1 is only storing one file
  #../../scripts/test.sh access1 STATE


#state after reclaim and backup
#../../scripts/test.sh access1 RECLAIM 0
#../../scripts/test.sh access0 BACKUP ../files/bigimage.jpg 3
#../../scripts/test.sh access0 STATE
#../../scripts/test.sh access1 STATE

# TEST DEMO FILES
#../../scripts/test.sh access0 BACKUP ../files/files/medium_file.pdf 2
#../../scripts/test.sh access1 STATE
#../../scripts/test.sh access0 RESTORE ../files/files/medium_file.pdf
#../../scripts/test.sh access0 DELETE ../files/files/medium_file.pdf
#../../scripts/test.sh access2 RECLAIM 0
