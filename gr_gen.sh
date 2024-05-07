#!/bin/bash

# Function to calculate m as c * (n * ln(n))
function calculate_m() {
    local n=$1
    local c=$2
    local m_float=$(echo "$c * $n * l($n)" | bc -l)
    echo ${m_float%.*}  # Round down to the nearest integer
}

# Main function to generate random graphs
function generate_graphs() {

    rm -rf rand_*.gr

    local seeds=("1234" "5678" "91011" "121314" "151617" "432423" "67574257" "8327625" "6682633")
    local n_values=(50 100 500 1000 5000 10000 50000 100000 200000)
    local c_values=(5 5 5 5 5 1 1 1 1)

    for ((i = 0; i < ${#n_values[@]}; i++)); do
        local n=${n_values[i]}
        local c=${c_values[i]}
        local m=$(calculate_m $n $c)
        local seed=${seeds[i]}
        local output_file="rand_${n}.gr"
        
        echo "Generating a random graph with n=$n, m=$m, and seed=$seed"

        ./sprand.exe $n $m 1 1 $seed > "./inputs/rands/$output_file"
    done
}

# Check if sprand executable exists
if [ ! -x "./sprand.exe" ]; then
    echo "Error: The 'sprand.exe' executable does not exist or is not executable."
    exit 1
fi

# Call the main function to generate graphs
generate_graphs