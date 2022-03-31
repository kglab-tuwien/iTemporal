#!/bin/sh

echo "Running script $1.vada with $2 seeding values"

DATAPATH=$(dirname $PWD)/data/$1

result=$(cat "vadalog/$1.vada" | sed -e "s#xxxx#${DATAPATH}#g" | sed -e "s#\.csv#_${2}.csv#g")


max=3
for i in `seq 1 $max`
do
echo "---"
curl "http://localhost:8080/evaluate" --data-urlencode "program=$result"
echo "---"
done

