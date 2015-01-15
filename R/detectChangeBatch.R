library(bcp)

inputFile <- "./changedata.txt"
con  <- file(inputFile, open = "r")
outputFile <- "./changedata-points.txt"
ocon <- file(outputFile, open="w")

# Read data from file
print("Loading data...")
uids <- vector()
dataList <- list()
while (length(oneLine <- readLines(con, n = 1, warn = FALSE)) > 0) {
  myVector <- (strsplit(oneLine, " "))[[1]]
  uids[length(uids)+1] <- myVector[1]
  st <- c() # start time seconds
  et <- c() # end time seconds
  icf <- c() # ICF
  for( record in myVector[2:length(myVector)]){
    localVector <- (strsplit(record, ","))[[1]]
    st[length(st)+1] <- localVector[1]
    et[length(et)+1] <- localVector[2]
    icf[length(icf)+1] <- localVector[3]
  }
  udata <- data.frame(as.numeric(st), as.numeric(et), as.numeric(icf))
  colnames(udata) <- c("stime", "etime", "icf")
  dataList[[length(dataList)+1]] <- udata
}

# detect change point and write result to disk
print("Detecting change point...")
for ( i in seq(1, length(uids))){
  bcp.icf <- bcp(dataList[[i]]$icf)
  dataList[[i]]["pp"] <- bcp.icf$posterior.prob
  user <- dataList[[i]]
  strcps <- paste(user$pp, collapse=" ")
  content <- paste(uids[i], strcps, collapse=" ")
  writeLines(content, ocon)
}

close(con)
close(ocon)