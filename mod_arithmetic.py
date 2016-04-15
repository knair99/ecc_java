import math

#fast multiplication - modulo multiplication with prime numbers
#instead of multiplying, add repeatedly
def multiply_mod(a,b,p):
	(count, result, n) = (0, 0, a)

	for count in range (b.bit_length()):
		mask = 1 << count
		if b & mask:
			result = (result + n)
		n = (n + n)

	return result % p

def multiply_reg(a,b,p):
	c = a * b
	r = c % p
	return r

for i in range(0, 1000000):
	if multiply_mod(i, 1000000-i, 101) != multiply_reg(i, 1000000-i, 101):
		print "Error"

