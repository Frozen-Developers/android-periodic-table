#!/usr/bin/env python
# -*- coding: utf-8 -*-

import urllib.request
import lxml.html
from lxml import etree
import re

OUTPUT_XML = 'PeriodicTable/src/main/res/raw/elements.xml'

def add_to_element(root, name, value):
	subelement = etree.SubElement(root, name)
	subelement.text = value

def fetch(url, root):
	content = lxml.html.fromstring(urllib.request.urlopen(url).read())

	nsm = content.xpath('//table[@class="infobox bordered"]/tr[th[contains(., "Name, ")]]/td/text()')[0].replace(",", "").split()

	saw = re.sub('\([0-9]?\)', '', content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Standard atomic weight")]]]/td/text()')[0]).replace('(', '[').replace(')', ']')
	try:
		saw = format(float(saw), '.3f').rstrip('0').rstrip('.')
	except ValueError:
		pass

	cat = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Element category")]]]/td/a/text()')[0]

	pb = content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Group")]]]/td/a/text()')
	grp = re.sub('[^0-9]', '', content.xpath('//table[@class="infobox bordered"]/tr[th[a[contains(., "Group")]]]/td/span/a/text()')[0].replace('n/a', '0'))


	element = etree.SubElement(root, 'element')
	element.attrib['number'] = nsm[2]

	add_to_element(element, 'symbol', nsm[1])
	add_to_element(element, 'name', nsm[0].capitalize())
	add_to_element(element, 'weight', saw)
	add_to_element(element, 'category', cat)
	add_to_element(element, 'group', grp)
	add_to_element(element, 'period', pb[0])
	add_to_element(element, 'block', pb[1])
	add_to_element(element, 'wiki', url)
	
	print(nsm[0].capitalize() + ', ' + nsm[1] + ', ' + nsm[2] + ', ' + saw + ', ' + cat + ', ' + grp + ', ' + pb[0] + ', ' + pb[1])

if __name__ == '__main__':
	pages = lxml.html.fromstring(urllib.request.urlopen('http://en.wikipedia.org/wiki/Periodic_table').read()).xpath('//table/tr/td/div[@title]/div/a/@href')

	root = etree.Element('elements')

	for page in pages:
		fetch('http://en.wikipedia.org' + page, root)

	with open(OUTPUT_XML, "w+") as out_file:
		out_file.write(etree.tostring(root, encoding='utf-8', pretty_print=True, xml_declaration=True).decode('utf-8'));
