#!/usr/local/bin/fish
set -l graphs graphs/3elt.graph graphs/add20.graph graphs/twitter.graph
set -l temperatures 20 5 2
set -l speeds 0.997 0.9984 0.9993

function run
    ./run.sh -graph $argv[1] -annealing exponential -temp $argv[2] -annealingSpeed $argv[3]
end

for graph in $graphs
    for temperature in $temperatures
        for speed in $speeds
            run $graph $temperature $speed
        end
    end
end