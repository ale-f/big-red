#!/bin/sh

ead() {
	echo "$@" && "$@"
}

if [ ! -f "bigraph.jar" ]; then
	ead ./build-jar.sh
fi

if [ "$1" != "" ]; then
	FILE="$1"
	BASE="`basename "$1" .java`"
	shift 1
	(
		export "CLASSPATH=bigraph.jar:."
		if [ \( ! -e "$BASE.class" \) -o "$FILE" -nt "$BASE.class" ]; then
			ead javac "$FILE"
		fi
		ead java "$BASE" "$@"
	)
fi
