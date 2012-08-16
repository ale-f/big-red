#!/bin/sh

SELF="`readlink -f "$0"`"
SELFDIR="`dirname "$SELF"`"
SCHEMAPATH="$SELFDIR/../plugins/org.bigraph.model/resources/schema"

usage() {
	echo "Usage: $0 [bigraph|signature|rule|spec|edit] XML-DOCUMENT"
	exit "$1"
}

if [ "$1" != "" -a "$2" != "" ]; then
	FILE="$1"
	shift 1
	case "$FILE" in
	bigraph|signature|rule|spec|edit)
		FILE="$SCHEMAPATH/$FILE.xsd" ;;
	*)
		usage 1 ;;
	esac
	exec xmllint --schema "$FILE" "$1"
else
	usage 0
fi
