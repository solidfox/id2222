#Report

ID2222 - Homework 5 - 2017-12-10 - Daniel Schlaug

Using a parameter grid of 27 + sets of parameters for both Linear and Exponential annealing I concluded the following:

## Minimal cuts 

3elt: 533 with Exponential and 598 with Linear.

add20: 1355 with Linear and 1368 with Exponential.

twitter: 40791 with exponential and 40865 with Linear.

The conclusion is thus that the annealing method is of little consequence to the result, at least when using the full 1000 rounds.

## Time to converge

Time to convergence increases with decreasing annealing speed.

## Number of swaps

Number of swaps increases with decreasing annealing speed.

## Additional remarks

With the current JaBeJa the good initial temperature depends on the average degree of the graph. To alleviate this I changed the algorithm to normalize the benefits between 0 and 1 by dividing by the total degree. This enabled using the same tempeerature and annealing speed for all graphs. (See Exponential-Normalized in the outputs.)