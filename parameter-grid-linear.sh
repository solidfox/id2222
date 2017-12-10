#!/usr/local/bin/fish
set -l graphs graphs/3elt.graph graphs/add20.graph graphs/twitter.graph
set -l temperatures 2 5 20
set -l speeds 0.001 0.004 0.019

function run
    ./run.sh -graph $argv[1] -annealing linear -temp $argv[2] -annealingSpeed $argv[3]
end

for graph in $graphs
    for temperature in $temperatures
        for speed in $speeds
            run $graph $temperature $speed
        end
    end
end