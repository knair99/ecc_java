


#find out mod inverse of a, p
# a * ? = 1 mod p

def mod_inv(a, p):

	lm, hm = 1, 0
	low = a % p
	high = p
	new = 0

	while low > 1: 
		quotient = high / low
		nm = hm - (lm * quotient)
		new = high - low* quotient
		#print "lm = ", lm, "\thm = ", hm, "\thigh ", high, "\tlow", low, "\tnew", new, "\tquotient", quotient, "\tnm", nm
		high = low
		hm = lm
		low = new
		lm = nm

	return lm % p


def modinv(a,n): #Extended Euclidean Algorithm/'division' in elliptic curves
    lm, hm = 1,0
    low, high = a%n,n
    while low > 1:
        ratio = high/low
        nm, new = hm-lm*ratio, high-low*ratio
        lm, low, hm, high = nm, new, lm, low
    return lm % n


def ECdouble(xp,yp): # EC point doubling,  invented for EC. It doubles Point-P.
	Pcurve = 43
	LamNumer = 3*xp*xp
	LamDenom = 2*yp
	Lam = (LamNumer * modinv(LamDenom,Pcurve)) % Pcurve
	xr = (Lam*Lam-2*xp) % Pcurve
	yr = (Lam*(xp-xr)-yp) % Pcurve
	return (xr,yr)


def ECadd(xp,yp,xq,yq): # Not true addition, invented for EC. It adds Point-P with Point-Q.
	Pcurve = 43
	m = ((yq-yp) * modinv(xq-xp,Pcurve)) % Pcurve

	xr = (m*m-xp-xq) % Pcurve
	
	yr = (m*(xp-xr)-yp) % Pcurve
	return (xr,yr)



def EccMultiply(xs,ys,Scalar): # Double & add. EC Multiplication, Not true multiplication
	ScalarBin = str(bin(Scalar))[2:]

	print ScalarBin, len(ScalarBin)
	Qx,Qy=xs,ys
	for i in range (1, len(ScalarBin)): # This is invented EC multiplication.
	    Qx,Qy=ECdouble(Qx,Qy);  print "DUB", Qx; print
	    if ScalarBin[i] == "1":
	        Qx,Qy=ECadd(Qx,Qy,xs,ys);  print "ADD", Qx; print
	return (Qx,Qy)


print EccMultiply(20, 30, 43)



