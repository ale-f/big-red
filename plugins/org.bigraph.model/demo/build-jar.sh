#!/bin/sh

ead() {
	echo "$@" && "$@"
}

cd ..
(
	ead mkdir -p bin/
	ead javac -d bin/ `find src/ -iname "*.java"`
	ead jar cf bigraph.jar resources/ -C bin org/
	ead mv bigraph.jar demo/
)
