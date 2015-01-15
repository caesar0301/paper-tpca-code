library(bcp)

inputFile <- "changedata-xi.txt"
con  <- file(inputFile, open = "r")

userList <- list()

# read data from file
while (length(oneLine <- readLines(con, n = 1, warn = FALSE)) > 0) {
  myVector <- (strsplit(oneLine, " "))[[1]]
  uid <- myVector[1]
  minsim <- myVector[2]
  dpoints <- as.numeric(myVector[3:length(myVector)])
  oneUser <- list(uid <- uid, minsim <- minsim, dpoints <- dpoints)
  userList[[length(userList)+1]] <- oneUser
}

# format data
oneguy <- userList[[1]][[1]]
simVector <- c()
sequences <- c()
for (record in userList){
  if (record[[1]] == oneguy){
    simVector[length(simVector)+1] <- record[2]
    sequences[length(sequences)+1] <- record[3]
  }
}

simVector <- as.numeric(simVector)
sims <- c(0.3, 0.4, 0.5, 0.6)
colors <- c("red", "blue", "chocolate4", "green4")
selector <- which(simVector %in% sims)
ltypes <- seq(1, length(selector))

pdf("estimateXI.pdf")
op <- par(mfrow=c(2,1),col.lab="black",col.main="black", 
          mgp = c(1.7, 0.3, 0), cex.axis=1.2, cex.lab=1.2)

op2 <- par(mar=c(0,3,1,1))
plot.indicator <- FALSE
i <- 0
for (sequence in sequences[selector]){
  i <- i+1
  data <- (as.numeric(sequence))
  bcp.d <- bcp(data)
  if (plot.indicator == FALSE){
    plot(1:length(bcp.d$data),bcp.d$posterior.mean, type="l", 
         lwd=1, lty=ltypes[i], xaxt='n', yaxt='n',
         xlab="", ylab="Posterior Expectation", col=colors[i])
    axis(2, tcl=0.5)
    plot.indicator <- TRUE
  } else {
    lines(bcp.d$posterior.mean, lwd=1, lty=ltypes[i], col=colors[i])
  }
}
grid()

## plot legend
exps <- vector()
#exps <- c(exps, expression(paste(xi,"=", 0.2)))
exps <- c(exps, expression(paste(xi,"=", 0.3)))
exps <- c(exps, expression(paste(xi,"=", 0.4)))
exps <- c(exps, expression(paste(xi,"=", 0.5)))
exps <- c(exps, expression(paste(xi,"=", 0.6)))
legend("topleft",  exps, lty=ltypes, col=colors)
par(op2)


## plot second graph
plot.indicator <- FALSE
i <- 0
op3 <- par(mar=c(3,3,0,1), xaxt="s")
for (sequence in sequences[selector]){
  i <- i+1
  data <- (as.numeric(sequence))
  bcp.d <- bcp(data)
  if (plot.indicator == FALSE){
    plot(bcp.d$posterior.prob, type="l", 
         lty=ltypes[i], yaxt="n", ylim=c(0,1), xaxt="n", yaxt="n",
         xlab="Time", ylab="Posterior Probability", main="", col=colors[i])
    dt.seq <- seq(as.POSIXct("2013-04-23"), as.POSIXct("2013-07-15"), length.out=6)
    fmt <- "%b.%d" # format for axis labels
    labs <- format(dt.seq, fmt)
    axis(1, tcl=0.5, at=seq(0, 50, 10), labels=labs)
    axis(2, yaxp=c(0, 1, 5), tcl=0.5)
    plot.indicator <- TRUE
  } else {
    lines(bcp.d$posterior.prob, lwd=1, lty=ltypes[i], col=colors[i])
  }
}
grid()
par(op3)
par(op)

dev.off()

close(con)