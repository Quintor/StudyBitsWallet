#!/bin/bash
echo "Waiting for dockers to start"
while [ -n "$(docker ps -a | grep starting)" ]; 
do
    echo $(docker ps -a | grep starting)
    sleep 5
done 
echo "Dockers are booted"
