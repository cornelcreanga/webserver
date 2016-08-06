#!/usr/bin/env bash

ab -n 10000 -c 100 http://127.0.0.1:8082/ > test1.txt &
ab -n 10000 -c 100 http://127.0.0.1:8082/folder1 > test2.txt &
ab -n 10000 -c 100 http://127.0.0.1:8082/folder1/bigFile.txt > test3.txt &
ab -n 10000 -c 100 http://127.0.0.1:8082/folder1/notexisting > test4.txt &
ab -n 10000 -c 100 http://127.0.0.1:8082/folder1_1/ > test5.txt &