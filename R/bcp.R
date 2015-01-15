library(bcp)
library(strucchange)
########################
# Draw the plot of BH and BP change detection
########################

# As example of user 00904cc51238
b <- c(47.070,32.509,17.877,21.636,21.984,31.237,29.273,26.584,22.477,36.012,52.398,51.205,38.532,45.602,28.457,31.267,28.916,23.643,24.447,15.548,17.805,23.682,21.140,33.618,25.911,49.807,31.534,30.629,41.480,27.303,25.158,22.077,24.100,20.129,19.718,26.893,25.037,41.072,38.497,50.827,41.775,64.570,36.033,31.097,39.896,43.649,34.415,43.529,45.415,48.009,48.885)
data <- b
bcp.d <- bcp(as.vector(data))
plot.bcp(bcp.d)

pdf("bh_vs_bp.pdf")

# To see bcp and Bai and Perron results:
bp <- breakpoints(data ~ 1, h = 2)$breakpoints
rho <- rep(0, length(data))
rho[bp] <- 1
b.num<-1 + c(0,cumsum(rho[1:(length(rho)-1)]))
bp.mean <- unlist(lapply(split(data, b.num),mean))
bp.ri <- rep(0,length(data))
for (i in 1:length(bp.ri)) bp.ri[i] <- bp.mean[b.num[i]]

op <- par(mfrow=c(2,1),col.lab="black",col.main="black", 
          mgp = c(1.7, 0.3, 0), cex.axis=1.2, cex.lab=1.2)

op2 <- par(mar=c(0,3,1,1),xaxt="n")
plot(1:length(bcp.d$data), bcp.d$data, col="grey", pch=20,
     xlab="", ylab="Posterior Expectation", main="", xaxt="n", yaxt='n')
axis(2, tcl=0.5)
lines(bcp.d$posterior.mean, lwd=1)
lines(bp.ri, col="blue")
legend("topleft" , c("Frequentist procedure", "Bayesian procedure"),
       col=c("blue", "black"), lty=c(1,1))
grid()
par(op2)

op3 <- par(mar=c(3,3,0,1), xaxt="s")
plot(bcp.d$posterior.prob, yaxt="n", xaxt="n", yaxt='n',
     type="l", ylim=c(0,1),
     xlab="Time", ylab="Posterior Probability", main="")
xax <- seq(1, length(bcp.d$posterior.prob))
for (i in 1:length(bp.ri)) abline(v=xax[bp[i]], col="blue")
dt.seq <- seq(as.POSIXct("2013-04-23"), as.POSIXct("2013-07-15"), length.out=6)
fmt <- "%b.%d" # format for axis labels
labs <- format(dt.seq, fmt)
axis(1, tcl=0.5, at=seq(0, 50, 10), labels=labs)
axis(2, yaxp=c(0, 1, 5), tcl=0.5)
grid()
par(op3)
par(op)

dev.off()