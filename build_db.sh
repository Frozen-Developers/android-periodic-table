#!/bin/bash

ELEMENTS_XML="Periodic Table/src/main/res/raw/elements.xml"

write_to_xml() {
	echo "$1" >> "$ELEMENTS_XML"
}

format_number() {
	if [[ $(echo "$1" | grep -e "\[[^][]*]") != "" ]] || [[ $(echo "$1" | grep "([^()]*)") != "" ]]; then
		echo "$1"
	else
		printf "%0.3f\n" "$1"		
	fi
}

fetch() {
	CONTENT=$(curl -s "$1")

	NSN=($(echo "$CONTENT" | awk "NR==$(expr $(echo "$CONTENT" | grep -n "<th>" | grep Name | grep symbol | grep number | cut -d ':' -f 1) + 1)" | \
		sed 's|<[^>]*>||g' | tr -d ','))

	SAW=$(format_number $(echo "$CONTENT" | awk "NR==$(expr $(echo "$CONTENT" | grep -n "<th>" | grep "Standard atomic weight" | cut -d ':' -f 1) + 1)" | \
		sed 's|<[^>]*>||g' | sed -e 's/([^(*)])//g' | sed -e 's/\[[^*][]*\]//g' | sed -e 's/&#160;//g') | sed -e 's/.000//g')

	CAT=$(echo "$CONTENT" | sed '/<td>unknown<br \/>$/d' | awk "NR==$(expr $(echo "$CONTENT" | sed '/<td>unknown<br \/>$/d' | grep -n "<th>" | \
		grep "Element category" | cut -d ':' -f 1) + 1)" | sed 's|<[^>]*>||g' | sed 's/but probably a //g' | tr -d '[:digit:]' | tr -d '[]()' | cut -d ',' -f 1)

	GPB=($(echo "$CONTENT" | awk "NR==$(expr $(echo "$CONTENT" | grep -n "<th>" | grep Group | grep period | grep block | cut -d ':' -f 1) + 1)" | \
		sed 's|<[^>]*>||g' | sed -e 's/([^()]*)//g' | sed 's/n\/a/0/' | tr -d ','))

	echo "${NSN[2]}, ${NSN[1]}, ${NSN[0]^}, $SAW, $CAT, ${GPB[0]}, ${GPB[1]}, ${GPB[2]}, $1"

	write_to_xml "	<element number=\"${NSN[2]}\">"

	write_to_xml "		<symbol>${NSN[1]}</symbol>"
	write_to_xml "		<name>${NSN[0]^}</name>"
	write_to_xml "		<weight>$SAW</weight>"
	write_to_xml "		<category>$CAT</category>"
	write_to_xml "		<group>${GPB[0]}</group>"
	write_to_xml "		<period>${GPB[1]}</period>"
	write_to_xml "		<block>${GPB[2]}</block>"
	write_to_xml "		<wiki>$1</wiki>"

	write_to_xml "	</element>"
}

URLS=( 	$(curl -s http://en.wikipedia.org/wiki/Periodic_table | \
		sed -n '/font-size:85%; vertical-align:top;">/,/<\/td>/p' | \
		grep -o '<a href=['"'"'"][^"'"'"']*['"'"'"]' | \
		sed -e 's/^<a href=["'"'"']//' -e 's/["'"'"']$//' | tr '\n' ' ')	)

rm "$ELEMENTS_XML" 2> /dev/null

write_to_xml '<?xml version="1.0" encoding="utf-8"?>'

write_to_xml "<elements>"

for i in ${!URLS[@]};
do
	fetch "http://en.wikipedia.org${URLS[$i]}" $1
done

write_to_xml "</elements>"
